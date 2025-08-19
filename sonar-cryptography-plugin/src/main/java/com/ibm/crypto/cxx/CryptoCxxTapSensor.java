package com.ibm.crypto.cxx;

import com.ibm.crypto.CbomAsset;
import com.ibm.crypto.CryptoInventoryWriter;
import com.ibm.crypto.catalog.CryptoFunctionCatalog;
import com.ibm.crypto.catalog.CryptoFunctionCatalog.Info;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Sensor that taps into artifacts produced by the sonar-cxx plugin and
 * derives cryptographic assets without relying on regex or AST traversal.
 */
public class CryptoCxxTapSensor implements Sensor {
    private static final Logger LOG = Loggers.get(CryptoCxxTapSensor.class);

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("CryptoCxxTapSensor")
                .onlyOnLanguages("c", "cxx", "cpp")
                .onlyWhenConfiguration(c -> true);
    }

    @Override
    public void execute(SensorContext context) {
        Path work = context.fileSystem().workDir().toPath();
        Map<String, List<FuncHit>> hits = new HashMap<>();

        for (Path p : discoverCxxArtifacts(work)) {
            try {
                consumeOneArtifact(p, hits);
            } catch (IOException e) {
                context.newAnalysisError()
                        .message("CryptoCxxTapSensor: failed parsing " + p.getFileName() + " : " + e.getMessage())
                        .save();
            }
        }

        List<CbomAsset> assets = buildAssetsFromHits(hits, context.fileSystem());
        if (!assets.isEmpty()) {
            CryptoInventoryWriter.writeCbomJson(context.fileSystem().workDir().toPath(), assets);
        }
    }

    private static List<Path> discoverCxxArtifacts(Path workDir) {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(workDir)) {
            return result;
        }
        try {
            Files.walk(workDir)
                    .filter(Files::isRegularFile)
                    .filter(f -> {
                        String s = f.getFileName().toString().toLowerCase(Locale.ROOT);
                        return s.endsWith(".json") || s.endsWith(".xml") || s.contains("cxx");
                    })
                    .forEach(result::add);
        } catch (IOException ignored) {
        }
        return result;
    }

    private static void consumeOneArtifact(Path file, Map<String, List<FuncHit>> hits) throws IOException {
        String content = Files.readString(file);
        String trimmed = content.trim();
        try {
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                parseJson(new JSONObject(content), file, hits);
            } else if (trimmed.startsWith("<")) {
                // Very small XML handler: look for elements named "function" with attributes
                parseXml(file, hits);
            }
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private static void parseJson(Object json, Path file, Map<String, List<FuncHit>> hits) {
        if (json instanceof JSONObject obj) {
            if (obj.has("functions") && obj.get("functions") instanceof JSONArray arr) {
                for (int i = 0; i < arr.length(); i++) {
                    Object o = arr.get(i);
                    if (o instanceof JSONObject fn) {
                        String name = fn.optString("name", null);
                        int line = fn.optInt("line", -1);
                        String path = fn.optString("file", file.toString());
                        if (name != null) {
                            addHit(hits, name, path, line);
                        }
                    }
                }
            }
            for (String k : obj.keySet()) {
                parseJson(obj.get(k), file, hits);
            }
        } else if (json instanceof JSONArray arr) {
            for (int i = 0; i < arr.length(); i++) {
                parseJson(arr.get(i), file, hits);
            }
        }
    }

    private static void parseXml(Path file, Map<String, List<FuncHit>> hits) {
        // Minimal XML extraction: read lines and look for attributes name= and line=
        try {
            List<String> lines = Files.readAllLines(file);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int nIdx = line.indexOf("name=\"");
                if (nIdx >= 0) {
                    int end = line.indexOf('\"', nIdx + 6);
                    if (end > nIdx) {
                        String name = line.substring(nIdx + 6, end);
                        addHit(hits, name, file.toString(), i + 1);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void addHit(Map<String, List<FuncHit>> hits, String function, String filePath, int line) {
        hits.computeIfAbsent(function, k -> new ArrayList<>())
                .add(new FuncHit(function, filePath, line));
    }

    private static List<CbomAsset> buildAssetsFromHits(Map<String, List<FuncHit>> hits, FileSystem fs) {
        List<CbomAsset> out = new ArrayList<>();
        for (Map.Entry<String, List<FuncHit>> e : hits.entrySet()) {
            String fun = e.getKey();
            Optional<Info> meta = CryptoFunctionCatalog.lookup(fun);
            if (meta.isEmpty()) {
                continue;
            }
            for (FuncHit h : e.getValue()) {
                out.add(CbomAsset.from(meta.get(), h.filePath, h.line, fun));
            }
        }
        return out;
    }

    private static final class FuncHit {
        final String function;
        final String filePath;
        final int line;

        FuncHit(String function, String filePath, int line) {
            this.function = function;
            this.filePath = filePath;
            this.line = line;
        }
    }
}
