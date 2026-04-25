package net.typicartist.discordipc;

import java.util.Deque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONException;
import org.json.JSONObject;

import net.typicartist.discordipc.connection.Connection;
import net.typicartist.discordipc.data.Packet;
import net.typicartist.discordipc.data.RichPresence;
import net.typicartist.discordipc.enums.Command;
import net.typicartist.discordipc.enums.Event;
import net.typicartist.discordipc.enums.Opcode;

public class IPCClient {
    private static final long RECOONECT_DELAY_MS = 5000;
    private static final long IO_INTERVAL_MS = 50;
    private static final long READ_RETRY_DELAY_MS = 100;

    private final long clientId;
    private Connection connection;
    private IPCListener listener;

    private final Deque<Packet> outgoing = new ConcurrentLinkedDeque<>();
    private final Queue<Packet> incoming = new ConcurrentLinkedQueue<>();

    private volatile boolean running = false;
    private Thread ioThread;
    private Thread readThread;

    public IPCClient(long clientId) {
        this.clientId = clientId;
    }

    public void connect() {
        if (running) {
            throw new IllegalStateException("Already connected or connecting.");
        }

        connection = Connection.create(this, clientId);
        connection.setListener(listener);

        running = true;
        startThreads();
    }

    public void shutdown() {
        running = false;
        if (ioThread != null) ioThread.interrupt();
        if (readThread != null ) readThread.interrupt();

        try {
            if (connection != null) connection.close();
        } catch (Exception ignored) {}
    }

    public void subscribe(Event event) {
        ensureConnected();
        try {
            if (event.isSubscribable()) {
                outgoing.add(new Packet(Opcode.Frame, new JSONObject()
                        .put("cmd", Command.SUBSCRIBE.name())
                        .put("evt", event.name())
                ));
            }
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public void unsubscribe(Event event) {
        ensureConnected();
        try {
            if (event.isSubscribable()) {
                outgoing.add(new Packet(Opcode.Frame, new JSONObject()
                        .put("cmd", Command.UNSUBSCRIBE.name())
                        .put("evt", event.name())
                ));
            }
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public void updatePresence(RichPresence presense) {
        ensureConnected();
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
        if (connection != null) {
            connection.setListener(listener);
        }
    }

    private void ensureConnected() {
        if (connection == null || !connection.isOpen()) {
            throw new IllegalStateException("IPCClient (ID: " + clientId +  ") is not connected!");
        }
    }

    private void startThreads() {
        ioThread = new Thread(() -> {
            while (running) {
                try {
                    if (!connection.isOpen()) {
                        try {
                            connection.open();
                        } catch (Exception e) {
                            if (listener != null) listener.onError(this, "Reconnect failed: " + e.getMessage());
                            
                            Thread.sleep(RECOONECT_DELAY_MS);
                            continue;
                        }
                    }

                    Packet in;
                    while ((in = incoming.poll()) != null) {
                        try {
                            JSONObject json = in.getJson();
                            Event evt = Event.of(json.optString("evt"));
                            String nonce = json.optString("nonce", null);

                            if (listener != null) {
                                listener.onPacketReceived(this, in);
                            }

                            switch (evt) {
                                case ERROR:
                                    break;
                                case ACTIVITY_JOIN:
                                    break;
                                case ACTIVITY_SPECTATE:
                                    break;
                                case ACTIVITY_JOIN_REQUEST:
                                    break;
                                case UNKNOWN:
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    while (!(outgoing.isEmpty())) {
                        Packet out = outgoing.peek();
                        if (connection.sendPacket(out)) {
                            outgoing.poll();
                        } else {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    if (listener != null) listener.onError(this, e.getMessage());
                } finally {
                    try {
                        Thread.sleep(IO_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Discord-IPC-IO");

        readThread = new Thread(() -> {
            while (running) {
                if (!connection.isOpen()) {
                    try { Thread.sleep(READ_RETRY_DELAY_MS); } catch (InterruptedException e) { 
                        Thread.currentThread().interrupt(); 
                        break;
                    }
                    continue;
                }

                try {
                    Packet packet = connection.readPacket();
                    if (packet != null) {
                        incoming.offer(packet);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    if (running && listener != null) {
                        listener.onError(this, "Read error: " + e.getMessage());
                    }
                    try { Thread.sleep(READ_RETRY_DELAY_MS);} catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Discord-IPC-Reader");

        ioThread.setDaemon(true);
        ioThread.start();
        readThread.setDaemon(true);
        readThread.start();
    }

    public static long getPid() {
        return ProcessHandle.current().pid();
    }

    public static String getNonce() {
        return UUID.randomUUID().toString();
    }
}