/*
 * Sonar Cryptography Plugin
 * Copyright (C) 2024 PQCA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.crypto;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;

public class CryptoRulesDefinition implements RulesDefinition {
    public static final String BASE_REPO_KEY = "sonar-c-crypto";
    public static final String RULE_INVENTORY_KEY = "CBOM_INVENTORY";

    static String repoKeyFor(String lang) {
        return BASE_REPO_KEY + "-" + lang;
    }

    @Override
    public void define(Context context) {
        String[] langs = System.getProperty("crypto.langs", "c,cpp").split(",");
        for (String raw : langs) {
            String lang = raw.trim();
            NewRepository repo = context.createRepository(repoKeyFor(lang), lang)
                    .setName("Custom C/C++ Crypto Rules (" + lang + ")");

            repo.createRule(RULE_INVENTORY_KEY)
                    .setName("Cryptographic Inventory (CBOM)")
                    .setHtmlDescription("Detects cryptographic APIs and inventories them to cbom.json.")
                    .setTags("cryptography", "cbom", "inventory");

            repo.done();
        }
    }
}
