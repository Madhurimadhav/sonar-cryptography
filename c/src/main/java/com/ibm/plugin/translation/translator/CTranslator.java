package com.ibm.plugin.translation.translator;

import com.ibm.engine.detection.DetectionStore;
import com.ibm.engine.model.IValue;
import com.ibm.engine.model.context.IDetectionContext;
import com.ibm.engine.rule.IBundle;
import com.ibm.mapper.ITranslator;
import com.ibm.mapper.model.INode;
import com.ibm.mapper.model.algorithms.AES;
import com.ibm.mapper.model.algorithms.RSA;
import com.ibm.mapper.model.algorithms.SHA2;
import com.ibm.mapper.utils.DetectionLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.sonar.api.batch.fs.InputFile;


/**
 * Very small translator that converts action values produced by detection
 * rules into algorithm nodes. Only a handful of WolfCrypt algorithms are
 * supported.
 */
public final class CTranslator extends ITranslator<Object, Object, Object, Object> {

    @Nonnull
    public List<INode> translate(@Nonnull DetectionStore<Object, Object, Object, Object> store) {
        List<INode> nodes = new ArrayList<>();
        collect(store, nodes);
        return nodes;
    }

    private void collect(DetectionStore<Object, Object, Object, Object> store, List<INode> out) {
        store.getActionValue()
                .ifPresent(
                        action -> {
                            DetectionLocation loc =
                                    getDetectionContextFrom(
                                            store,
                                            store.getDetectionRule().bundle(),
                                            store.getScanContext().getFilePath());
                            String value = action.asString();
                            switch (value) {
                                case "AES" -> out.add(new AES(loc));
                                case "SHA-256" -> out.add(new SHA2(256, loc));
                                case "RSA" -> out.add(new RSA(loc));
                                default -> {
                                }
                            }
                        });
        store.getChildren().forEach(child -> collect(child, out));
    }

    @Override
    public Optional<INode> translate(
            @Nonnull IBundle bundle,
            @Nonnull IValue<Object> value,
            @Nonnull IDetectionContext detectionValueContext,
            @Nonnull String filePath) {
        DetectionLocation detectionLocation =
                getDetectionContextFrom(value.asString(), bundle, filePath);
        if (detectionLocation == null) {
            return Optional.empty();
        }

        String algorithm = value.asString();
        return switch (algorithm) {
            case "AES" -> Optional.of(new AES(detectionLocation));
            case "SHA-256" -> Optional.of(new SHA2(256, detectionLocation));
            case "RSA" -> Optional.of(new RSA(detectionLocation));
            default -> Optional.empty();
        };
    }

    @Override
    @Nullable
    protected DetectionLocation getDetectionContextFrom(
            @Nonnull Object location, @Nonnull IBundle bundle, @Nonnull String filePath) {
        return new DetectionLocation(filePath, 1, 0, List.of(String.valueOf(location)), bundle);
    }

    @Override
    protected DetectionLocation getDetectionContextFrom(
            @Nonnull Object location, @Nonnull com.ibm.engine.rule.IBundle bundle, @Nonnull String filePath) {
        if (location instanceof DetectionStore<?, ?, ?, ?> store) {
            String keyword =
                    store.getActionValue().map(av -> av.asString()).orElse("");
            int line = 1;
            int column = 0;
            try {
                InputFile inputFile = ((DetectionStore<?, ?, ?, ?>) location).getScanContext().getInputFile();
                String content = inputFile.contents();
                int index = keyword.isEmpty() ? -1 : content.indexOf(keyword);
                if (index >= 0) {
                    for (int i = 0; i < index; i++) {
                        if (content.charAt(i) == '\n') {
                            line++;
                            column = 0;
                        } else {
                            column++;
                        }
                    }
                }
            } catch (IOException e) {
                // ignore and keep defaults
            }
            List<String> keywords = keyword.isEmpty() ? List.of() : List.of(keyword);
            return new DetectionLocation(filePath, line, column, keywords, bundle);
        }
        return new DetectionLocation(filePath, 1, 0, List.of(), bundle);
    }
}
