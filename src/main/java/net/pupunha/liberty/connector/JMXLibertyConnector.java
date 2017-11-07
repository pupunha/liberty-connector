package net.pupunha.liberty.connector;

import net.pupunha.liberty.connector.Application.Operation;
import net.pupunha.liberty.connector.cli.Main;
import net.pupunha.liberty.connector.exception.JMXLibertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.Set;

import static net.pupunha.liberty.connector.constants.MBeanConstants.*;
import static net.pupunha.liberty.connector.exception.ErrorCode.*;

public class JMXLibertyConnector {

    private Logger log = LoggerFactory.getLogger(JMXLibertyConnector.class);

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
            ObjectName applicationsMBeanName = new ObjectName(MBEAN_APPLICATIONS);
            return mbean.queryNames(applicationsMBeanName, null);
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ MBEAN_APPLICATIONS, e);
        }
    }

    public Application getApplication(ObjectName appObjectName) throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            if (!mbean.isRegistered(appObjectName)) {
                throw new JMXLibertyException("MBean invoke request failed " +
                        appObjectName.getCanonicalName() + " is not registered.");
            }
            Application application = createApplication(appObjectName, mbean);
            return application;
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ appObjectName.getCanonicalName(), e);
        }
    }

    private Application createApplication(ObjectName appObjectName, MBeanServerConnection mbean) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        String name = appObjectName.getKeyProperty(NAME);
        String state = mbean.getAttribute(appObjectName, STATE).toString();
        String pid = mbean.getAttribute(appObjectName, PID).toString();
        return new Application(pid, name, state, appObjectName);
    }

    public Application getApplication(String name) throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            ObjectName application = new ObjectName(MBEAN_APPLICATION);
            QueryExp queryExp = Query.eq(Query.attr(NAME), Query.value(name));
            Set<ObjectName> objectNames = mbean.queryNames(application, queryExp);
            if (objectNames.size() > 0) {
                Optional<ObjectName> result = objectNames.stream().findFirst();
                if (result.isPresent()) {
                    return createApplication(result.get(), mbean);
                }
            }
            return null;
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to connect on MBean "+ name, e);
        }
    }

    public void invokeOperationApplication(Application application, Operation operation) throws JMXLibertyException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            if (!mbean.isRegistered(application.getObjectName())) {
                throw new JMXLibertyException("MBean invoke request failed " +
                        application.getObjectName().getCanonicalName() + " is not registered.");
            }
            mbean.invoke(application.getObjectName(), operation.getName(), null, null);
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to invoke operation " +
                    operation.getName() +" on MBean "+ application.getObjectName().getCanonicalName(), e);
        }
    }

    public JMXConnector getJmxConnector() {
        return jmxConnector;
    }

    public void addNotification(Application application, NotificationListener listener) throws JMXLibertyException {
        try {
            AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
            filter.enableAttribute(STATE);

            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            mbean.addNotificationListener(application.getObjectName(), listener, filter, null);
        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to add notification", e);
        }
    }

    public void removeNotification(Application application, NotificationListener listener) throws JMXLibertyException {
        try {
            AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
            filter.enableAttribute(STATE);

            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            mbean.removeNotificationListener(application.getObjectName(), listener);

        } catch (IOException e) {
            throw new JMXLibertyException(CONNECTION_NOT_ESTABLISHED, e);
        } catch (Exception e) {
            throw new JMXLibertyException("Failed to remove notification", e);
        }
    }

}
