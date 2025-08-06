package com.ibm.plugin.rules;

import com.ibm.plugin.rules.detection.CBaseDetectionRule;
import com.ibm.plugin.rules.detection.CDetectionRules;
import java.util.Collections;
import org.sonar.check.Rule;

@Rule(key = "Inventory")
public class CInventoryRule extends CBaseDetectionRule {
    public CInventoryRule() {
        super(true, CDetectionRules.rules(), Collections.emptyList());
    }
}
