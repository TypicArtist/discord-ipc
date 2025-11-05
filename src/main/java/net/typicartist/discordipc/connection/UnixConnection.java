package net.typicartist.discordipc.connection;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.SocketChannel;

import net.typicartist.discordipc.IPCClient;

public class UnixConnection extends Connection {
    public UnixConnection(IPCClient client, long clientId) {
        super(client, clientId);
    }

    @Override
    public boolean open(int index) {
        String tempPath = getTempPath();
        UnixDomainSocketAddress addr = UnixDomainSocketAddress.of(String.format("%s/discord-ipc-%d", tempPath, index));
        try {
            channel = SocketChannel.open(addr);
            if (channel.isOpen()) {
                return true;
            }
        } catch (IOException ignored) {}

        return false;
    }

    private static String getTempPath() {
        String temp = System.getenv("XDG_RUNTIME_DIR");
        if (temp == null) temp = System.getenv("TMPDIR");
        if (temp == null) temp = System.getenv("TMP");
        if (temp == null) temp = System.getenv("TEMP");
        if (temp == null) temp = "/tmp";
        return temp;
    }
}
