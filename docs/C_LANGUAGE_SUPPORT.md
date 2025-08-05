# Building SonarCXX Locally

The C/C++ parser used by the plugin (`sonar-cxx`) is not available on Maven Central. You must build the project locally and install it to your Maven repository before compiling this plugin.

1. Clone the repository:
   ```bash
   git clone https://github.com/SonarOpenCommunity/sonar-cxx.git
   ```
2. Build and install all modules:
   ```bash
   cd sonar-cxx
   mvn clean install -DskipTests
   ```
   This installs the `sonar-cxx-plugin` artifacts into your local Maven repository (`~/.m2/repository`).

3. Copy the generated plugin JAR to your SonarQube instance:
   ```bash
   cp sonar-cxx-plugin/target/sonar-cxx-plugin-*.jar $SONARQUBE_HOME/extensions/plugins/
   ```

# Compiling the Cryptography Plugin with C support

After installing the SonarCXX artifacts, edit the root `pom.xml` to add the `c` module and add the module’s dependency on `org.sonarsource.sonarqube-plugins.cxx:sonar-cxx-plugin`.
Then run:

```bash
mvn clean package
```

# Running on Amazon Linux (EC2)

1. Install Java 17 and Maven using your package manager.
2. Build SonarCXX as described above and copy the plugin JAR to the SonarQube `extensions/plugins` directory.
3. Build this repository and copy the resulting `sonar-cryptography-plugin` JAR to the same directory.
4. Start SonarQube and verify that both plugins appear in **Administration → Marketplace → Installed**.
## Current limitations

The C and C++ support in this plugin is experimental. A minimal translation layer and detection engine exist but only search for WolfSSL function names such as `wc_AesCbcEncrypt` in the source text. This allows the WolfCrypt detection rules in `c/src/main/java/com/ibm/plugin/rules/detection/wolfcrypt` to trigger on simple cases. Complex C code may still be missed and further integration with a real parser is required for reliable CBOM generation.
