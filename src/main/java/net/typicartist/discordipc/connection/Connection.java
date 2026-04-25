package net.typicartist.discordipc.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import net.typicartist.discordipc.IPCClient;
import net.typicartist.discordipc.IPCListener;
import net.typicartist.discordipc.data.Callback;
import net.typicartist.discordipc.data.Packet;
import net.typicartist.discordipc.data.User;
import net.typicartist.discordipc.enums.Command;
import net.typicartist.discordipc.enums.DiscordBuild;
import net.typicartist.discordipc.enums.Event;
import net.typicartist.discordipc.enums.Opcode;
import net.typicartist.discordipc.enums.State;

public abstract class Connection implements AutoCloseable {
    public static final int RPC_VERSION = 1;
    public static final int MAX_RPC_FRAME_SIZE = 64 * 1024;

    protected Channel channel;
    protected State state = State.Disconnected;

    protected final IPCClient client;
    protected final long clientId;
    protected IPCListener listener;
    protected DiscordBuild build;
    protected Map<String, Callback> callbacks = new ConcurrentHashMap<>();

    public static Connection create(IPCClient client, long clientId) {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win")
            ? new WinConnection(client, clientId)
            : new UnixConnection(client, clientId);
    }

    public Connection(IPCClient client, long clientId) {
        this.client = client;
        this.clientId = clientId;
    }

    public abstract boolean openChannel(int index);

    public void open(DiscordBuild ...preferredOrder) throws IOException, JSONException, InterruptedException {
        if (state == State.Connected) return;

        if (preferredOrder == null || preferredOrder.length == 0)
            preferredOrder = new DiscordBuild[]{DiscordBuild.ANY};

        for (int i = 0; i <= 9; i++) {
            if (!openChannel(i)) continue;

            JSONObject handshake = new JSONObject()
                    .put("v", RPC_VERSION)
                    .put("client_id", String.valueOf(clientId));
            
            if (!sendPacket(new Packet(Opcode.Handshake, handshake))) {
                close();
                continue;
            }

            state = State.SentHandshake;

            Packet packet;
            try {
                packet = readPacket();
            } catch (IOException | InterruptedException e) {
                close();
                continue;
            }

            JSONObject json = packet.getJson();
            Command cmd = Command.of(json.optString("cmd"));
            Event evt = Event.of(json.optString("evt"));

            if (cmd == Command.DISPATCH && evt == Event.READY) {
                state = State.Connected;

                JSONObject data = json.getJSONObject("data");
                build = DiscordBuild.from(data.optJSONObject("config").optString("api_endpoint"));

                if (build == preferredOrder[0] || preferredOrder[0] == DiscordBuild.ANY) {
                    if (listener != null) {
                        listener.onReady(client, User.from(data.getJSONObject("user")));
                    }
                    return;
                }
            }

            close();
        }

        throw new IOException("No Discord IPC pipe available");
    }

    protected boolean write(final byte[] data) throws IOException {
        if (!(channel instanceof WritableByteChannel wch) || !channel.isOpen()) return false;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            if (wch.write(buffer) == -1) return false;
        }
        return true;
    }

    protected void readFully(byte[] data, int offset, int length) throws IOException, InterruptedException {
        if (!(channel instanceof ReadableByteChannel rch) || !channel.isOpen())
           throw new IOException("Not readable");

        ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);
        int attempts = 0;
        while (buffer.hasRemaining()) {
            int r = rch.read(buffer);

            if (r < 0) throw new IOException("Pipe closed");
            
            if (r == 0) {
                if (++attempts > 100)  {
                    throw new IOException("Read timeout");
                }
                Thread.sleep(1);
            } else {
                attempts = 0;
            }
        }
    }

    public boolean sendPacket(Packet packet) {
        try {
            packet.getJson().put("nonce", IPCClient.getNonce());
            byte[] bytes = packet.toBytes();

            if (bytes.length > MAX_RPC_FRAME_SIZE) 
                throw new IllegalArgumentException("Packet too large: " + bytes.length);

            return write(bytes);
        } catch (Exception ignored) { return false; }
    }

    public Packet readPacket() throws IOException, JSONException, InterruptedException {
        byte[] header = new byte[8];
        readFully(header, 0, 8);
       
        ByteBuffer headerBuf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int opInt = headerBuf.getInt();
        int length = headerBuf.getInt();

        if (length < 0 || length > MAX_RPC_FRAME_SIZE)
            throw new IOException("Invalid frame length: " + length);

        byte[] payload = new byte[length];
        readFully(payload, 0, length);
    
        Opcode op = Opcode.of(opInt);
        String json = new String(payload, StandardCharsets.UTF_8);

        return new Packet(op, new JSONObject(json));
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        state = State.Disconnected;
    }

    public boolean isOpen() {
        return channel != null && channel.isOpen() && state == State.Connected;
    }

    public void setListener(IPCListener listener) {
        this.listener = listener;
    }

    public DiscordBuild getBuild() {
        return build;
    }

    public State getState() {
        return state;
    }
}