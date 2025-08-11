package com.ibm.plugin.rules.detection;

import com.ibm.common.IObserver;
import com.ibm.engine.detection.DetectionStore;
import com.ibm.engine.detection.Finding;
import com.ibm.engine.executive.DetectionExecutive;
import com.ibm.engine.language.c.CCallNode;
import com.ibm.engine.language.c.CxxScanContext;
import com.ibm.engine.rule.IDetectionRule;
import com.ibm.mapper.model.INode;
import com.ibm.mapper.reorganizer.IReorganizerRule;
import com.ibm.plugin.CAggregator;
import com.ibm.plugin.translation.CTranslationProcess;
import com.ibm.rules.IReportableDetectionRule;
import com.ibm.rules.InventoryRule;
import com.ibm.rules.issue.Issue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.sonar.api.batch.fs.InputFile;

/**
 * Base class for C/C++ rules. It wires the detection engine with the translation
 * process so that detected findings can be converted into mapper nodes and
 * ultimately written to the CBOM.
 */
public abstract class CBaseDetectionRule
        implements IObserver<Finding<Object, CCallNode, Object, Object>>,
                IReportableDetectionRule<CCallNode> {

    private static final Pattern CALL_PATTERN = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");
    private static final Set<String> KEYWORDS = Set.of("if", "while", "for", "switch", "return");

    private final boolean isInventory;
    @Nonnull protected final CTranslationProcess translationProcess;
    @Nonnull protected final List<IDetectionRule<CCallNode>> detectionRules;

    protected CBaseDetectionRule() {
        this(false, CDetectionRules.rules(), Collections.emptyList());
    }

    protected CBaseDetectionRule(
            boolean isInventory,
            @Nonnull List<IDetectionRule<CCallNode>> detectionRules,
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
        List<CCallNode> nodes = new ArrayList<>();
        String[] lines = content.split("\n");
        boolean inBlockComment = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            if (inBlockComment) {
                if (trimmed.contains("*/")) {
                    inBlockComment = false;
                }
                continue;
            }
            if (trimmed.startsWith("/*")) {
                inBlockComment = !trimmed.contains("*/");
                continue;
            }
            if (trimmed.startsWith("//") || trimmed.startsWith("#")) {
                continue;
            }
            Matcher m = CALL_PATTERN.matcher(line);
            while (m.find()) {
                String name = m.group(1);
                if (KEYWORDS.contains(name) || !name.toLowerCase().contains("aes")) {
                    continue;
                }
                nodes.add(new CCallNode(name, List.of(), i + 1));
            }
        }
        for (CCallNode node : nodes) {
            for (IDetectionRule<CCallNode> rule : detectionRules) {
                DetectionExecutive<Object, CCallNode, Object, Object> exec =
                        CAggregator.getLanguageSupport()
                                .createDetectionExecutive(node, rule, new CxxScanContext(inputFile));
                exec.subscribe(this);
                exec.start();
            }
        }
    }

    @Override
    public void update(@Nonnull Finding<Object, CCallNode, Object, Object> finding) {
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
    public @Nonnull List<Issue<CCallNode>> report(
            @Nonnull CCallNode markerTree, @Nonnull List<INode> nodes) {
        return new InventoryRule<>().report(markerTree, nodes);
    }
}
