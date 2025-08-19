package com.ibm.crypto;

import com.ibm.crypto.catalog.CryptoFunctionCatalog;

/**
 * Simple record representing one cryptographic asset detected during analysis.
 */
public record CbomAsset(
    String provider,
    String algorithm,
    String mode,
    String file,
    Integer line,
    String function
) {
    public static CbomAsset from(CryptoFunctionCatalog.Info info, String file, int line, String function) {
        return new CbomAsset(info.library, info.algorithm, info.operation, file, line, function);
    }
}
