package net.typicartist.discordipc.enums;

public enum Command {
    DISPATCH,
    SUBSCRIBE,
    UNSUBSCRIBE,
    SET_ACTIVITY,
    SEND_ACTIVITY_JOIN_INVITE,
    CLOSE_ACTIVITY_REQUEST,
    UNKNOWN;

    public static Command of(String value) {
        for (Command e : values()) {
            if (e != UNKNOWN && e.name().equalsIgnoreCase(value)) {
                return e;
            }
        }
        return UNKNOWN;
    }
}
