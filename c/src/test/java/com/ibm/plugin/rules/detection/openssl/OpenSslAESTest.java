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
package com.ibm.plugin.rules.detection.openssl;

import static org.assertj.core.api.Assertions.assertThat;

import com.ibm.mapper.model.INode;
import com.ibm.mapper.model.algorithms.AES;
import com.ibm.plugin.CAggregator;
import com.ibm.plugin.rules.CInventoryRule;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

class OpenSslAESTest {

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        CAggregator.reset();
    }

    @Test
    void detectsAesUsageAsAsset() {
        String code = "void test(){ AES_encrypt(0,0,0); }";

        InputFile inputFile =
                TestInputFileBuilder.create("moduleKey", "test.c")
                        .setModuleBaseDir(tempDir)
                        .setLanguage("c")
                        .setContents(code)
                        .build();

        CInventoryRule rule = new CInventoryRule();
        rule.scanFile(code, inputFile);

        List<INode> nodes = CAggregator.getDetectedNodes();
        assertThat(nodes.stream().anyMatch(n -> n instanceof AES)).isTrue();
    }
}
