package net.typicartist.discordipc;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;

import net.typicartist.discordipc.connection.Connection;
import net.typicartist.discordipc.data.Packet;
import net.typicartist.discordipc.data.RichPresence;
import net.typicartist.discordipc.data.User;
import net.typicartist.discordipc.enums.Command;
import net.typicartist.discordipc.enums.Event;
import net.typicartist.discordipc.enums.Opcode;
import net.typicartist.discordipc.exception.IPCConnectionException;
import net.typicartist.discordipc.exception.IPCNotConnectedException;
import net.typicartist.discordipc.exception.IPCProtocolException;

public class IPCClient {
    private static final long RECONNECT_DELAY_MS = 5000;
    private static final long READ_RETRY_DELAY_MS = 100;

    private final long clientId;
    private Connection conn;
    private IPCListener listener;

    private final BlockingQueue<Packet> outgoing = new LinkedBlockingQueue<>();
    private final BlockingQueue<Packet> incoming = new LinkedBlockingQueue<>();

    private volatile boolean running = false;
    private Thread ioThread;
    private Thread readThread;

    public IPCClient(long clientId) {
        this.clientId = clientId;
    }

    public void connect() {
        if (running) throw new IllegalStateException("Already connected or connecting.");

        conn = Connection.create(this, clientId);
        conn.setListener(listener);

        running = true;
        startThreads();
    }

    public void shutdown() throws InterruptedException {
        running = false;
        if (ioThread != null) ioThread.interrupt();
        if (readThread != null) readThread.interrupt();

        ioThread.join();
        readThread.join();

        try {
            if (conn != null) conn.close();
        } catch (Exception ignored) {}
    }

    public void subscribe(Event event) {
        if (conn == null || !conn.isOpen()) throw new IPCNotConnectedException(clientId);

        if (!event.isSubscribable()) return;
        
        try {
            outgoing.add(new Packet(Opcode.Frame, new JSONObject()
                    .put("cmd", Command.SUBSCRIBE.name())
                    .put("evt", event.name())
            ));
        } catch (JSONException e) { 
            throw new IPCProtocolException("Malformed presence JSON", e);
        }
    }

    public void unsubscribe(Event event) {
        if (conn == null || !conn.isOpen()) throw new IPCNotConnectedException(clientId);

        if (!event.isSubscribable()) return;
        
        try {
            outgoing.add(new Packet(Opcode.Frame, new JSONObject()
                    .put("cmd", Command.UNSUBSCRIBE.name())
                    .put("evt", event.name())
            ));
        } catch (JSONException e) { 
            throw new IPCProtocolException("Malformed presence JSON", e);
        }
    }

    public void updatePresence(RichPresence presense) {
        if (conn == null || !conn.isOpen()) throw new IPCNotConnectedException(clientId);

        try {
            outgoing.add(new Packet(Opcode.Frame, new JSONObject()
                    .put("cmd", Command.SET_ACTIVITY.name())
                    .put("args", new JSONObject()
                            .put("pid", getPid())
                            .put("activity", presense.toJson()))
            ));
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public void setListener(IPCListener listener) {
        this.listener = listener;
        if (conn != null) {
            conn.setListener(listener);
        }
    }

    private void startThreads() {
        ioThread = new Thread(() -> {
            while (running) {
                try {
                    if (!conn.isOpen()) {
                        try {
                            conn.connect();
                        } catch (IPCConnectionException e) {
                            if (listener != null) listener.onError(this, "Reconnect failed: " + e.getMessage());
                            Thread.sleep(RECONNECT_DELAY_MS);
                            continue;
                        }
                    }

                    Packet in;
                    while ((in = incoming.poll()) != null) {
                        dispatch(in);
                    }

                    Packet out;
                    while ((out = outgoing.poll()) != null) {
                        conn.sendPacket(out.getOp(), out.getJson());
                        if (listener != null) listener.onPacketSent(this, out);
                    }

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    if (listener != null) listener.onError(this, e.getMessage());
                }
            }
        }, "Discord-IPC-IO");

        readThread = new Thread(() -> {
            while (running) {
                if (!conn.isOpen()) {
                    try { Thread.sleep(READ_RETRY_DELAY_MS); } catch (InterruptedException e) { 
                        Thread.currentThread().interrupt(); 
                        break;
                    }
                    continue;
                }

                try {
                    Packet packet = conn.readPacket();
                    if (packet != null) incoming.offer(packet);
                } catch (Exception e) {
                    if (running && listener != null)
                        listener.onError(this, "Read error: " + e.getMessage());
                    try { Thread.sleep(READ_RETRY_DELAY_MS); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Discord-IPC-Reader");

        ioThread.setDaemon(true);
        readThread.setDaemon(true);
        ioThread.start();
        readThread.start();
    }

    private void dispatch(Packet packet) {
        JSONObject json = packet.getJson();
        Event evt = Event.of(json.optString("evt"));

        if (listener == null) return;

        listener.onPacketReceived(this, packet);

        switch (evt) {
            case ACTIVITY_JOIN: 
                listener.onJoinGame(this, json.optString("data"));
                break;
            case ACTIVITY_SPECTATE: 
                listener.onSpectateGame(this, json.optString("data"));
                break;
            case ACTIVITY_JOIN_REQUEST: 
                listener.onJoinRequest(this, User.fromJson(json.optJSONObject("data")));
                break;
            case ERROR: 
                listener.onError(this, json.toString());
                break;
            default: break;
        }
    }

    public static long getPid() {
        return ProcessHandle.current().pid();
    }

    public static String getNonce() {
        return UUID.randomUUID().toString();
    }
}