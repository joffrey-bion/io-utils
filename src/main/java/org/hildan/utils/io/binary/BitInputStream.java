package org.hildan.utils.io.binary;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a stream of bits. It wraps any {@link InputStream}, buffering it so
 * that it can be accessed bit by bit. A {@link BitInputStream} also allows to access
 * unaligned {@code byte}s, {@code char} s, {@code int}s and {@code long}s.
 */
public class BitInputStream extends BufferedInputStream {

    private static final int BITS_PER_BYTE = 8;
    /**
     * The buffer of bits. Only the right-most bits (least significant) are used.
     */
    private long buffer = 0;
    /**
     * Indicates how many bits of the buffer are currently used.
     */
    private int bufferLength = 0;

    /**
     * Creates a new {@link BitInputStream} reading from the specified file.
     * 
     * @param filename
     *            the name of the file to read from
     * @throws FileNotFoundException
     *             if the file does not exist, is a directory rather than a regular
     *             file, or for some other reason cannot be opened for reading.
     */
    public BitInputStream(String filename) throws FileNotFoundException {
        this(new File(filename));
    }

    /**
     * Creates a new {@link BitInputStream} reading from the specified file.
     * 
     * @param file
     *            the file to read from
     * @throws FileNotFoundException
     *             if the file does not exist, is a directory rather than a regular
     *             file, or for some other reason cannot be opened for reading.
     */
    public BitInputStream(File file) throws FileNotFoundException {
        super(new FileInputStream(file));
    }

    /**
     * Creates a new {@link BitInputStream} wrapping the specified
     * {@link InputStream}.
     * 
     * @param in
     *            the {@link InputStream} to wrap
     */
    public BitInputStream(InputStream in) {
        super(in);
    }

    /**
     * Creates a new {@link BitInputStream} wrapping the specified
     * {@link InputStream}.
     * 
     * @param in
     *            the {@link InputStream} to wrap
     * @param size
     *            the buffer size for the underlying {@link BufferedInputStream}
     */
    public BitInputStream(InputStream in, int size) {
        super(in, size);
    }

    @Override
    public synchronized int available() throws IOException {
        return bufferLength / 8 + super.available();
    }

    /**
     * Reads the next byte from this stream. If less than 8 bits are available
     * because the end of the stream has been reached, the value -1 is returned.
     * 
     * @see InputStream#read()
     */
    @Override
    public synchronized int read() throws IOException {
        try {
            return readByte();
        } catch (IllegalStateException ise) {
            // end of stream reached
            return -1;
        }
    }

    /**
     * Retrieves and removes the first {@code length} bits from the buffer. Ensure
     * the buffer is long enough before calling this method.
     * 
     * @param length
     *            the number of bits to get
     * @return the long value representing the bits taken from the buffer
     */
    private synchronized long pollBitsFromBuffer(int length) {
        assert bufferLength >= length : "buffer too short!";
        long leftBits = buffer >>> (bufferLength - length);
        buffer -= leftBits << (bufferLength - length);
        bufferLength -= length;
        return leftBits;
    }

    /**
     * Reads up to {@link Long#SIZE} bits as a long value.
     * 
     * @param length
     *            the number of bits to read. Must not exceed {@link Long#SIZE}.
     * @param failOnEOF
     *            indicates how to behave in case of premature end of input. If
     *            {@code true}, this method throws {@link IllegalStateException} if
     *            there's not enough bits to read. If {@code false}, this methods
     *            returns -1 in such a case. The problem is that there is no way to
     *            tell if -1 is returned because it is the value that was read or a
     *            premature end of input.
     * @return the long value of the read bits, or -1 if the end of input is reached
     *         (only if {@code failOnEOF} is {@code false})
     * @throws IllegalStateException
     *             if the end of stream is reached before the specified number of
     *             bits could be read (only if {@code failOnEOF} is {@code true})
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized long readBits(int length, boolean failOnEOF) throws IOException {
        if (length > Long.SIZE) {
            throw new IllegalArgumentException("can't read more bits than the size of a long");
        }
        while (bufferLength < length) {
            int octet = super.read();
            if (octet == -1) {
                if (failOnEOF) {
                    throw new IllegalStateException("premature end of input, cannot read the requested number of bits");
                } else {
                    return -1;
                }
            }
            buffer = (buffer << BITS_PER_BYTE) + octet;
            bufferLength += BITS_PER_BYTE;
        }
        return pollBitsFromBuffer(length);
    }

    /**
     * Reads up to {@link Long#SIZE} bits as a long value.
     * 
     * @param length
     *            the number of bits to read. Must not exceed {@link Long#SIZE}, nor
     *            the number of available bits in this stream.
     * @return the long value of the read bits
     * @throws IllegalStateException
     *             if the end of stream is reached before the specified number of
     *             bits could be read
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized long readBits(int length) throws IOException {
        return readBits(length, true);
    }

    /**
     * Reads the next bit from this stream.
     *
     * @return 1 or 0 depending on the read bit, or -1 if the end of stream was
     *         reached
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized int readBit() throws IOException {
        assert bufferLength >= 0 : "buffer has negative length";
        if (bufferLength == 0) {
            int octet = super.read();
            if (octet == -1) {
                return -1;
            }
            buffer = octet;
            bufferLength = BITS_PER_BYTE;
        }
        return (int) pollBitsFromBuffer(1);
    }

    /**
     * Reads a binary String representing the next {@code length} bits in this
     * stream.
     *
     * @param length
     *            the number of bits to read
     * @return a {@code String} representing the bits read with the characters '0'
     *         and '1'. The left-most characters are the first bits read from the
     *         stream.
     * @throws IllegalStateException
     *             if the end of stream is reached before the specified number of
     *             bits could be read
     * @throws IOException
     *             if an I/O error occurs
     */
    public String readBitsAsString(int length) throws IOException {
        int remainingLength = length;
        final StringBuilder sb = new StringBuilder();
        while (remainingLength > 0) {
            int toRead = Math.min(remainingLength, Long.SIZE);
            String tempBits = Long.toBinaryString(readBits(toRead));
            sb.append(BinHelper.addLeadingZeros(tempBits, toRead));
            remainingLength -= toRead;
        }
        return sb.toString();
    }

