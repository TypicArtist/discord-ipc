package net.typicartist.discordipc.enums;

public enum Event {
    READY(false),
    ERROR(false),
    ACTIVITY_JOIN(true),
    ACTIVITY_SPECTATE(true),
    ACTIVITY_JOIN_REQUEST(true),
    UNKNOWN(false);

    private final boolean subscribable;

    Event(boolean subscribable) {
        this.subscribable = subscribable;
    }

    public boolean isSubscribable() {
        return subscribable;
    }

    public static Event of(String value) {
        for (Event e : values()) {
            if (e != UNKNOWN && e.name().equalsIgnoreCase(value)) {
                return e;
            }
        }
        return UNKNOWN;
    }
}