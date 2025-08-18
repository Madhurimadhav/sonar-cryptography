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
package com.ibm.crypto.catalog;

import java.util.*;

public final class CryptoFunctionCatalog {

  public static final class Info {
    public final String algorithm;   // e.g., "AES", "RSA", "SHA-256"
    public final String operation;   // e.g., "encrypt", "decrypt", "hash", "sign", "keygen"
    public final String library;     // e.g., "OpenSSL", "mbedTLS", "wolfCrypt"
    public Info(String alg, String op, String lib) {
      this.algorithm = alg; this.operation = op; this.library = lib;
    }
  }

  private static final Map<String, Info> MAP = new HashMap<>();

  static {
    // OpenSSL
    MAP.put("EVP_EncryptInit_ex",  new Info("AES", "encrypt", "OpenSSL"));
    MAP.put("EVP_DecryptInit_ex",  new Info("AES", "decrypt", "OpenSSL"));
    MAP.put("EVP_DigestInit_ex",   new Info("SHA-2", "hash",   "OpenSSL"));
    MAP.put("RSA_generate_key_ex", new Info("RSA",  "keygen",  "OpenSSL"));

    // mbedTLS
    MAP.put("mbedtls_aes_setkey_enc", new Info("AES", "encrypt", "mbedTLS"));
    MAP.put("mbedtls_aes_setkey_dec", new Info("AES", "decrypt", "mbedTLS"));
    MAP.put("mbedtls_md_setup",       new Info("SHA-2", "hash",  "mbedTLS"));

    // wolfCrypt (wolfSSL)
    MAP.put("wc_AesSetKey",          new Info("AES", "encrypt", "wolfCrypt"));
    MAP.put("wc_RsaPublicEncrypt",   new Info("RSA", "encrypt", "wolfCrypt"));
    MAP.put("wc_RsaPrivateDecrypt",  new Info("RSA", "decrypt", "wolfCrypt"));
  }

  public static Optional<Info> lookup(String functionName) {
    return Optional.ofNullable(MAP.get(functionName));
  }
}
