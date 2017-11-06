package net.pupunha.liberty.connector.exception;

public enum ErrorCode {

    URL_NOT_FOUND("001", "URL not found"),
    CONNECTION_NOT_ESTABLISHED("002", "Connection not established"),
    CONNECTION_ALREADY_CLOSED("003", "Connection already closed"),
    ;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + " - " + description;
    }
}
