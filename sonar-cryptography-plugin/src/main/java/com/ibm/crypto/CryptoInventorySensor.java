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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CryptoInventorySensor implements Sensor {
    private static final Logger LOG = Loggers.get(CryptoInventorySensor.class);

    private final Configuration cfg;

    public CryptoInventorySensor(Configuration cfg) {
        this.cfg = cfg;
    }

    private static final class Sig {
        final String vendor;
        final String primitive;
        final Pattern p;

        Sig(String v, String prim, String re) {
            vendor = v;
            primitive = prim;
            p = Pattern.compile(re);
        }
    }

    private static final List<Sig> SIGS = List.of(
            // OpenSSL
            new Sig("OpenSSL", "EVP", "\\bEVP_(?:CipherInit_ex|EncryptInit_ex|DecryptInit_ex|DigestInit_ex)\\b"),
            new Sig("OpenSSL", "AES", "\\bAES_(?:set_(?:encrypt|decrypt)_key|encrypt|decrypt)\\b"),
            new Sig("OpenSSL", "RSA", "\\bRSA_(?:public|private)\\w*\\b"),
            new Sig("OpenSSL", "SHA", "\\bSHA(?:1|224|256|384|512)_Init\\b"),
            // wolfSSL/wolfCrypt
            new Sig("wolfCrypt", "AES",
                    "\\bwc_Aes(?:SetKey|GcmSetKey|CbcEncrypt|CbcDecrypt|GcmEncrypt|GcmDecrypt)\\b"),
            new Sig("wolfCrypt", "RNG", "\\bwc_InitRng|wc_RNG_GenerateBlock\\b"),
            // mbedTLS
            new Sig("mbedTLS", "AES",
                    "\\bmbedtls_aes_(?:init|setkey_enc|setkey_dec|crypt_ecb|crypt_cbc)\\b"),
            new Sig("mbedTLS", "CTR_DRBG", "\\bmbedtls_ctr_drbg_(?:seed|random)\\b"),
            // libsodium
            new Sig("libsodium", "AEAD_CHACHA20_POLY1305", "\\bcrypto_aead_chacha20poly1305_\\w+\\b"),
            new Sig("libsodium", "BOX", "\\bcrypto_box_\\w+\\b"),
            // Botan
            new Sig("Botan", "Cipher", "\\bBotan::Cipher_Mode::create\\b"),
            // Crypto++
            new Sig("Crypto++", "AES", "\\bCryptoPP::AES\\b"));

    private record Hit(String vendor, String primitive, String algo, String path, int line) {
    }

    @Override
    public void describe(SensorDescriptor d) {
        d.name("Cryptography Inventory Sensor (CBOM)").onlyOnLanguages(langs());
    }

    private String[] langs() {
        return cfg.get("crypto.langs").orElse("c,cpp").replace(" ", "").split(",");
    }

    @Override
    public void execute(SensorContext ctx) {
        FileSystem fs = ctx.fileSystem();
        List<Hit> hits = new ArrayList<>();
        Set<String> dedupe = new HashSet<>();

        for (String lang : langs()) {
            FilePredicate pred = fs.predicates().and(fs.predicates().hasLanguage(lang),
                    fs.predicates().hasType(InputFile.Type.MAIN));
            for (InputFile f : fs.inputFiles(pred)) {
                scanFile(ctx, f, hits, dedupe);
            }
        }
        writeCbom(ctx, hits);
        LOG.info("CBOM inventory wrote {} entries.", hits.size());
    }

    private void scanFile(SensorContext ctx, InputFile file, List<Hit> hits, Set<String> dedupe) {
        RuleKey rk = RuleKey.of(CryptoRulesDefinition.repoKeyFor(file.language()),
                CryptoRulesDefinition.RULE_INVENTORY_KEY);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.inputStream(), file.charset()))) {
            String line;
            int n = 0;
            while ((line = br.readLine()) != null) {
                n++;
                for (Sig s : SIGS) {
                    if (s.p.matcher(line).find()) {
                        String algo = guess(line, s.primitive);
                        String key = file.relativePath() + "#" + n + "#" + s.vendor + "#" + algo;
                        if (!dedupe.add(key)) {
                            continue;
                        }

                        NewIssue issue = ctx.newIssue().forRule(rk);
                        NewIssueLocation loc = issue.newLocation().on(file).at(file.selectLine(n)).message(
                                "Crypto usage: %s/%s (%s)".formatted(s.vendor, s.primitive, algo));
                        issue.at(loc).save();

                        hits.add(new Hit(s.vendor, s.primitive, algo, file.relativePath(), n));
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to scan {}: {}", file.relativePath(), e.getMessage());
        }
    }

    private static String guess(String line, String fallback) {
        String L = line.toUpperCase(Locale.ROOT);
        if (L.contains("AES_256") || L.contains("AES256")) {
            return "AES-256";
        }
        if (L.contains("AES_192") || L.contains("AES192")) {
            return "AES-192";
        }
        if (L.contains("AES_128") || L.contains("AES128")) {
            return "AES-128";
        }
        if (L.contains("GCM")) {
            return "AES-GCM";
        }
        if (L.contains("CBC")) {
            return "AES-CBC";
        }
        if (L.contains("CHACHA20")) {
            return "ChaCha20-Poly1305";
        }
        if (L.contains("RSA")) {
            return "RSA";
        }
        if (L.contains("SHA512")) {
            return "SHA-512";
        }
        if (L.contains("SHA384")) {
            return "SHA-384";
        }
        if (L.contains("SHA256")) {
            return "SHA-256";
        }
        if (L.contains("SHA1")) {
            return "SHA-1";
        }
        return fallback;
    }

    private void writeCbom(SensorContext ctx, List<Hit> hits) {
        try {
            JSONArray arr = new JSONArray();
            for (Hit h : hits) {
                arr.put(new JSONObject().put("vendor", h.vendor()).put("primitive", h.primitive())
                        .put("algorithm", h.algo()).put("path", h.path()).put("line", h.line()));
            }
            JSONObject root = new JSONObject().put("schema", "https://example.com/cbom.schema/1.0")
                    .put("generatedBy", "sonar-cryptography").put("entries", arr);

            String subdir = cfg.get("crypto.cbom.subdir").orElse("sonar-cryptography");
            java.nio.file.Path out = ctx.fileSystem().workDir().toPath().resolve(subdir).resolve("cbom.json");
            Files.createDirectories(out.getParent());
            try (Writer w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                w.write(root.toString(2));
            }
            LOG.info("CBOM JSON written to {}", out.toAbsolutePath());
        } catch (Exception e) {
            LOG.error("Failed writing cbom.json: {}", e.toString());
        }
    }
}
