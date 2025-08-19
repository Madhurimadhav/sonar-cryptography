package com.ibm.crypto.cxx;

import com.ibm.crypto.CryptoCbomWriter;
import com.ibm.crypto.CryptoInventoryCollector;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.scanner.protocol.output.ScannerReport;
import org.sonar.scanner.protocol.output.ScannerReportReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CxxCryptoHarvestPostJob implements PostJob {

  private static final Logger LOG = Loggers.get(CxxCryptoHarvestPostJob.class);

  private final CryptoInventoryCollector collector;
  private final CryptoCbomWriter cbomWriter;

  public CxxCryptoHarvestPostJob(CryptoInventoryCollector collector, CryptoCbomWriter cbomWriter) {
    this.collector = collector;
    this.cbomWriter = cbomWriter;
  }

  @Override
  public void describe(PostJobDescriptor descriptor) {
    descriptor.name("C++ Crypto Inventory Harvester").requirePlugins("cxx");
  }

  @Override
  public void execute(PostJobContext context) {
    File workDir = context.scannerContext().getWorkingDirectory();
    File reportDir = new File(workDir, "scanner-report");
    if (!reportDir.isDirectory()) {
      LOG.debug("No scanner-report dir found; skipping C++ crypto harvest");
      return;
    }

    try (ScannerReportReader reader = new ScannerReportReader(reportDir)) {
      ScannerReport.ComponentsReader components = reader.readComponents();
      ScannerReport.Component component;
      while ((component = components.read()) != null) {
        if (component.getType() != ScannerReport.Component.ComponentType.FILE) {
          continue;
        }
        int fileRef = component.getRef();
        String filePath = component.getProjectRelativePath();

        Map<Integer, String> symbolNameByRef = new HashMap<>();
        ScannerReport.SymbolsReader symbolsReader = reader.readSymbols(fileRef);
        ScannerReport.Symbol symbol;
        while ((symbol = symbolsReader.read()) != null) {
          if (symbol.getType() == ScannerReport.Symbol.SymbolType.FUNCTION) {
            symbolNameByRef.put(symbol.getRef(), symbol.getName());
          }
        }

        ScannerReport.ReferencesReader refsReader = reader.readReferences(fileRef);
        ScannerReport.Reference ref;
        while ((ref = refsReader.read()) != null) {
          if (ref.getType() != ScannerReport.Reference.ReferenceType.CALL) {
            continue;
          }
          String calledName = symbolNameByRef.get(ref.getSymbolRef());
          if (calledName == null) {
            continue;
          }
          CryptoFunctionRegistry.CryptoFn meta = CryptoFunctionRegistry.MAP.get(calledName);
          if (meta == null) {
            continue;
          }
          int line = ref.hasTextRange() ? ref.getTextRange().getStartLine() : -1;
          collector.recordCryptoUse(filePath, calledName, meta.lib, meta.primitive, meta.purpose, line);
        }
      }

      cbomWriter.writeCbomJson(new File(workDir, "cbom.json"), collector.snapshot());
    } catch (Exception e) {
      LOG.error("C++ crypto harvest failed", e);
    }
  }
}
