package edu.usf.imunet.rendering;

import android.opengl.GLES30;

import java.io.Closeable;
import java.nio.IntBuffer;

public class IndexBuffer implements Closeable {
    private final GpuBuffer buffer;

    /**
     * Construct an {@link IndexBuffer} populated with initial data.
     *
     * <p>The GPU buffer will be filled with the data in the <i>direct</i> buffer {@code entries},
     * starting from the beginning of the buffer (not the current cursor position). The cursor will be
     * left in an undefined position after this function returns.
     *
     * <p>The {@code entries} buffer may be null, in which case an empty buffer is constructed
     * instead.
     */
    public IndexBuffer(MotionRajawaliRenderer render, IntBuffer entries) {
        buffer = new GpuBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, GpuBuffer.INT_SIZE, entries);
    }

    /**
     * Populate with new data.
     *
     * <p>The entire buffer is replaced by the contents of the <i>direct</i> buffer {@code entries}
     * starting from the beginning of the buffer, not the current cursor position. The cursor will be
     * left in an undefined position after this function returns.
     *
     * <p>The GPU buffer is reallocated automatically if necessary.
     *
     * <p>The {@code entries} buffer may be null, in which case the buffer will become empty.
     */
    public void set(IntBuffer entries) {
        buffer.set(entries);
    }

    @Override
    public void close() {
        buffer.free();
    }

    /* package-private */
    int getBufferId() {
        return buffer.getBufferId();
    }

    /* package-private */
    int getSize() {
        return buffer.getSize();
    }
}

