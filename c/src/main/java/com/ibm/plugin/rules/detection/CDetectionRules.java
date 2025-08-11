package com.ibm.plugin.rules.detection;

import com.ibm.engine.language.c.CCallNode;
import com.ibm.engine.model.context.CipherContext;
import com.ibm.engine.model.factory.ValueActionFactory;
import com.ibm.engine.rule.IDetectionRule;
import com.ibm.engine.rule.builder.DetectionRuleBuilder;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/** Lists all detection rules available for the C/C++ frontend. */
public final class CDetectionRules {
    private CDetectionRules() {}

    private static final IDetectionRule<CCallNode> AES_RULE =
            new DetectionRuleBuilder<CCallNode>()
                    .createDetectionRule()
                    .forObjectTypes("*")
                    .forMethods(
                            "AES_set_encrypt_key",
                            "AES_encrypt",
                            "AES_cbc_encrypt",
                            "AES_ecb_encrypt",
                            "wc_AesSetKey",
                            "wc_AesEncrypt",
                            "wc_AesCbcEncrypt")
                    .withAnyParameters()
                    .shouldBeDetectedAs(new ValueActionFactory<>("AES"))
                    .buildForContext(new CipherContext(Map.of("kind", "BLOCK_CIPHER")))
                    .inBundle(() -> "C")
                    .withoutDependingDetectionRules();

    @Nonnull
    public static List<IDetectionRule<CCallNode>> rules() {
        return List.of(AES_RULE);
    }
}

