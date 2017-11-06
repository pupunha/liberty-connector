package net.pupunha.liberty.connector;

import javax.management.ObjectName;

public class Application {

    private String pid;
    private String name;
    private String state;
    private ObjectName objectName;

    public Application(String pid, String name, String state, ObjectName objectName) {
        this.pid = pid;
        this.name = name;
        this.state = state;
        this.objectName = objectName;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    @Override
    public String toString() {
        return "Application {" +
                "pid='" + pid + '\'' +
                ", name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", objectName=" + objectName +
                '}';
    }
}
