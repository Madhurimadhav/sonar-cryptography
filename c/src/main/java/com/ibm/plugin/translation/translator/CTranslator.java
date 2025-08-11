package com.ibm.plugin.translation.translator;

import com.ibm.engine.model.IValue;
import com.ibm.engine.model.context.IDetectionContext;
import com.ibm.engine.rule.IBundle;
import com.ibm.engine.language.c.CCallNode;
import com.ibm.mapper.ITranslator;
import com.ibm.mapper.model.INode;
import com.ibm.mapper.model.algorithms.AES;
import com.ibm.mapper.model.algorithms.RSA;
import com.ibm.mapper.model.algorithms.SHA2;
import com.ibm.mapper.utils.DetectionLocation;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;


/**
 * Very small translator that converts action values produced by detection
 * rules into algorithm nodes. Only a handful of WolfCrypt algorithms are
 * supported.
 */
public final class CTranslator extends ITranslator<Object, CCallNode, Object, Object> {

    @Override
    public Optional<INode> translate(
            @Nonnull IBundle bundle,
            @Nonnull IValue<CCallNode> value,
            @Nonnull IDetectionContext detectionValueContext,
            @Nonnull String filePath) {
        DetectionLocation detectionLocation =
                getDetectionContextFrom(value.getLocation(), bundle, filePath);
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
    protected DetectionLocation getDetectionContextFrom(
            @Nonnull CCallNode location, @Nonnull IBundle bundle, @Nonnull String filePath) {
        List<String> keywords = List.of(location.name());
        return new DetectionLocation(filePath, location.line(), 0, keywords, bundle);
    }
}
