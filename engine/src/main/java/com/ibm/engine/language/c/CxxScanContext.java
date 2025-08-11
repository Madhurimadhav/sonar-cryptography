package com.ibm.engine.language.c;

import com.ibm.engine.language.IScanContext;
import java.io.File;
import javax.annotation.Nonnull;
import org.sonar.api.batch.fs.InputFile;

public class CxxScanContext implements IScanContext<Object, CCallNode> {
    private final InputFile inputFile;

    public CxxScanContext(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public void reportIssue(@Nonnull Object rule, @Nonnull Object tree, @Nonnull String message) {
        // no-op
    }

    @Nonnull
    @Override
    public InputFile getInputFile() {
        return inputFile;
    }

    @Nonnull
    @Override
    public String getFilePath() {
        return new File(inputFile.uri()).getAbsolutePath();
    }
}
