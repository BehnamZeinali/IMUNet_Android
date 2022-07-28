package edu.usf.imunet.rendering;

import android.opengl.GLES30;

import java.io.Closeable;
import java.nio.FloatBuffer;

public class VertexBuffer implements Closeable {
    private final GpuBuffer buffer;
    private final int numberOfEntriesPerVertex;

    /**
     * Construct a {@link VertexBuffer} populated with initial data.
     *
     * <p>The GPU buffer will be filled with the data in the <i>direct</i> buffer {@code entries},
     * starting from the beginning of the buffer (not the current cursor position). The cursor will be
     * left in an undefined position after this function returns.
     *
     * <p>The number of vertices in the buffer can be expressed as {@code entries.limit() /
     * numberOfEntriesPerVertex}. Thus, The size of the buffer must be divisible by {@code
     * numberOfEntriesPerVertex}.
     *
     * <p>The {@code entries} buffer may be null, in which case an empty buffer is constructed
     * instead.
     */
    public VertexBuffer(MotionRajawaliRenderer render, int numberOfEntriesPerVertex, FloatBuffer entries) {
        if (entries != null && entries.limit() % numberOfEntriesPerVertex != 0) {
            throw new IllegalArgumentException(
                    "If non-null, vertex buffer data must be divisible by the number of data points per"
                            + " vertex");
        }

        this.numberOfEntriesPerVertex = numberOfEntriesPerVertex;
        buffer = new GpuBuffer(GLES30.GL_ARRAY_BUFFER, GpuBuffer.FLOAT_SIZE, entries);
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
     * Otherwise, the size of {@code entries} must be divisible by the number of entries per vertex
     * specified during construction.
     */
    public void set(FloatBuffer entries) {
        if (entries != null && entries.limit() % numberOfEntriesPerVertex != 0) {
            throw new IllegalArgumentException(
                    "If non-null, vertex buffer data must be divisible by the number of data points per"
                            + " vertex");
        }
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
    int getNumberOfEntriesPerVertex() {
        return numberOfEntriesPerVertex;
    }

    /* package-private */
    int getNumberOfVertices() {
        return buffer.getSize() / numberOfEntriesPerVertex;
    }
}


