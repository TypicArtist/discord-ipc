package net.typicartist.discordipc.exceptions;

import java.io.IOException;

public class aException extends IOException {
    public aException() {
    }

    public aException(String message) {
        super(message);
    }

    public aException(String message, Throwable cause) {
        super(message, cause);
    }

    public aException(Throwable cause) {
        super(cause);
    }
}
