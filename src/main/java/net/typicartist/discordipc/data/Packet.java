package net.typicartist.discordipc.data;

import org.json.JSONObject;

import net.typicartist.discordipc.enums.Opcode;

public class Packet {
    private final Opcode op;
    private final JSONObject json;

    public Packet(Opcode op, JSONObject json) {
        this.op = op;
        this.json = json;
    }

    public Opcode getOp() {
        return op;
    }

    public JSONObject getJson() {
        return json;
    }
}