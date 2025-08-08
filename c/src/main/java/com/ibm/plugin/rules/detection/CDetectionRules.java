package com.ibm.plugin.rules.detection;

import com.ibm.engine.rule.IDetectionRule;
import com.ibm.plugin.rules.detection.openssl.OpenSslRules;
import com.ibm.plugin.rules.detection.wolfcrypt.WolfCryptRules;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Lists all detection rules available for the C/C++ frontend.
 */
public final class CDetectionRules {
    private CDetectionRules() {}

    @Nonnull
    public static List<IDetectionRule<Object>> rules() {
        List<IDetectionRule<Object>> rules = new ArrayList<>();
        rules.addAll(WolfCryptRules.rules());
        rules.addAll(OpenSslRules.rules());
        return rules;
    }
}
