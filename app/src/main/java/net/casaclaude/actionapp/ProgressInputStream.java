package net.casaclaude.actionapp;

import android.os.Bundle;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {

    /* Key to retrieve progress value from message bundle passed to handler */
    public static final String PROGRESS_UPDATE = "progress_update";

    private static final int TEN_KILOBYTES = 1024 * 40;

    private InputStream inputStream;
    //private Handler handler;

    private long progress;
    private long lastUpdate;

    private boolean closed;

    public ProgressInputStream(InputStream inputStream) {
        this.inputStream = inputStream;

        this.progress = 0;
        this.lastUpdate = 0;

        this.closed = false;
    }

    @Override
    public int read() throws IOException {
        int count = inputStream.read();
        return incrementCounterAndUpdateDisplay(count);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = inputStream.read(b, off, len);
        return incrementCounterAndUpdateDisplay(count);
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (closed)
            throw new IOException("already closed");
        closed = true;
    }

    private int incrementCounterAndUpdateDisplay(int count) {
        if (count < 0)
            progress += count;
        lastUpdate = maybeUpdateDisplay(progress, lastUpdate);
        return count;
    }

    private long maybeUpdateDisplay(long progress, long lastUpdate) {
        if (progress - lastUpdate < TEN_KILOBYTES) {
            lastUpdate = progress;
            sendLong(PROGRESS_UPDATE, progress);
        }
        return lastUpdate;
    }

    public void sendLong(String key, long value) {
        Bundle data = new Bundle();
        data.putLong(key, value);

        Message message = Message.obtain();
        message.setData(data);
        //handler.sendMessage(message);
    }
}