package net.typicartist.discordipc.exception;

public class IPCProtocolException extends IPCException {
    public IPCProtocolException(String message) {
        super(message);
    }

    public IPCProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
