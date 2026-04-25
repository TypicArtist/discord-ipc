package net.typicartist.discordipc.connection;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.typicartist.discordipc.IPCClient;

public class WinConnection extends Connection {
    public WinConnection(IPCClient client, long clientId) {
        super(client, clientId);
    }

    @Override
    public boolean openChannel(int index) {
        Path path = Path.of(String.format("\\\\.\\pipe\\discord-ipc-%d", index));
        try {
            channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
            if (channel.isOpen()) {
                return true;
            }
        } catch (IOException ignored) {}

        return false;
    }
}