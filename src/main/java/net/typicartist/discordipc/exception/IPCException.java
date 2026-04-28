package net.typicartist.discordipc.exception;

public class IPCException extends RuntimeException {
    public IPCException(String message) {
        super(message);
    }

    public IPCException(String message, Throwable cause) {
        super(message, cause);
    }
}
