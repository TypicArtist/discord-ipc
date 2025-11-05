package net.typicartist.discordipc;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONException;
import org.json.JSONObject;

import net.typicartist.discordipc.connection.Connection;
import net.typicartist.discordipc.data.Packet;
import net.typicartist.discordipc.data.RichPresense;
import net.typicartist.discordipc.enums.Command;
import net.typicartist.discordipc.enums.Event;
import net.typicartist.discordipc.enums.Opcode;

public class IPCClient {
    private final long clientId;
    private Connection connection;
    private IPCListener listener;

    private final Queue<Packet> outgoing = new ConcurrentLinkedQueue<>();
    private final Queue<Packet> incoming = new ConcurrentLinkedQueue<>();

    private Thread ioThread;
    private Thread readThread;

    public IPCClient(long clientId) {
        this.clientId = clientId;
    }

    public synchronized void connect() {
        if (connection != null && connection.isOpen()) {
            throw new IllegalStateException("Already connected.");
        }

        connection = Connection.create(this, clientId);
        connection.setListener(listener);

        startThreads();
    }

    public synchronized void shutdown() {
        if (connection != null && !connection.isOpen()) {
            throw new IllegalStateException("IPCClient (ID: " + clientId + ") is not connected!");
        }

        if (ioThread != null && ioThread.isAlive()) {
            ioThread.interrupt();
        }
        try {
            if (connection != null) connection.close();
        } catch (Exception ignored) {}
    }

    public void subscribe(Event event) {
        if (connection != null && !connection.isOpen()) {
            throw new IllegalStateException("IPCClient (ID: " + clientId + ") is not connected!");
        }

        try {
            if (event.isSubscribable()) {
                JSONObject data = new JSONObject()
                                        .put("cmd", Command.SUBSCRIBE.name())
                                        .put("evt", event.name());
                outgoing.offer(new Packet(Opcode.Frame, data));
            }
        } catch (JSONException ignored) {}
    }

    public void unsubscribe(Event event) {
        if (connection != null && !connection.isOpen()) {
            throw new IllegalStateException("IPCClient (ID: " + clientId + ") is not connected!");
        }

        try {
            if (event.isSubscribable()) {
                JSONObject data = new JSONObject()
                                        .put("cmd", Command.UNSUBSCRIBE.name())
                                        .put("evt", event.name());
                outgoing.offer(new Packet(Opcode.Frame, data));
            }
        } catch (JSONException ignored) {}
    }

    public void updatePresence(RichPresense presense) {
        if (connection != null && !connection.isOpen()) {
            throw new IllegalStateException("IPCClient (ID: " + clientId + ") is not connected!");
        }                                

        try {
            JSONObject data = new JSONObject()
                                    .put("cmd", Command.SET_ACTIVITY.name())
                                    .put("args", new JSONObject()
                                            .put("pid", getPid())
                                            .put("activity", presense.toJson()));
            Packet packet = new Packet(Opcode.Frame, data);
            outgoing.offer(packet);
        } catch (JSONException ignored) { ignored.printStackTrace(); }
    }

    public void setListener(IPCListener listener) {
        this.listener = listener;
        if (connection != null) {
            connection.setListener(listener);
        }
    }

    private void startThreads() {
        readThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (connection == null || !connection.isOpen()) continue;

                try {
                    Packet packet = connection.readPacket();
                    if (packet == null) continue;
                    if (packet.getOp() == Opcode.Close) {
                        if (listener != null) {
                            listener.onDisconnected(this, "Received Close opcode");
                        }
                        break;
                    }
                    incoming.offer(packet);
                } catch (Exception e) {
                    if (listener != null) listener.onError(this, e.getMessage());
                }
            }
        }, "Discord-IPC-Reader");

        ioThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (connection == null) return;

                try {
                    synchronized (connection) {
                        if (!connection.isOpen()) connection.open();
                    }

                    Packet in;
                    while ((in = incoming.poll()) != null) {
                        JSONObject json = in.getJson();
                        Event evt = Event.of(json.optString("evt"));

                        switch (evt) {
                            default:
                                break;
                        }
                    }

                    Packet out;
                    while ((out = outgoing.poll()) != null) {
                        synchronized (connection) {
                            if (!connection.sendPacket(out)) {
                                outgoing.offer(out);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(this, e.getMessage());
                    }
                } finally {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }
            }
        }, "Discord-IPC-IO");

        readThread.start();
        ioThread.start();
    }

    public static long getPid() {
        return ProcessHandle.current().pid();
    }

    public static String getNonce() {
        return UUID.randomUUID().toString();
    }
}