package net.pupunha.liberty.connector;

import net.pupunha.liberty.connector.exception.JMXLibertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

public class ApplicationStateNotificationLogListener implements NotificationListener {

    private JMXLibertyConnector connector;

    public ApplicationStateNotificationLogListener(JMXLibertyConnector connector) {
        this.connector = connector;
    }

    private Logger log = LoggerFactory.getLogger(JMXLibertyConnector.class);

    public void handleNotification(Notification notification, Object obj) {
        if(notification instanceof AttributeChangeNotification) {
            try {
                if (!connector.isConnected()) {
                    connector.connect();
                }
                AttributeChangeNotification attributeChange =
                        (AttributeChangeNotification) notification;

                Application application = (Application) obj;

                log.info("{} - {} - {} - {}", application.getPid(), application.getName(), attributeChange.getAttributeName(), attributeChange.getNewValue());
            } catch (JMXLibertyException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
