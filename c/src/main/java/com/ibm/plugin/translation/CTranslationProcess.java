package com.ibm.plugin.translation;

import com.ibm.engine.detection.DetectionStore;
import com.ibm.engine.language.c.CCallNode;
import com.ibm.enricher.Enricher;
import com.ibm.mapper.ITranslationProcess;
import com.ibm.mapper.model.INode;
import com.ibm.mapper.reorganizer.IReorganizerRule;
import com.ibm.mapper.reorganizer.Reorganizer;
import com.ibm.mapper.utils.Utils;
import com.ibm.plugin.translation.translator.CTranslator;
import java.util.List;
import javax.annotation.Nonnull;

public final class CTranslationProcess
        extends ITranslationProcess<Object, CCallNode, Object, Object> {

    public CTranslationProcess(@Nonnull List<IReorganizerRule> reorganizerRules) {
        super(reorganizerRules);
    }

    @Override
    @Nonnull
    public List<INode> initiate(
            @Nonnull DetectionStore<Object, CCallNode, Object, Object> rootDetectionStore) {
        CTranslator translator = new CTranslator();
        List<INode> translated = translator.translate(rootDetectionStore);
        Utils.printNodeTree("translated ", translated);

        Reorganizer reorganizer = new Reorganizer(reorganizerRules);
        List<INode> reorganized = reorganizer.reorganize(translated);
        Utils.printNodeTree("reorganised", reorganized);

        List<INode> enriched = Enricher.enrich(reorganized).stream().toList();
        Utils.printNodeTree("enriched   ", enriched);

        return enriched;
    }
}
