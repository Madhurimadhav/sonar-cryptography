package com.ibm.crypto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility to emit a consolidated cbom.json into the scanner work directory.
 * Analysis should not fail if writing the file fails.
 */
public final class CryptoInventoryWriter {
    private CryptoInventoryWriter() {}

    public static void writeCbomJson(Path workDir, List<CbomAsset> assets) {
        JSONObject root = new JSONObject()
                .put("version", "1.0")
                .put("generated_by", "sonar-cryptography");

        JSONArray arr = new JSONArray();
        for (CbomAsset a : assets) {
            arr.put(new JSONObject()
                    .put("provider", a.provider())
                    .put("algorithm", a.algorithm())
                    .put("mode", a.mode())
                    .put("file", a.file())
                    .put("line", a.line())
                    .put("function", a.function()));
        }
        root.put("assets", arr);

        try {
            Path out = workDir.resolve("cbom.json");
            Files.createDirectories(out.getParent());
            Files.writeString(out, root.toString(2));
        } catch (IOException e) {
            // swallow, analysis should continue
        }
    }
}
