package com.ibm.plugin.rules.detection;

import com.ibm.common.IObserver;
import com.ibm.engine.detection.DetectionStore;
import com.ibm.engine.detection.Finding;
import com.ibm.engine.executive.DetectionExecutive;
import com.ibm.engine.language.c.CxxScanContext;
import com.ibm.engine.rule.IDetectionRule;
import com.ibm.mapper.model.INode;
import com.ibm.mapper.reorganizer.IReorganizerRule;
import com.ibm.plugin.CAggregator;
import com.ibm.plugin.translation.CTranslationProcess;
import com.ibm.rules.IReportableDetectionRule;
import com.ibm.rules.InventoryRule;
import com.ibm.rules.issue.Issue;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.sonar.api.batch.fs.InputFile;

/**
 * Base class for C/C++ rules. It wires the detection engine with the translation
 * process so that detected findings can be converted into mapper nodes and
 * ultimately written to the CBOM.
 */
public abstract class CBaseDetectionRule
        implements IObserver<Finding<Object, Object, Object, Object>>,
                IReportableDetectionRule<Object> {

    private final boolean isInventory;
    @Nonnull protected final CTranslationProcess translationProcess;
    @Nonnull protected final List<IDetectionRule<Object>> detectionRules;

    protected CBaseDetectionRule() {
        this(false, CDetectionRules.rules(), Collections.emptyList());
    }

    protected CBaseDetectionRule(
            boolean isInventory,
            @Nonnull List<IDetectionRule<Object>> detectionRules,
            @Nonnull List<IReorganizerRule> reorganizerRules) {
        this.isInventory = isInventory;
        this.detectionRules = detectionRules;
        this.translationProcess = new CTranslationProcess(reorganizerRules);
    }

    /**
     * Scans a file by running all configured detection rules against its
     * textual content.
     */
    public void scanFile(@Nonnull String content, @Nonnull InputFile inputFile) {
        detectionRules.forEach(
                rule -> {
                    DetectionExecutive<Object, Object, Object, Object> exec =
                            CAggregator.getLanguageSupport()
                                    .createDetectionExecutive(
                                            content, rule, new CxxScanContext(inputFile));
                    exec.subscribe(this);
                    exec.start();
                });
    }

    @Override
    public void update(@Nonnull Finding<Object, Object, Object, Object> finding) {
        List<INode> nodes = translationProcess.initiate(finding.detectionStore());
        if (isInventory) {
            CAggregator.addNodes(nodes);
        }
        report(finding.getMarkerTree(), nodes)
                .forEach(issue ->
                        finding.detectionStore()
                                .getScanContext()
                                .reportIssue(this, issue.tree(), issue.message()));
    }

    @Override
    public @Nonnull List<Issue<Object>> report(
            @Nonnull Object markerTree, @Nonnull List<INode> nodes) {
        return new InventoryRule<>().report(markerTree, nodes);
    }
}
