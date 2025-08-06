package com.ibm.plugin.rules.detection;

import com.ibm.engine.rule.IDetectionRule;
import com.ibm.plugin.rules.detection.wolfcrypt.WolfCryptRules;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Lists all detection rules available for the C/C++ frontend.
 */
public final class CDetectionRules {
    private CDetectionRules() {}

    @Nonnull
    public static List<IDetectionRule<Object>> rules() {
        return WolfCryptRules.rules();
    }
}
