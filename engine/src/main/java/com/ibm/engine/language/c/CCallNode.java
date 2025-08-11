package com.ibm.engine.language.c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** Simple representation of a C function call extracted from source text. */
public final class CCallNode {
    @Nonnull private final String name;
    @Nonnull private final List<String> args;
    private final int line;

    public CCallNode(@Nonnull String name, @Nonnull List<String> args, int line) {
        this.name = name;
        this.args = new ArrayList<>(args);
        this.line = line;
    }

    @Nonnull
    public String name() {
        return name;
    }

    @Nonnull
    public List<String> args() {
        return Collections.unmodifiableList(args);
    }

    public int line() {
        return line;
    }
}

