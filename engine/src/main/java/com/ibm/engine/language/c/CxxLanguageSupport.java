package com.ibm.engine.language.c;

import com.ibm.engine.detection.DetectionStore;
import com.ibm.engine.detection.EnumMatcher;
import com.ibm.engine.detection.Handler;
import com.ibm.engine.detection.IBaseMethodVisitorFactory;
import com.ibm.engine.detection.IDetectionEngine;
import com.ibm.engine.detection.MatchContext;
import com.ibm.engine.detection.MethodMatcher;
import com.ibm.engine.executive.DetectionExecutive;
import com.ibm.engine.language.ILanguageSupport;
import com.ibm.engine.language.ILanguageTranslation;
import com.ibm.engine.language.IScanContext;
import com.ibm.engine.rule.IDetectionRule;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CxxLanguageSupport implements ILanguageSupport<Object, CCallNode, Object, Object> {
    @Nonnull private final Handler<Object, CCallNode, Object, Object> handler;

    public CxxLanguageSupport() {
        this.handler = new Handler<>(this);
    }

    @Nonnull
    @Override
    public ILanguageTranslation<CCallNode> translation() {
        return new CxxLanguageTranslation();
    }

    @Nonnull
    @Override
    public DetectionExecutive<Object, CCallNode, Object, Object> createDetectionExecutive(
            @Nonnull CCallNode tree,
            @Nonnull IDetectionRule<CCallNode> detectionRule,
            @Nonnull IScanContext<Object, CCallNode> scanContext) {
        return new DetectionExecutive<>(tree, detectionRule, scanContext, this.handler);
    }

    @Nonnull
    @Override
    public IDetectionEngine<CCallNode, Object> createDetectionEngineInstance(
            @Nonnull DetectionStore<Object, CCallNode, Object, Object> detectionStore) {
        return new CxxDetectionEngine(detectionStore, this.handler);
    }

    @Nonnull
    @Override
    public IBaseMethodVisitorFactory<CCallNode, Object> getBaseMethodVisitorFactory() {
        return (traceSymbol, detectionEngine) ->
                new CxxBaseMethodVisitor(traceSymbol, detectionEngine);
    }

    @Nonnull
    @Override
    public Optional<CCallNode> getEnclosingMethod(@Nonnull CCallNode expression) {
        return Optional.empty();
    }

    @Nullable
    @Override
    public MethodMatcher<CCallNode> createMethodMatcherBasedOn(@Nonnull CCallNode methodDefinition) {
        return null;
    }

    @Nullable
    @Override
    public EnumMatcher<CCallNode> createSimpleEnumMatcherFor(
            @Nonnull CCallNode enumIdentifier, @Nonnull MatchContext matchContext) {
        return null;
    }
}
