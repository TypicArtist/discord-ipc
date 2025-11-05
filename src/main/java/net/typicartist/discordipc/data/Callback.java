package net.typicartist.discordipc.data;

import java.util.function.Consumer;

public class Callback {
    private final Consumer<Packet> onSuccess;
    private final Consumer<String> onFailure;

    public Callback(Consumer<Packet> onSuccess, Consumer<String> onFailure) {
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    public void succeed(Packet packet) {
        if (onSuccess != null) onSuccess.accept(packet);
    }

    public void fail(String message) {
        if (onFailure != null) onFailure.accept(message);
    }
}