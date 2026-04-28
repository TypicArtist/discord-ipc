package net.typicartist.discordipc.enums;

public enum Opcode {
    Handshake(0), 
    Frame(1), 
    Close(2), 
    Ping(3), 
    Pong(4);

    private final int value;

    Opcode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Opcode of(int value) {
        for (Opcode op : values()) {
            if (op.value == value) return op;
        }
        throw new IllegalArgumentException("Unknown opcode: " + value);
    }
}