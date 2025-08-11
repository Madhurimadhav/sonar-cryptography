package com.ibm.engine.language.c;

import com.ibm.engine.detection.IType;
import com.ibm.engine.detection.MatchContext;
import com.ibm.engine.language.ILanguageTranslation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

/** Translation utilities for {@link CCallNode} objects. */
public final class CxxLanguageTranslation implements ILanguageTranslation<CCallNode> {
    @Nonnull
    @Override
    public Optional<String> getMethodName(@Nonnull MatchContext matchContext, @Nonnull CCallNode methodInvocation) {
        return Optional.of(methodInvocation.name());
    }

    @Nonnull
    @Override
    public Optional<IType> getInvokedObjectTypeString(
            @Nonnull MatchContext matchContext, @Nonnull CCallNode methodInvocation) {
        return Optional.of(type -> true); // free functions
    }

    @Nonnull
    @Override
    public Optional<IType> getMethodReturnTypeString(
            @Nonnull MatchContext matchContext, @Nonnull CCallNode methodInvocation) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public List<IType> getMethodParameterTypes(
            @Nonnull MatchContext matchContext, @Nonnull CCallNode methodInvocation) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Optional<String> resolveIdentifierAsString(
            @Nonnull MatchContext matchContext, @Nonnull CCallNode name) {
        return Optional.of(name.name());
    }

    @Nonnull
    @Override
    public Optional<String> getEnumIdentifierName(
            @Nonnull MatchContext matchContext, @Nonnull CCallNode enumIdentifier) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<String> getEnumClassName(
            @Nonnull MatchContext matchContext, @Nonnull CCallNode enumClass) {
        return Optional.empty();
    }
}

