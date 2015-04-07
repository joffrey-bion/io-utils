package org.hildan.utils.io.binary;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a stream of bits. It wraps any {@link OutputStream}, buffering it so that it can be
 * written bit by bit. A {@link BitOutputStream} also allows to write unaligned {@code byte}s,
 * {@code char} s, {@code int}s and {@code long}s.
 */
public class BitOutputStream extends BufferedOutputStream {

    private long buffer = 0;

    private int bufferLength = 0;

    /**
     * Creates a new {@link BitOutputStream} wrapping the specified {@link OutputStream}.
     *
     * @param out
     *            the {@link OutputStream} to wrap
     */
    public BitOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Creates a new {@link BitOutputStream} wrapping the specified {@link OutputStream}.
     *
     * @param out
     *            the {@link OutputStream} to wrap
     * @param size
     *            the buffer size for the underlying {@link BufferedOutputStream}
     */
    public BitOutputStream(OutputStream out, int size) {
        super(out, size);
    }

    /**
     * Returns the value of the last n bits. If {@code n > 63}, the initial value is returned. If
     * {@code n == 0}, 0 is returned (no bit kept).
     *
     * @param value
     *            the value to keep the last n bits from
     * @param n
     *            the number of bits to keep
     * @return the long value represented by the last n bits of the specified value
     */
    private static long keepLastNBits(long value, int n) {
        if (n == 0) {
            // shift of 64 bits won't shift at all, force 0 instead
            return 0L;
        } else if (n > 63) {
            return value;
        }
        final int numBitsToDelete = Long.SIZE - n;
        return value << numBitsToDelete >>> numBitsToDelete;
    }

    /**
     * Returns the complete bytes within the specified bits.
     *
     * @param value
     *            the bits value
     * @param n
     *            the number of least significant bits to consider
     * @return a byte array containing the complete bytes from the most significant bits to the
     *         least significant ones.
     */
    private static byte[] getCompleteBytes(long value, int n) {
        final byte[] bytes = new byte[n / 8];
        long completeBytes = value >>> n % 8;
        for (int i = 0; i < bytes.length; i++) {
            bytes[bytes.length - i - 1] = (byte) (completeBytes & 0xFF);
            completeBytes >>>= 8;
        }
        return bytes;
    }

    /**
     * Writes the complete bytes of the buffer to the underlying {@link BufferedOutputStream}.
     *
     * @throws IOException
     *             if an I/O error occurs
     */
    private void writeBufferExcess() throws IOException {
        final byte[] bytes = getCompleteBytes(buffer, bufferLength);
        for (final byte b : bytes) {
            super.write(b);
            bufferLength -= 8;
        }
        buffer = keepLastNBits(buffer, bufferLength % 8);
        assert bufferLength < 8 : "buffer excess not completely removed";
    }

    /**
     * Writes the specified bit to this {@link BitOutputStream}.
     *
     * @param bit
     *            the bit to write, as an int
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized void writeBit(int bit) throws IOException {
        if (bit != 0 && bit != 1) {
            throw new IllegalArgumentException("the specified bit is neither 0 nor 1");
        }
        buffer = (buffer << 1) + bit;
        bufferLength += 1;
        writeBufferExcess();
    }

    /**
     * Writes the specified bit to this {@link BitOutputStream}.
     *
     * @param value
     *            the value of the bits to write
     * @param nBits
     *            the number of bits to write. Must not exceed {@link Long#SIZE}. The least
     *            significant (right-most) bits are taken from the input value.
     *
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized void writeBits(long value, int nBits) throws IOException {
        if (nBits > Long.SIZE) {
            throw new IllegalArgumentException("cannot write more bits than the length of a long");
        }
        if (nBits <= 8) {
            final long cleanBits = keepLastNBits(value, nBits);
            buffer = (buffer << nBits) + cleanBits;
            bufferLength += nBits;
            writeBufferExcess();
        } else {
            final byte[] bytes = getCompleteBytes(value, nBits);
            for (final byte b : bytes) {
                writeBits(b, 8);
            }
            writeBits(value, nBits - 8 * bytes.length);
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        writeBufferExcess();
        if (bufferLength > 0) {
            super.write((int) (buffer << 8 - bufferLength));
        }
        super.flush();
    }

    /**
     * Writes the specified bit to this stream.
     *
     * @param bit
     *            the bit to write: {@code true} for 1, {@code false} for 0
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeBit(boolean bit) throws IOException {
        if (bit) {
            writeBit(1);
        } else {
            writeBit(0);
        }
    }

    /**
     * Writes the given byte to this stream, with leading zeros to reach {@link Byte#SIZE}.
     *
     * @param value
     *            The value to write.
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeByte(byte value) throws IOException {
        writeBits(value, Byte.SIZE);
    }

    /**
     * Writes the given character's code to this stream, with leading zeros to reach
     * {@link Character#SIZE}.
     *
     * @param value
     *            The value to write.
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeChar(char value) throws IOException {
        writeBits(value, Character.SIZE);
    }

    /**
     * Writes the given integer to this stream, with leading zeros to reach {@link Integer#SIZE}.
     *
     * @param value
     *            The value to write.
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeInt(int value) throws IOException {
        writeBits(value, Integer.SIZE);
    }

    /**
     * Writes the given long to this stream, with leading zeros to reach {@link Long#SIZE}.
     *
     * @param value
     *            The value to write.
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeLong(long value) throws IOException {
        writeBits(value, Long.SIZE);
    }

    /**
     * Writes the given binary string to a buffer that will be written byte by byte.
     *
     * @param binaryString
     *            A binary {@code String}. This {@code String} must contain only the characters '0'
     *            or '1'.
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeString(String binaryString) throws IOException {
        if (!binaryString.matches("[01]*")) {
            throw new IllegalArgumentException("The input string '" + binaryString + "' must contain only 0s and 1s.");
        }
        for (final char c : binaryString.toCharArray()) {
            if (c == '0') {
                writeBit(0);
            } else {
                assert c == '1' : "argument check failed";
                writeBit(1);
            }
        }
    }

    /**
     * Closes this stream, flushing the buffer.
     */
    @Override
    public void close() {
        try {
            flush();
            super.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
