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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class CxxCryptoInventoryStore {
  private static final Set<Entry> SET = ConcurrentHashMap.newKeySet();

  static void add(String file, String function, String algorithm, String operation, String library) {
    SET.add(new Entry(file, function, algorithm, operation, library));
  }

  static Set<Entry> snapshotAndClear() {
    Set<Entry> snap = new LinkedHashSet<>(SET);
    SET.clear();
    return snap;
  }

  static final class Entry {
    final String file, function, algorithm, operation, library;
    Entry(String f, String fn, String alg, String op, String lib) {
      this.file=f; this.function=fn; this.algorithm=alg; this.operation=op; this.library=lib;
    }
    @Override public int hashCode() { return Objects.hash(file, function, algorithm, operation, library); }
    @Override public boolean equals(Object o) {
      if (!(o instanceof Entry e)) return false;
      return Objects.equals(file,e.file)&&Objects.equals(function,e.function)
          &&Objects.equals(algorithm,e.algorithm)&&Objects.equals(operation,e.operation)
          &&Objects.equals(library,e.library);
    }
  }

  private CxxCryptoInventoryStore() {}
}
