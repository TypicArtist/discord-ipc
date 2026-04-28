package net.typicartist.discordipc.connection;

import java.io.File;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.typicartist.discordipc.IPCClient;

public class UnixConnection extends Connection {
    private SocketChannel channel;
    private static String cachedBase;

    public UnixConnection(IPCClient client, long clientId) {
        super(client, clientId);
    }

    @Override
    protected void open(int index) throws IOException {
        String base = findSocketBase();
        channel = SocketChannel.open(
            UnixDomainSocketAddress.of(base + "/discord-ipc-" + index)
        );
        channel.configureBlocking(true);
    }

    @Override
    protected int read(byte[] buf, int offset, int len) throws IOException {
        return channel.read(ByteBuffer.wrap(buf, offset, len));
    }

    @Override
    protected void write(byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    @Override
    public boolean isOpen() {
        return channel != null && channel.isOpen() && super.isOpen();
    }

    @Override
    public void close() throws IOException {
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
        } finally {
            super.close();
        }
    }

    private static String findSocketBase() throws IOException {
        if (cachedBase != null) return cachedBase;

        String[] candidates = {
            System.getenv("XDG_RUNTIME_DIR"),
            System.getenv("TMPDIR"),
            System.getenv("TMP"),
            System.getenv("TEMP"),
            "/tmp"
        };
        for (String base : candidates) {
            if (base != null && new File(base).isDirectory()) {
                cachedBase = base;
                return cachedBase;
            }
        }

        throw new IOException("No valid temp directory found");
    }
}
