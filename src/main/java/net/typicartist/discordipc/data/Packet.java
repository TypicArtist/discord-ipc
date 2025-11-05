package net.typicartist.discordipc.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import net.typicartist.discordipc.enums.Opcode;

public class Packet {
    private final Opcode op;
    private final JSONObject data;

    public Packet(Opcode op, JSONObject data) {
        this.op = op;
        this.data = data;
    }

    public Opcode getOp() {
        return op;
    }

    public JSONObject getJson() {
        return data;
    }

    public byte[] toBytes() {
        byte[] d = data.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES + d.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(op.ordinal());
        buffer.putInt(d.length);
        buffer.put(d);

        return buffer.array();
    }
}