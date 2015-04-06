package org.hildan.utils.io.binary;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.hildan.utils.io.binary.BitOutputStream;
import org.junit.Test;

public class TestBitOutputStream {

    private static final String TEMP_FILE_OUT = System.getProperty("user.home") + "/temp_test_out";
        
    @Test
    public void testOutputSream() throws IOException {
        try (BitOutputStream out = new BitOutputStream(new FileOutputStream(TEMP_FILE_OUT))) {
            out.writeByte((byte) 0xD3);
            out.writeByte((byte) 0x00);
            out.writeByte((byte) 0xFF);
            out.writeBits(0x0, 4);
            out.writeBits(0x5, 4);
            out.writeChar((char) 0x0AF0);
            out.writeInt(0xABCDEFFF);
            out.writeLong(0x0123456789ABCDEFL);
            out.writeBits(0x0, 3); // 0x03..
            out.writeBits(0x11, 5);
            out.writeBits(0x0, 2); // 0x21..
            out.writeBits(0x21, 6);
            out.writeBits(0x0, 3); // 0x03..
            out.writeBit(0);
            out.writeBit(0);
            out.writeBit(0);
            out.writeBit(1);
            out.writeBit(1);
            out.writeString("00111010"); // 0x3A
            out.writeString("101000"); // 0xA3..
            out.writeBits(0b111011, 6); // 0xB4..
            out.writeBit(0);
            out.writeBit(1);
            out.writeLong(0x0FFFFFFFFFFFFFFFL); // 0x3F FF FF FF FF FF FF FC
            out.writeBit(0);
            out.writeBit(0);
        }
        try (InputStream in = new BufferedInputStream(new FileInputStream(TEMP_FILE_OUT))) {
            assertEquals(0xD3, in.read());
            assertEquals(0x00, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0x05, in.read());
            assertEquals(0x0A, in.read());
            assertEquals(0xF0, in.read());
            assertEquals(0xAB, in.read());
            assertEquals(0xCD, in.read());
            assertEquals(0xEF, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0x01, in.read());
            assertEquals(0x23, in.read());
            assertEquals(0x45, in.read());
            assertEquals(0x67, in.read());
            assertEquals(0x89, in.read());
            assertEquals(0xAB, in.read());
            assertEquals(0xCD, in.read());
            assertEquals(0xEF, in.read());
            assertEquals(0x11, in.read());
            assertEquals(0x21, in.read());
            assertEquals(0x03, in.read());
            assertEquals(0x3A, in.read());
            assertEquals(0xA3, in.read());
            assertEquals(0xB4, in.read());
            assertEquals(0x3F, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0xFF, in.read());
            assertEquals(0xFC, in.read());
            assertEquals(-1, in.read()); // EOF
        }
        new File(TEMP_FILE_OUT).delete();
    }
}