    /**
     * Reads the next bit from this stream as a {@code boolean}.
     *
     * @return {@code true} for a 1 and {@code false} for a 0
     * @throws IllegalStateException
     *             if the end of stream was reached before enough bits could be read
     * @throws IOException
     *             if an I/O error occurs
     */
    public boolean readBoolean() throws IOException {
        return readBits(1) % 2 == 1;
    }

    /**
     * Reads the next bit from this stream as a {@link Boolean}.
     *
     * @return {@code true} for a 1 and {@code false} for a 0, or {@code null} if the
     *         end of stream was reached
     * @throws IOException
     *             if an I/O error occurs
     */
    public Boolean readBooleanOrNull() throws IOException {
        try {
            return readBits(1) % 2 == 1;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Reads the next {@link Byte#SIZE} bits from this stream as a {@code byte}.
     *
     * @return the read {@code byte}
     * @throws IllegalStateException
     *             if the end of stream was reached before enough bits could be read
     * @throws IOException
     *             if an I/O error occurs
     */
    public byte readByte() throws IOException {
        return (byte) readBits(Byte.SIZE);
    }

    /**
     * Reads the next {@link Byte#SIZE} bits from this stream as a {@link Byte}.
     *
     * @return the read {@link Byte}, or {@code null} if the end of input was reached
     * @throws IOException
     *             if an I/O error occurs
     */
    public Byte readByteOrNull() throws IOException {
        try {
            return (byte) readBits(Byte.SIZE);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Reads the next {@link Character#SIZE} bits from this stream as a {@code char}.
     *
     * @return the read {@code char}
     * @throws IllegalStateException
     *             if the end of stream was reached before enough bits could be read
     * @throws IOException
     *             if an I/O error occurs
     */
    public char readChar() throws IOException {
        return (char) readBits(Character.SIZE);
    }

    /**
     * Reads the next {@link Character#SIZE} bits from this stream as a
     * {@link Character}.
     *
     * @return the read {@link Character}, or {@code null} if the end of input was
     *         reached
     * @throws IOException
     *             if an I/O error occurs
     */
    public Character readCharacter() throws IOException {
        try {
            return (char) readBits(Character.SIZE);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Reads the next {@link Integer#SIZE} bits from this stream as an {@code int}.
     *
     * @return the read {@code int}
     * @throws IllegalStateException
     *             if the end of stream was reached before enough bits could be read
     * @throws IOException
     *             if an I/O error occurs
     */
    public int readInt() throws IOException {
        return (int) readBits(Integer.SIZE);
    }

    /**
     * Reads the next {@link Integer#SIZE} bits from this stream as a {@link Integer}
     * .
     *
     * @return the read {@link Integer}, or {@code null} if the end of input was
     *         reached
     * @throws IOException
     *             if an I/O error occurs
     */
    public Integer readInteger() throws IOException {
        try {
            return (int) readBits(Integer.SIZE);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Reads the next {@link Long#SIZE} bits from this stream as a {@code long} from
     * this stream.
     *
     * @return the read {@code long}
     * @throws IllegalStateException
     *             if the end of stream was reached before enough bits could be read
     * @throws IOException
     *             if an I/O error occurs
     */
    public long readLong() throws IOException {
        return readBits(Long.SIZE);
    }

    /**
     * Reads the next {@link Long#SIZE} bits from this stream as a {@link Long}.
     *
     * @return the read {@link Long}, or {@code null} if the end of input was reached
     * @throws IOException
     *             if an I/O error occurs
     */
    public Long readLongOrNull() throws IOException {
        try {
            return readBits(Long.SIZE);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
