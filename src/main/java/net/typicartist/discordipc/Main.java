package net.typicartist.discordipc;

import net.typicartist.discordipc.data.RichPresense;
import net.typicartist.discordipc.data.User;

public class Main {
    public static void main(String[] args) {
        long clientId = 1120354582418165780l; 

        IPCClient client = new IPCClient(clientId);
        client.setListener(new IPCListener() {
            @Override
            public void onReady(IPCClient client, User user) {
                System.out.println("Logged as " + (user != null ? user.getUsername() : "unknown"));
                client.updatePresence(new RichPresense());
            }
        });
        client.connect();
    }
}
