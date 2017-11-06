package net.pupunha.liberty.connector.constants;

public final class MBeanConstants {

    /** ApplicationMBean name */
    public final static String MBEAN_APPLICATIONS = "WebSphere:service=com.ibm.websphere.application.ApplicationMBean,name=*";

    /** ApplicationMBean name attributes */
    public static final String PID = "Pid";
    public static final String NAME = "name";
    public static final String STATE = "State";

    public static final Object[] ATTRIBUTES_APPLICATIONS = {
            PID.toUpperCase(),
            NAME.toUpperCase(),
            STATE.toUpperCase()
    };


}
