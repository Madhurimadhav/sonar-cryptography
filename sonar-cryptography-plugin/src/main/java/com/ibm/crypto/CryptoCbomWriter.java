package com.ibm.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CryptoCbomWriter {
  public void writeCbomJson(File out, List<CryptoInventoryCollector.Entry> entries) throws IOException {
    ObjectNode root = JsonNodeFactory.instance.objectNode();
    root.put("version", "1.0");
    ArrayNode assets = root.putArray("assets");
    for (CryptoInventoryCollector.Entry e : entries) {
      ObjectNode a = assets.addObject();
      a.put("file", e.file);
      a.put("function", e.function);
      a.put("library", e.library);
      a.put("primitive", e.primitive);
      a.put("purpose", e.purpose);
      if (e.line != null && e.line > 0) {
        a.put("line", e.line);
      }
    }
    try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
      new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(w, root);
    }
  }
}
