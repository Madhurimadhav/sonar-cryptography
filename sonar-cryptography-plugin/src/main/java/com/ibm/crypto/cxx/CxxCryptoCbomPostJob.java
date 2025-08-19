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

import com.ibm.crypto.CbomAsset;
import com.ibm.crypto.CryptoInventoryWriter;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.PostJobContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CxxCryptoCbomPostJob implements PostJob {

  private static final Logger LOG = Loggers.get(CxxCryptoCbomPostJob.class);
  private final FileSystem fs;

  public CxxCryptoCbomPostJob(FileSystem fs) { this.fs = fs; }

  @Override
  public void execute(PostJobContext ctx) {
    Set<CxxCryptoInventoryStore.Entry> entries = CxxCryptoInventoryStore.snapshotAndClear();
    if (entries.isEmpty()) {
      LOG.info("CBOM: no crypto assets detected for C/C++.");
      return;
    }

    List<CbomAsset> assets = new ArrayList<>();
    for (CxxCryptoInventoryStore.Entry e : entries) {
      assets.add(new CbomAsset(e.library, e.algorithm, e.operation, e.file, null, e.function));
    }

    CryptoInventoryWriter.writeCbomJson(fs.workDir().toPath(), assets);
    LOG.info("CBOM: wrote {} assets", assets.size());
  }
}
