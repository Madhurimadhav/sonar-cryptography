package com.ibm.engine.language.c;

import com.ibm.engine.detection.IType;
import com.ibm.engine.detection.MatchContext;
import com.ibm.engine.language.ILanguageTranslation;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class CxxLanguageTranslation implements ILanguageTranslation<Object> {
    private static final Pattern CALL_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)\\s*\\(");

    @Nonnull
    @Override
    public Optional<String> getMethodName(@Nonnull MatchContext matchContext, @Nonnull Object methodInvocation) {
        if (methodInvocation instanceof String call) {
            Matcher m = CALL_PATTERN.matcher(call);
            if (m.find()) {
                return Optional.of(m.group(1));
            }
        } else if (methodInvocation instanceof AstNode node) {
            for (AstNode child : node.getChildren()) {
                Token token = child.getToken();
                if (token != null
                        && token.getValue() != null
                        && token.getValue().matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    return Optional.of(token.getValue());
                }
            }
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<IType> getInvokedObjectTypeString(@Nonnull MatchContext matchContext, @Nonnull Object methodInvocation) {
        return getMethodName(matchContext, methodInvocation)
                .flatMap(
                        name -> {
                            if (name.startsWith("wc_")) {
                                return Optional.of((IType) (String s) -> s.equals("wolfssl"));
                            }
                            if (name.startsWith("AES_")) {
                                return Optional.of((IType) (String s) -> s.equals("openssl"));
                            }
                            return Optional.empty();
                        });
    }

    @Nonnull
    @Override
    public Optional<IType> getMethodReturnTypeString(@Nonnull MatchContext matchContext, @Nonnull Object methodInvocation) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public List<IType> getMethodParameterTypes(@Nonnull MatchContext matchContext, @Nonnull Object methodInvocation) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Optional<String> resolveIdentifierAsString(@Nonnull MatchContext matchContext, @Nonnull Object name) {
        if (name instanceof String s) {
            return Optional.of(s);
        } else if (name instanceof AstNode node) {
            Token token = node.getToken();
            if (token != null && token.getValue() != null) {
                return Optional.of(token.getValue());
            }
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<String> getEnumIdentifierName(@Nonnull MatchContext matchContext, @Nonnull Object enumIdentifier) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<String> getEnumClassName(@Nonnull MatchContext matchContext, @Nonnull Object enumClass) {
        return Optional.empty();
    }
}
