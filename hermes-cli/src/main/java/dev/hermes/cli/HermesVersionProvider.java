package dev.hermes.cli;

import java.io.InputStream;
import java.util.Properties;

import picocli.CommandLine.IVersionProvider;

public final class HermesVersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        Properties properties = new Properties();
        try (InputStream in =
                     HermesVersionProvider.class.getResourceAsStream("/hermes-cli-version.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }
        String version = properties.getProperty("version", "0.1.0-SNAPSHOT");
        return new String[]{"hermes " + version};
    }
}
