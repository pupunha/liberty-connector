package net.pupunha.liberty.connector;

import net.pupunha.liberty.connector.exception.JMXLibertyException;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import static net.pupunha.liberty.connector.constants.MBeanConstants.*;
import static net.pupunha.liberty.connector.exception.ErrorCode.*;

public class JMXLibertyConnector {

    private JMXConnector jmxConnector;
    private String url;

    public JMXLibertyConnector(String url) {
        this.url = url;
    }

    public void connect() throws JMXLibertyException {
        try {
            JMXServiceURL serviceUrl = new JMXServiceURL(url);
            jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
        } catch (MalformedURLException e) {
            throw new JMXLibertyException(URL_NOT_FOUND, e);
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        }
    }

    public void disconnect() {
        if (jmxConnector != null) {
            try {
                jmxConnector.close();
            } catch (IOException e) {
                System.err.println(CONNECTION_ALREADY_CLOSED.getDescription());
            }
        }
    }

    public boolean isConnected() throws JMXLibertyException {
        try {
            String id = jmxConnector.getConnectionId();
            if (id != null) {
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public Set<ObjectName> getApplications() throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            ObjectName applicationsMonitorMBeanName = new ObjectName(MBEAN_APPLICATIONS);
            return mbean.queryNames(applicationsMonitorMBeanName, null);
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ MBEAN_APPLICATIONS, e);
        }
    }

    public Application getApplication(ObjectName appMonitorObjectName) throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            if (!mbean.isRegistered(appMonitorObjectName)) {
                throw new JMXLibertyException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
            }
            String name = appMonitorObjectName.getKeyProperty(NAME);
            String state = mbean.getAttribute(appMonitorObjectName, STATE).toString();
            String pid = mbean.getAttribute(appMonitorObjectName, PID).toString();
            Application application = new Application(pid, name, state, appMonitorObjectName);
            return application;
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ appMonitorObjectName.getCanonicalName(), e);
        }
    }

    public JMXConnector getJmxConnector() {
        return jmxConnector;
    }
}
