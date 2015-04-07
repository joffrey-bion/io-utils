package org.hildan.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * A class providing helpful methods to handle streams.
 */
public class Streams {

    /**
     * The size of the buffer to use when reading an {@link InputStream}, in characters.
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Reads the specified {@link InputStream} to the end, and returns it as a {@code String}.
     *
     * @param inputStream
     *            the {@code InputStream} to read
     * @param charsetName
     *            the name of the charset to use to decode the given stream
     * @return the built {@code String}, or an empty one
     * @throws UnsupportedEncodingException
     *             if the encoding specified by charsetName cannot be found
     * @throws IOException
     *             if an I/O error occurs while reading the stream
     */
    public static String toString(InputStream inputStream, String charsetName) throws UnsupportedEncodingException,
    IOException {
        final char[] buffer = new char[BUFFER_SIZE];
        final StringBuilder sb = new StringBuilder();
        Reader in = null;
        try {
            in = new InputStreamReader(inputStream, charsetName);
            while (true) {
                final int numRead = in.read(buffer, 0, buffer.length);
                if (numRead < 0) {
                    break;
                }
                sb.append(buffer, 0, numRead);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return sb.toString();
    }
}
