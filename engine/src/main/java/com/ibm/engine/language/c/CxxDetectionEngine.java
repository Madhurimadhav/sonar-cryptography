package com.ibm.engine.language.c;

import com.ibm.engine.detection.DetectionStore;
import com.ibm.engine.detection.Handler;
import com.ibm.engine.detection.IDetectionEngine;
import com.ibm.engine.detection.MethodDetection;
import com.ibm.engine.detection.ResolvedValue;
import com.ibm.engine.detection.TraceSymbol;
import com.ibm.engine.language.ILanguageTranslation;
import com.ibm.engine.rule.Parameter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Detection engine for the C front-end operating on {@link CCallNode} objects. */
public final class CxxDetectionEngine implements IDetectionEngine<CCallNode, Object> {
    private final DetectionStore<Object, CCallNode, Object, Object> detectionStore;
    private final Handler<Object, CCallNode, Object, Object> handler;

    public CxxDetectionEngine(
            @Nonnull DetectionStore<Object, CCallNode, Object, Object> detectionStore,
            @Nonnull Handler<Object, CCallNode, Object, Object> handler) {
        this.detectionStore = detectionStore;
        this.handler = handler;
    }

    @Override
    public void run(@Nonnull CCallNode tree) {
        run(TraceSymbol.createStart(), tree);
    }

    @Override
    public void run(@Nonnull TraceSymbol<Object> traceSymbol, @Nonnull CCallNode tree) {
        ILanguageTranslation<CCallNode> translation = handler.getLanguageSupport().translation();
        if (detectionStore.getDetectionRule().match(tree, translation)) {
            MethodDetection<CCallNode> methodDetection = new MethodDetection<>(tree, null);
            detectionStore.onReceivingNewDetection(methodDetection);
        }
    }

    @Override
    public @Nullable CCallNode extractArgumentFromMethodCaller(
            @Nonnull CCallNode methodDefinition,
            @Nonnull CCallNode methodInvocation,
            @Nonnull CCallNode methodParameterIdentifier) {
        return null;
    }

    @Nonnull
    @Override
    public <O> List<ResolvedValue<O, CCallNode>> resolveValuesInInnerScope(
            @Nonnull Class<O> clazz,
            @Nonnull CCallNode expression,
            @Nullable com.ibm.engine.model.factory.IValueFactory<CCallNode> valueFactory) {
        return Collections.emptyList();
    }

    @Override
    public void resolveValuesInOuterScope(@Nonnull CCallNode expression, @Nonnull Parameter<CCallNode> parameter) {}

    @Override
    public <O> void resolveMethodReturnValues(
            @Nonnull Class<O> clazz,
            @Nonnull CCallNode methodDefinition,
            @Nonnull Parameter<CCallNode> parameter) {}

    @Nullable
    @Override
    public <O> ResolvedValue<O, CCallNode> resolveEnumValue(
            @Nonnull Class<O> clazz,
            @Nonnull CCallNode enumClassDefinition,
            @Nonnull LinkedList<CCallNode> selections) {
        return null;
    }

    @Nonnull
    @Override
    public Optional<TraceSymbol<Object>> getAssignedSymbol(@Nonnull CCallNode expression) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<TraceSymbol<Object>> getMethodInvocationParameterSymbol(
            @Nonnull CCallNode methodInvocation, @Nonnull Parameter<CCallNode> parameter) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<TraceSymbol<Object>> getNewClassParameterSymbol(
            @Nonnull CCallNode newClass, @Nonnull Parameter<CCallNode> parameter) {
        return Optional.empty();
    }

    @Override
    public boolean isInvocationOnVariable(
            CCallNode methodInvocation, @Nonnull TraceSymbol<Object> variableSymbol) {
        return false;
    }

    @Override
    public boolean isInitForVariable(CCallNode newClass, @Nonnull TraceSymbol<Object> variableSymbol) {
        return false;
    }
}

