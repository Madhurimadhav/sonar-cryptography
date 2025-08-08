package com.ibm.plugin.rules.detection.openssl;

import com.ibm.engine.model.context.CipherContext;
import com.ibm.engine.model.factory.ValueActionFactory;
import com.ibm.engine.rule.IDetectionRule;
import com.ibm.engine.rule.builder.DetectionRuleBuilder;
import java.util.List;
import javax.annotation.Nonnull;

public final class OpenSslRules {
    private OpenSslRules() {}

    private static final String LIB_TYPE = "openssl";

    private static final IDetectionRule<Object> AES_RULE =
            new DetectionRuleBuilder<Object>()
                    .createDetectionRule()
                    .forObjectTypes(LIB_TYPE)
                    .forMethods(
                            "AES_encrypt",
                            "AES_decrypt",
                            "AES_cbc_encrypt",
                            "AES_ecb_encrypt",
                            "AES_ctr128_encrypt",
                            "AES_set_encrypt_key",
                            "AES_set_decrypt_key")
                    .shouldBeDetectedAs(new ValueActionFactory<>("AES"))
                    .withAnyParameters()
                    .buildForContext(new CipherContext())
                    .inBundle(() -> "OpenSSL")
                    .withoutDependingDetectionRules();

    @Nonnull
    public static List<IDetectionRule<Object>> rules() {
        return List.of(AES_RULE);
    }
}
