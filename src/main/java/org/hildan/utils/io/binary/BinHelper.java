package org.hildan.utils.io.binary;

public class BinHelper {

    /**
     * Appends as many 0s as necessary to the front of {@code str} to make it reach the specified
     * {@code length}.
     *
     * @param str
     *            The {@code String} to complete.
     * @param length
     *            The length to reach.
     * @return A {@code String} of the specified {@code length}, ending with {@code str} and
     *         starting with zeros.
     */
    public static String addLeadingZeros(String str, int length) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - str.length(); i++) {
            sb.append("0");
        }
        sb.append(str);
        return sb.toString();
    }

}
