package com.ibm.crypto;

import java.util.ArrayList;
import java.util.List;

public class CryptoInventoryCollector {
  public static final class Entry {
    public final String file;
    public final String function;
    public final String library;
    public final String primitive;
    public final String purpose;
    public final Integer line;

    public Entry(String file, String function, String library, String primitive, String purpose, Integer line) {
      this.file = file;
      this.function = function;
      this.library = library;
      this.primitive = primitive;
      this.purpose = purpose;
      this.line = line;
    }
  }

  private final List<Entry> entries = new ArrayList<>();

  public void recordCryptoUse(String file, String function, String lib, String primitive, String purpose, Integer line) {
    entries.add(new Entry(file, function, lib, primitive, purpose, line));
  }

  public List<Entry> snapshot() {
    return List.copyOf(entries);
  }
}
