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
package com.ibm.crypto.cxx;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.rule.Severity;

public class CxxRulesDefinition implements RulesDefinition {
  public static final String REPO_KEY = "sonar-c-crypto";     // keep consistent with your C repo if desired
  public static final String REPO_NAME = "Custom C/C++ Crypto Rules";
  public static final String INVENTORY_KEY = "Inventory";      // reuse your existing key

  @Override
  public void define(Context ctx) {
    NewRepository repoC   = ctx.createRepository(REPO_KEY, "c").setName(REPO_NAME);
    NewRepository repoCpp = ctx.createRepository(REPO_KEY, "c++").setName(REPO_NAME);

    defineInventory(repoC);
    defineInventory(repoCpp);

    repoC.done(); repoCpp.done();
  }

  private static void defineInventory(NewRepository repo) {
    repo.createRule(INVENTORY_KEY)
        .setName("Cryptographic Inventory (CBOM)")
        .setHtmlDescription("Detects cryptographic asset usage and inventories it into CBOM.")
        .setSeverity(Severity.MAJOR)
        .setTags("cryptography", "cbom", "cwe");
  }
}
