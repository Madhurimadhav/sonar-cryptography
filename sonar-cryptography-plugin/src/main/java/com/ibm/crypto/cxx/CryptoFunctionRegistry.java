package com.ibm.crypto.cxx;

import java.util.Map;

public final class CryptoFunctionRegistry {
  public static final class CryptoFn {
    public final String lib;
    public final String primitive;
    public final String purpose;
    public CryptoFn(String lib, String primitive, String purpose) {
      this.lib = lib;
      this.primitive = primitive;
      this.purpose = purpose;
    }
  }

  // Exact, case-sensitive names
  public static final Map<String, CryptoFn> MAP = Map.ofEntries(
      // wolfSSL (wolfCrypt)
      Map.entry("wc_AesSetKey", new CryptoFn("wolfssl", "AES", "key_setup")),
      Map.entry("wc_AesCbcEncrypt", new CryptoFn("wolfssl", "AES-CBC", "encrypt")),
      Map.entry("wc_AesCbcDecrypt", new CryptoFn("wolfssl", "AES-CBC", "decrypt")),
      Map.entry("wc_Sha256Update", new CryptoFn("wolfssl", "SHA-256", "hash")),
      Map.entry("wc_RsaPublicEncrypt", new CryptoFn("wolfssl", "RSA", "encrypt")),
      Map.entry("wc_RsaPrivateDecrypt", new CryptoFn("wolfssl", "RSA", "decrypt")),
      // OpenSSL EVP
      Map.entry("EVP_EncryptInit_ex", new CryptoFn("openssl", "EVP", "encrypt_init")),
      Map.entry("EVP_EncryptUpdate", new CryptoFn("openssl", "EVP", "encrypt")),
      Map.entry("EVP_EncryptFinal_ex", new CryptoFn("openssl", "EVP", "encrypt_final")),
      Map.entry("EVP_DigestInit_ex", new CryptoFn("openssl", "EVP-Digest", "hash_init")),
      // mbedTLS
      Map.entry("mbedtls_aes_setkey_enc", new CryptoFn("mbedtls", "AES", "key_setup")),
      Map.entry("mbedtls_aes_crypt_cbc", new CryptoFn("mbedtls", "AES-CBC", "encrypt_decrypt"))
  );

  private CryptoFunctionRegistry() {}
}
