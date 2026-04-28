package net.typicartist.discordipc.exception;

public class IPCConnectionException extends IPCException {
    public IPCConnectionException(String message) {
        super(message);
    }

    public IPCConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
