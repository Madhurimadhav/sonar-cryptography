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

import com.ibm.crypto.catalog.CryptoFunctionCatalog;
import com.ibm.crypto.catalog.CryptoFunctionCatalog.Info;

import com.sonar.cxx.sslr.api.AstNode;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.sonar.cxx.checks.AbstractCxxCheck;
import com.sonar.cxx.parser.CxxGrammarImpl;

import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.fs.InputFile;

import java.util.Optional;

public class CxxCryptoInventoryCheck extends AbstractCxxCheck {

  private static final Logger LOG = Loggers.get(CxxCryptoInventoryCheck.class);
  public static final RuleKey RULE = RuleKey.of(CxxRulesDefinition.REPO_KEY, CxxRulesDefinition.INVENTORY_KEY);

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.CALL_EXPRESSION);
  }

  @Override
  public void visitNode(AstNode node) {
    // Extract unqualified callee identifier from the CALL_EXPRESSION
    String callee = extractCalleeIdentifier(node);
    if (callee == null || callee.isEmpty()) return;

    Optional<Info> infoOpt = CryptoFunctionCatalog.lookup(callee);
    if (infoOpt.isEmpty()) return;

    Info info = infoOpt.get();

    // Raise an issue instance at callsite under Inventory rule
    InputFile file = getContext().getInputFile(node);
    if (file != null) {
      int line = Math.max(1, node.getTokenLine());
      int col  = Math.max(1, node.getToken().getColumn());
      NewIssue issue = getSensorContext().newIssue().forRule(RULE);
      NewIssueLocation loc = issue.newLocation()
          .on(file)
          .at(file.newRange(line, col, line, Math.max(col + 1, 1)))
          .message(String.format("Crypto asset: %s %s (%s) via %s()",
              info.algorithm, info.operation, info.library, callee));
      issue.at(loc).save();
    }

    // Accumulate for CBOM output later
    CxxCryptoInventoryStore.add(
        file == null ? null : file.uri().getPath(),
        callee, info.algorithm, info.operation, info.library);
  }

  /**
   * Extracts the identifier used as the callee of this CALL_EXPRESSION.
   * Navigates children (e.g., POSTFIX_EXPRESSION -> PRIMARY_EXPRESSION -> id-expression)
   * without regex; returns the terminal IDENTIFIER token value.
   */
  private static String extractCalleeIdentifier(AstNode callExpr) {
    // Typical shape: CALL_EXPRESSION -> POSTFIX_EXPRESSION '(' argument-list? ')'
    AstNode post = callExpr.getFirstChild(CxxGrammarImpl.POSTFIX_EXPRESSION);
    if (post == null) return tokenIfIdentifier(callExpr); // fallback

    // Peel off nested postfix/primary constructs until we hit an identifier-like token
    AstNode cursor = post;
    while (cursor != null) {
      if (cursor.getToken() != null && cursor.getToken().isIdentifier()) {
        return cursor.getToken().getValue();
      }
      AstNode idExpr = cursor.getFirstChild(CxxGrammarImpl.ID_EXPRESSION);
      if (idExpr != null && idExpr.getToken() != null && idExpr.getToken().isIdentifier()) {
        return idExpr.getToken().getValue();
      }
      AstNode prim = cursor.getFirstChild(CxxGrammarImpl.PRIMARY_EXPRESSION);
      if (prim != null && prim.getToken() != null && prim.getToken().isIdentifier()) {
        return prim.getToken().getValue();
      }
      // dive to the leftmost child if none of the above
      cursor = cursor.getFirstChild();
    }
    return tokenIfIdentifier(callExpr);
  }

  private static String tokenIfIdentifier(AstNode n) {
    return (n.getToken() != null && n.getToken().isIdentifier())
        ? n.getToken().getValue() : null;
  }
}
