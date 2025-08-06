package com.ibm.plugin.translation;

import com.ibm.engine.detection.DetectionStore;
import com.ibm.mapper.ITranslationProcess;
import com.ibm.mapper.model.INode;
import com.ibm.mapper.reorganizer.IReorganizerRule;
import com.ibm.mapper.reorganizer.Reorganizer;
import com.ibm.mapper.utils.Utils;
import com.ibm.plugin.translation.translator.CTranslator;
import java.util.List;
import javax.annotation.Nonnull;

public final class CTranslationProcess extends ITranslationProcess<Object, Object, Object, Object> {

    public CTranslationProcess(@Nonnull List<IReorganizerRule> reorganizerRules) {
        super(reorganizerRules);
    }

    @Override
    @Nonnull
    public List<INode> initiate(@Nonnull DetectionStore<Object, Object, Object, Object> rootDetectionStore) {
        CTranslator translator = new CTranslator();
        List<INode> translated = translator.translate(rootDetectionStore);
        Utils.printNodeTree("translated ", translated);
        Reorganizer reorganizer = new Reorganizer(reorganizerRules);
        List<INode> reorganized = reorganizer.reorganize(translated);
        Utils.printNodeTree("reorganised", reorganized);
        return reorganized;
    }
}
