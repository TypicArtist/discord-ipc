package net.typicartist.discordipc.exception;

public class IPCNotConnectedException extends IPCException {
    public IPCNotConnectedException(long clientId) {
        super("IPCClient (ID: " + clientId + ") is not connected.");
    }
}
