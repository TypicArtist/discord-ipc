package net.typicartist.discordipc.enums;

public enum Opcode {
    Handshake, Frame, Close, Ping, Pong;

    public static Opcode of(int code) {
        return values()[code];
    }
}