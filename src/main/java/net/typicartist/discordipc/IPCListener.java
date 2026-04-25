package net.typicartist.discordipc;

import net.typicartist.discordipc.data.Packet;
import net.typicartist.discordipc.data.User;

public interface IPCListener {
    default void onReady(IPCClient client, User user) {}
    default void onDisconnected(IPCClient client, String message) {}
    default void onError(IPCClient client, String message) {}
    default void onJoinGame(IPCClient client, String secret) {}
    default void onSpectateGame(IPCClient client, String secret) {}
    default void onJoinRequest(IPCClient client, User user) {}
    
    default void onPacketReceived(IPCClient client, Packet packet) {}
}