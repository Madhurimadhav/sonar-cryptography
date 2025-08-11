package com.ibm.engine.language.c;

import com.ibm.engine.detection.IBaseMethodVisitor;
import com.ibm.engine.detection.IDetectionEngine;
import com.ibm.engine.detection.TraceSymbol;
import javax.annotation.Nonnull;

public class CxxBaseMethodVisitor implements IBaseMethodVisitor<CCallNode> {
    @Nonnull private final TraceSymbol<Object> traceSymbol;
    @Nonnull private final IDetectionEngine<CCallNode, Object> detectionEngine;

    public CxxBaseMethodVisitor(
            @Nonnull TraceSymbol<Object> traceSymbol,
            @Nonnull IDetectionEngine<CCallNode, Object> detectionEngine) {
        this.traceSymbol = traceSymbol;
        this.detectionEngine = detectionEngine;
    }

    @Override
    public void visitMethodDefinition(@Nonnull CCallNode method) {
        // no-op for now

    }
}
