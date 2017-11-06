package net.pupunha.liberty.connector;

import net.pupunha.liberty.connector.constants.ProfileConstants;
import net.pupunha.liberty.connector.exception.ConfigurationException;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class LibertyConfiguration {

    public static final String CONFIGURATION_PROPERTIES = "configuration.properties";
    private static final String PROFILE_PATH = "profile.path";

    private Properties properties;

    private String profilePath;
    private String profileUse;

    public LibertyConfiguration() {
        properties = new Properties();
//        try {
//            URL url = ClassLoader.getSystemResource(CONFIGURATION_PROPERTIES);
////            if (!Files.exists(Paths.get(url.getFile()))) {
////
////            }
//            load(new FileInputStream(url.getFile()));
//            setProfilePath(getProperty(PROFILE_PATH));
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("Configuracao Padrao");
//        }
    }

    public boolean existFile() {
        return Files.exists(Paths.get(getConfigurationFile()));
    }

    public String getConfigurationFile() {
        URL fullPath = getClass().getProtectionDomain().getCodeSource().getLocation();
        File location = new File(fullPath.getFile());
        location = location.getParentFile();
        return location.getPath() + "/" + CONFIGURATION_PROPERTIES;
    }

    public void save() {
        try {
            properties.store(new FileOutputStream(getConfigurationFile()), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            properties.load(new FileInputStream(getConfigurationFile()));
            setProfilePath(properties.getProperty(PROFILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProfilePath() {
        String profile = properties.getProperty(PROFILE_PATH);
        if (profile != null) {
            return profile;
        }
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        properties.setProperty(PROFILE_PATH, profilePath);
    }

    public String getProfileUse() {
        return profileUse;
    }

    public void setProfileUse(String profileUse) {
        this.profileUse = profileUse;
    }

    private String getProfileFullPath() {
        return getProfilePath() + ProfileConstants.SERVERS + "/" + getProfileUse();
    }

    public String getJmxLocalAddresssPath() throws ConfigurationException {
        final String path = getProfileFullPath() + ProfileConstants.JMX_LOCAL_ADDRESS_PATH;
        try {
            if(!Files.exists(Paths.get(path))) {
                throw new ConfigurationException("The file '" + path + " was not found");
            }
            List<String> lines = Files.readAllLines(Paths.get(path));
            return lines.get(0);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read path: " + path);
        }
    }
}
