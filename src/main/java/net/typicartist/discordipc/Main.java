package net.typicartist.discordipc;

import net.typicartist.discordipc.data.RichPresence;
import net.typicartist.discordipc.data.User;

public class Main {
    public static void main(String[] args) throws Exception {
        IPCClient client = new IPCClient(1497532235530768394L);
        client.setListener(new IPCListener() {
            @Override
            public void onReady(IPCClient client, User user) {
                System.out.println("Logged as " + (user != null ? user.getUsername() : "unknown"));
                client.updatePresence(new RichPresence.Builder()
                            .setDetails("清水谷は友達")
                            .setState("Hmmm")
                            .build()
                );
            }
        });
        client.connect();
        
        Thread.currentThread().join();
    }
}
