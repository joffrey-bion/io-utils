package org.hildan.utils.io.extractor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * An {@code Extractor} provides methods to parse easily a text file. It is especially useful when
 * parsing an HTML page containing a table.
 */
public abstract class Extractor {

    private final String source;

    private final BufferedReader reader;

    private boolean eof;

    private String line;

    /**
     * Creates a new Extractor for the specified resource file.
     *
     * @param resourceFile
     *            The file to parse.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public Extractor(String resourceFile) throws IOException {
        this.source = resourceFile;
        final InputStream is = getClass().getResourceAsStream(source);
        if (is == null) {
            throw new FileNotFoundException("Couldn't find the file " + source);
        }
        reader = new BufferedReader(new InputStreamReader(is));
        eof = false;
        nextLine();
    }

    /**
     * Calls {@link BufferedReader#readLine()} on the current {@link #reader}, and updates EOF flag.
     *
     * @return {@code true} if a line was actually read, {@code false} if the end of the file was
     *         reached.
     * @throws IOException
     *             If an I/O error occurs. When this happens, the {@link #reader} is properly closed
     *             before throwing this exception again.
     */
    private boolean nextLine() throws IOException {
        try {
            line = reader.readLine();
            if (line == null) {
                eof = true;
            }
            return !eof;
        } catch (final IOException e) {
            try {
                reader.close();
            } catch (final IOException ignore) {
            }
            throw e;
        }
    }

    /**
     * Returns whether the end of the file has been reached.
     *
     * @return {@code true} if the end of the file has been reached, meaning that there are no more
     *         lines to read.
     */
    public boolean isEofReached() {
        return eof;
    }

    /**
     * Extracts the content between the next occurrence of {@code prefix} and the next occurrence of
     * {@code suffix}. The prefix and the suffix have to be on the same line. If the content is not
     * found in the current line, the next line is read and so on until the end of the file.
     *
     * @param prefix
     *            The {@link String} preceding the part that has to be extracted.
     * @param suffix
     *            The {@link String} following the part that has to be extracted.
     * @return The content between {@code prefix} and {@code suffix}, or {@code null} if no
     *         occurrence is found in the remaining lines of the file.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public String extractNextBetween(String prefix, String suffix) throws IOException {
        return extractNextBetween(prefix, suffix, true);
    }

    /**
     * Extracts the content between the next occurrence of {@code prefix} and the next occurrence of
     * {@code suffix}. The prefix and the suffix have to be on the same line. If the content is not
     * found in the current line and {@code searchNextLines} is {@code true}, the next line is read
     * and so on until the end of the file.
     *
     * @param prefix
     *            The {@link String} preceding the part that has to be extracted.
     * @param suffix
     *            The {@link String} following the part that has to be extracted.
     * @param searchNextLines
     *            Whether or not to search the next lines if the prefix and suffix are not found in
     *            the current line.
     * @return The content between {@code prefix} and {@code suffix}, or {@code null} if no
     *         occurrence is found in the remaining lines of the file.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public String extractNextBetween(String prefix, String suffix, boolean searchNextLines) throws IOException {
        String result = null;
        while ((result = currentLineExtractBetween(prefix, suffix)) == null) {
            if (!searchNextLines) {
                return null;
            } else {
                if (!nextLine()) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Extracts the content between the next occurrence of {@code prefix} and the end of the line.
     * If the prefix is not found in the current line, the next line is read and so on until the end
     * of the file.
     *
     * @param prefix
     *            The {@link String} preceding the part that has to be extracted.
     * @return The content between {@code prefix} and the end of the line, or {@code null} if no
     *         occurrence of {@code prefix} is found in the remaining lines of the file.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public String extractNextAfter(String prefix) throws IOException {
        return extractNextAfter(prefix, true);
    }

    /**
     * Extracts the content between the next occurrence of {@code prefix} and the end of the line.
     * If the prefix is not found in the current line and {@code searchNextLines} is {@code true},
     * the next line is read and so on until the end of the file.
     *
     * @param prefix
     *            The {@link String} preceding the part that has to be extracted.
     * @param searchNextLines
     *            Whether or not to search the next lines if the prefix is not found in the current
     *            line.
     * @return The content between {@code prefix} and the end of the line, or {@code null} if no
     *         occurrence of {@code prefix} is found in the remaining lines of the file.
     * @throws IOException
     *             If an I/O error occurs.
     */
    public String extractNextAfter(String prefix, boolean searchNextLines) throws IOException {
        String result = null;
        ;
        while ((result = currentLineExtractAfter(prefix)) == null) {
            if (!searchNextLines) {
                return null;
            } else {
                if (!nextLine()) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Extracts the content between the next occurrence of {@code prefix} and the end of the current
     * line. If {@code prefix} is found, then this method consumes the current line.
     *
     * @param prefix
     *            The {@link String} preceding the part that has to be extracted.
     * @return The content between {@code prefix} and the end of the current line, or {@code null}
     *         if {@code prefix} is not found in the current line.
     */
    private String currentLineExtractAfter(String prefix) {
        if (line == null) {
            return null;
        }
        final int i = line.indexOf(prefix);
        if (i == -1) {
            return null;
        }
        final String res = line.substring(i + prefix.length());
        // consumes the end of the line
        line = "";
        return res;
    }

    /**
     * Extracts the content between the next occurrence of {@code prefix} and the next occurrence of
     * {@code suffix}. If {@code prefix} and {@code suffix} are found, then this method consumes the
     * current line up to the end of the extracted content (this corresponds to the beginning of
     * {@code suffix}).
     *
     * @param prefix
     *            The {@link String} preceding the part that has to be extracted.
     * @param suffix
     *            The {@link String} following the part that has to be extracted.
     * @return The content between {@code prefix} and {@code suffix}, or {@code null} if one of
     *         {@code prefix} and {@code suffix} is not found in the current line.
     */
    private String currentLineExtractBetween(String prefix, String suffix) {
        line = currentLineExtractAfter(prefix);
        if (line == null) {
            return null;
        }
        final int i = line.indexOf(suffix);
        final String res = line.substring(0, i);
        // consumes the line up to the end of the returned part
        line = line.substring(i);
        return res;
    }
}
