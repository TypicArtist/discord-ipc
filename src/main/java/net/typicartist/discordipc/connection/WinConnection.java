package net.typicartist.discordipc.connection;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.typicartist.discordipc.IPCClient;

public class WinConnection extends Connection {
    private RandomAccessFile pipe;

    public WinConnection(IPCClient client, long clientId) {
        super(client, clientId);
    }

    @Override
    public void open(int index) throws IOException {
        pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + index, "rw");
    }

    @Override
    protected int read(byte[] buf, int offset, int len) throws IOException {
        return pipe.read(buf, offset, len);
    }

    @Override
    protected void write(byte[] data) throws IOException {
        pipe.write(data);
    }

    @Override
    public boolean isOpen() {
        return pipe != null && pipe.getChannel().isOpen() && super.isOpen();
    }

    @Override
    public void close() throws IOException {
        try {
            if (pipe != null) {
                pipe.close();
                pipe = null;
            }
        } finally {
            super.close();
        }
    }
}