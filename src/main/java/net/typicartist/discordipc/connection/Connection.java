package net.typicartist.discordipc.connection;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

import net.typicartist.discordipc.IPCClient;
import net.typicartist.discordipc.IPCListener;
import net.typicartist.discordipc.data.Packet;
import net.typicartist.discordipc.data.User;
import net.typicartist.discordipc.enums.Command;
import net.typicartist.discordipc.enums.Event;
import net.typicartist.discordipc.enums.Opcode;
import net.typicartist.discordipc.enums.State;

public abstract class Connection implements AutoCloseable {
    public static final int RPC_VERSION = 1;
    public static final int MAX_RPC_FRAME_SIZE = 64 * 1024;

    protected State state = State.Disconnected;
    protected final IPCClient client;
    protected final long clientId;
    protected IPCListener listener;

    protected Connection(IPCClient client, long clientId) {
        this.client = client;
        this.clientId = clientId;
    }

    public static Connection create(IPCClient client, long clientId) {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win")
            ? new WinConnection(client, clientId)
            : new UnixConnection(client, clientId);
    }

    protected abstract void open(int index) throws IOException;
    protected abstract int read(byte[] buf, int offset, int len) throws IOException;
    protected abstract void write(byte[] data) throws IOException;

    public final void connect() throws IOException {
        if (state == State.Connected) return;

        for (int i = 0; i <= 9; i++) {
            try {
                open(i);
                handshake();
                return;
            } catch (IOException e) {
                try { close(); } catch (IOException ignored) {}
            }
        }
        throw new IOException("No Discord IPC pipe available");
    }

    private void handshake() throws IOException {
        try {
            sendPacket(Opcode.Handshake, new JSONObject()
                .put("v", RPC_VERSION)
                .put("client_id", String.valueOf(clientId)));
            
            state = State.SentHandshake;

            Packet response = readPacket();
            JSONObject json = response.getJson();

            if (Command.of(json.optString("cmd")) != Command.DISPATCH
                    || Event.of(json.optString("evt")) != Event.READY) {
                throw new IOException("Unexpected handshake response");
            }

            state = State.Connected;

            if (listener != null) {
                JSONObject user = json.getJSONObject("data").getJSONObject("user");
                listener.onReady(client, User.fromJson(user));
            }
        } catch (JSONException e) {
            throw new IOException("Malformed handshake JSON", e);
        }
    }

    public final void sendPacket(Opcode opcode, JSONObject json) throws IOException {
        try {
            json.put("nonce", IPCClient.getNonce());

            byte[] body = json.toString().getBytes(StandardCharsets.UTF_8);
            if (body.length + 8 > MAX_RPC_FRAME_SIZE)
                throw new IOException("Packet too large: " + (body.length + 8));

            ByteBuffer buf = ByteBuffer.allocate(8 + body.length)
                    .order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(opcode.getValue());
            buf.putInt(body.length);
            buf.put(body);
            write(buf.array());
        } catch (JSONException e) {
            throw new IOException("Malformed packet JSON: ", e);
        }
    }

    public final Packet readPacket() throws IOException {
        try {
            byte[] header = new byte[8];
            readFully(header, 0, 8);

            ByteBuffer buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            Opcode opcode = Opcode.of(buf.getInt());
            int length = buf.getInt();

            if (length < 0 || length > MAX_RPC_FRAME_SIZE)
                throw new IOException("Invalid frame length: " + length);
            
            byte[] body = new byte[length];
            readFully(body, 0, length);
            JSONObject json = new JSONObject(new String(body, StandardCharsets.UTF_8));

            if (opcode == Opcode.Close) {
                state = State.Disconnected;
                throw new EOFException("Discord closed the connection");
            }

            return new Packet(opcode, json);
        } catch (JSONException e) {
            throw new IOException("Malformed packet JSON: ", e);
        }
    }

    private void readFully(byte[] buf, int offset, int length) throws IOException {
        int total = 0;
        while (total < length) {
            int n = read(buf, offset + total, length - total);
            if (n == -1) throw new EOFException("Connection closed");
            total += n;
        }
    }

    @Override
    public void close() throws IOException {
        state = State.Disconnected;
    }

    public boolean isOpen() {
        return state == State.Connected;
    }

    public IPCClient getClient() {
        return client;
    }

    public void setListener(IPCListener listener) {
        this.listener = listener;
    }

    public State getState() {
        return state;
    }
}