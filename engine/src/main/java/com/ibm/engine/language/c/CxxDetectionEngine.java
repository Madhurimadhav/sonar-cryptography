package com.ibm.engine.language.c;

import com.ibm.engine.detection.DetectionStore;
import com.ibm.engine.detection.Handler;
import com.ibm.engine.detection.IDetectionEngine;

import com.ibm.engine.detection.MethodDetection;
import com.ibm.engine.detection.ResolvedValue;
import com.ibm.engine.detection.TraceSymbol;
import com.ibm.engine.executive.DetectionExecutive;
import com.ibm.engine.language.ILanguageTranslation;
import com.ibm.engine.rule.DetectionRule;
import com.ibm.engine.rule.MethodDetectionRule;
import com.ibm.engine.rule.Parameter;
import com.sonar.sslr.api.AstNode;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.cxx.parser.CxxParser;

public class CxxDetectionEngine implements IDetectionEngine<Object, Object> {
    private final DetectionStore<Object, Object, Object, Object> detectionStore;
    private final Handler<Object, Object, Object, Object> handler;

    public CxxDetectionEngine(
            @Nonnull DetectionStore<Object, Object, Object, Object> detectionStore,
            @Nonnull Handler<Object, Object, Object, Object> handler) {
        this.detectionStore = detectionStore;
        this.handler = handler;
    }

    @Override
    public void run(@Nonnull Object tree) {
        run(TraceSymbol.createStart(), tree);
    }

    @Override
    public void run(@Nonnull TraceSymbol<Object> traceSymbol, @Nonnull Object tree) {
        AstNode root = null;
        if (tree instanceof AstNode ast) {
            root = ast;
        } else if (tree instanceof String code) {
            root = (AstNode) CxxParser.createParser().parse(new StringReader(code));
        }
        if (root == null) {
            return;
        }

        ILanguageTranslation<Object> translation = handler.getLanguageSupport().translation();

        if (detectionStore.getDetectionRule().is(MethodDetectionRule.class)) {
            traverseCallNodes(root, translation);
            return;
        }

        if (detectionStore.getDetectionRule().match(root, translation)) {
            analyseExpression(root);
        }
    }

    private void traverseCallNodes(@Nonnull AstNode node, @Nonnull ILanguageTranslation<Object> translation) {
        String typeName = node.getType() != null ? node.getType().toString().toLowerCase() : "";
        if (typeName.contains("call") && detectionStore.getDetectionRule().match(node, translation)) {
            analyseExpression(node);
        }
        for (AstNode child : node.getChildren()) {
            traverseCallNodes(child, translation);
        }
    }

    private void analyseExpression(@Nonnull Object expression) {
        if (detectionStore.getDetectionRule().is(MethodDetectionRule.class)) {
            MethodDetection<Object> methodDetection = new MethodDetection<>(expression, null);
            detectionStore.onReceivingNewDetection(methodDetection);
            return;
        }

        if (detectionStore.getDetectionRule() instanceof DetectionRule<?> detectionRule && detectionRule.actionFactory() != null) {
            MethodDetection<Object> methodDetection = new MethodDetection<>(expression, null);
            detectionStore.onReceivingNewDetection(methodDetection);
        }
    }

    @Override
    public @Nullable Object extractArgumentFromMethodCaller(
            @Nonnull Object methodDefinition,
            @Nonnull Object methodInvocation,
            @Nonnull Object methodParameterIdentifier) {
        return null;
    }

    @Nonnull
    @Override
    public <O> List<ResolvedValue<O, Object>> resolveValuesInInnerScope(
            @Nonnull Class<O> clazz,
            @Nonnull Object expression,
            @Nullable com.ibm.engine.model.factory.IValueFactory<Object> valueFactory) {
        return Collections.emptyList();
    }

    @Override
    public void resolveValuesInOuterScope(@Nonnull Object expression, @Nonnull Parameter<Object> parameter) {}

    @Override
    public <O> void resolveMethodReturnValues(
            @Nonnull Class<O> clazz, @Nonnull Object methodDefinition, @Nonnull Parameter<Object> parameter) {}

    @Nullable
    @Override
    public <O> ResolvedValue<O, Object> resolveEnumValue(
            @Nonnull Class<O> clazz,
            @Nonnull Object enumClassDefinition,
            @Nonnull LinkedList<Object> selections) {
        return null;
    }

    @Nonnull
    @Override
    public Optional<TraceSymbol<Object>> getAssignedSymbol(@Nonnull Object expression) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<TraceSymbol<Object>> getMethodInvocationParameterSymbol(
            @Nonnull Object methodInvocation, @Nonnull Parameter<Object> parameter) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<TraceSymbol<Object>> getNewClassParameterSymbol(
            @Nonnull Object newClass, @Nonnull Parameter<Object> parameter) {
        return Optional.empty();
    }

    @Override
    public boolean isInvocationOnVariable(Object methodInvocation, @Nonnull TraceSymbol<Object> variableSymbol) {
        return false;
    }

    @Override
    public boolean isInitForVariable(Object newClass, @Nonnull TraceSymbol<Object> variableSymbol) {
        return false;
    }
}
