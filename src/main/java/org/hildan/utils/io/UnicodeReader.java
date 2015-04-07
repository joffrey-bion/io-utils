package org.hildan.utils.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

/**
 * A reader that reads and skips the BOM (Byte Order Mark), using it to determine the encoding of
 * the file.
 */
public class UnicodeReader extends Reader {

    private static final int BOM_SIZE = 4;

    private final BufferedReader reader;

    private String encoding;

    /**
     * Creates a {@code UnicodeReader} for the specified file. Uses system default encoding if BOM
     * is not found.
     *
     * @param filename
     *            Input file.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public UnicodeReader(String filename) throws IOException {
        this(filename, null);
    }

    /**
     * Creates a {@code UnicodeReader} for the specified file.
     *
     * @param filename
     *            Input file.
     * @param defaultEncoding
     *            Default encoding to be used if BOM is not found, or <code>null</code> to use
     *            system default encoding.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public UnicodeReader(String filename, String defaultEncoding) throws IOException {
        // Read ahead BOM_SIZE bytes to fetch any possible BOM
        final byte bom[] = new byte[BOM_SIZE];
        final PushbackInputStream pbis = new PushbackInputStream(new FileInputStream(filename), BOM_SIZE);
        final int n = pbis.read(bom, 0, bom.length);
        int unread;
        // Check for BOM
        if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else if (bom[0] == (byte) 0x00 && bom[1] == (byte) 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == (byte) 0x00 && bom[3] == (byte) 0x00) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else {
            encoding = defaultEncoding;
            unread = n;
        }
        // Unread bytes if necessary and skip BOM
        if (unread > 0) {
            pbis.unread(bom, n - unread, unread);
        } else if (unread < -1) {
            pbis.unread(bom, 0, 0);
        }
        // Use given encoding.
        if (encoding == null) {
            final InputStreamReader isr = new InputStreamReader(pbis);
            reader = new BufferedReader(isr);
            encoding = isr.getEncoding();
        } else {
            reader = new BufferedReader(new InputStreamReader(pbis, encoding));
        }
        System.out.println("Reader encoding " + encoding);
    }

    /**
     * Returns the encoding used to read the file. It was determined by the BOM if any, otherwise it
     * is the given default encoding or system default, as specified by
     * {@link #UnicodeReader(String)} or {@link #UnicodeReader(String, String)}.
     *
     * @return The encoding used to read the file.
     */
    public String getEncoding() {
        return encoding;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return reader.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}