package com.ibm.plugin.translation.translator;

import com.ibm.engine.detection.DetectionStore;
import com.ibm.mapper.ITranslator;
import com.ibm.mapper.model.INode;
import com.ibm.mapper.model.algorithms.AES;
import com.ibm.mapper.model.algorithms.RSA;
import com.ibm.mapper.model.algorithms.SHA2;
import com.ibm.mapper.utils.DetectionLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

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
                            String value = action.asString();
                            DetectionLocation loc =
                                    new DetectionLocation(
                                            store.getScanContext().getFilePath(),
                                            1,
                                            0,
                                            List.of(value),
                                            store.getDetectionRule().bundle());
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
            @Nonnull com.ibm.engine.rule.IBundle bundle,
            @Nonnull com.ibm.engine.model.IValue<Object> value,
            @Nonnull com.ibm.engine.model.context.IDetectionContext detectionValueContext,
            @Nonnull String filePath) {
        return Optional.empty();
    }
}
