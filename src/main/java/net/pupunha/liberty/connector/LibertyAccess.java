package net.pupunha.liberty.connector;

import net.pupunha.liberty.connector.constants.ProfileConstants;
import net.pupunha.liberty.connector.exception.ConfigurationException;
import net.pupunha.liberty.connector.exception.JMXLibertyException;
import net.pupunha.liberty.connector.exception.LibertyAccessException;

import javax.management.ObjectName;
import java.io.File;
import java.util.*;

public class LibertyAccess {

    private JMXLibertyConnector connector;

    public List<Application> getApplications(LibertyConfiguration configuration) throws LibertyAccessException {
        try {
            String jmxLocalAddress = configuration.getJmxLocalAddresssPath();
            connector = new JMXLibertyConnector(jmxLocalAddress);
            connector.connect();
            Set<ObjectName> applicationsObjectName = connector.getApplications();
            List<Application> applications = new ArrayList<>();
            for (ObjectName objectName : applicationsObjectName) {
                applications.add(connector.getApplication(objectName));
            }
            connector.disconnect();
            return applications;
        } catch (ConfigurationException | JMXLibertyException e) {
            throw new LibertyAccessException("Failed to connect on ApplicationMBean", e);
        }
    }

    public Map<String, File> getServersProfile(LibertyConfiguration configuration) throws LibertyAccessException {
        final String servers = configuration.getProfilePath().concat(ProfileConstants.SERVERS);
        File[] listServers = new File(servers).listFiles(pathname -> !pathname.getName().contains("."));
        if (listServers == null) {
            throw new LibertyAccessException("Profile Path '"+ servers +"' not found");
        }
        Map<String, File> map = new LinkedHashMap<>();
        Arrays.stream(listServers).forEach(file -> map.put(file.getName(), file));
        return map;
    }

}
