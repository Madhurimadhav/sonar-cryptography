package com.ibm.plugin;

import com.ibm.engine.language.ILanguageSupport;
import com.ibm.engine.language.LanguageSupporter;
import com.ibm.engine.language.c.CCallNode;
import com.ibm.mapper.model.INode;
import com.ibm.output.IAggregator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class CAggregator implements IAggregator {
    private static ILanguageSupport<Object, CCallNode, Object, Object> cLanguageSupport =
            LanguageSupporter.cLanguageSupporter();
    private static List<INode> detectedNodes = new ArrayList<>();

    private CAggregator() {}

    public static void addNodes(@Nonnull List<INode> newNodes) {
        detectedNodes.addAll(newNodes);
        IAggregator.log(newNodes);
    }

    @Nonnull
    public static List<INode> getDetectedNodes() {
        return Collections.unmodifiableList(detectedNodes);
    }

    @Nonnull
    public static ILanguageSupport<Object, CCallNode, Object, Object> getLanguageSupport() {
        return cLanguageSupport;
    }

    public static void reset() {
        cLanguageSupport = LanguageSupporter.cLanguageSupporter();
        detectedNodes = new ArrayList<>();
    }
}
