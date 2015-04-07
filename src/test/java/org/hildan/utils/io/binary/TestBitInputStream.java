package org.hildan.utils.io.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBitInputStream {

    private static final String TEMP_FILE_IN_LONG = System.getProperty("user.home") + "/temp_test_in_long";

    private static final String TEMP_FILE_IN_SHORT = System.getProperty("user.home") + "/temp_test_in_short";

    private BitInputStream bisLong;

    private BitInputStream bisShort;

    @BeforeClass
    public static void initTestFiles() throws IOException {
        try (OutputStream out = new FileOutputStream(TEMP_FILE_IN_LONG)) {
            out.write(0x00);
            out.write(0xFF);
            out.write(0x05);
            out.write(0x0A);
            out.write(0xF0);
            out.write(0xAB);
            out.write(0xCD);
            out.write(0xEF);
            out.write(0xFF);
            out.write(0x01);
            out.write(0x23);
            out.write(0x45);
            out.write(0x67);
            out.write(0x89);
            out.write(0xAB);
            out.write(0xCD);
            out.write(0xEF);
            out.write(0x11);
            out.write(0x21);
            out.write(0x03);
            out.write(0x3A);
        }
        try (OutputStream out = new FileOutputStream(TEMP_FILE_IN_SHORT)) {
            out.write(0x3A);
            out.write(0xEF);
            out.write(0xFF);
            out.write(0x01);
            out.write(0x23);
            out.write(0x45);
            out.write(0x67);
            out.write(0x89);
            out.write(0xAB);
        }
    }

    @AfterClass
    public static void deleteTestFiles() {
        new File(TEMP_FILE_IN_LONG).delete();
        new File(TEMP_FILE_IN_SHORT).delete();
    }

    @Before
    public void initInputStreams() throws FileNotFoundException {
        bisLong = new BitInputStream(new FileInputStream(TEMP_FILE_IN_LONG));
        bisShort = new BitInputStream(new FileInputStream(TEMP_FILE_IN_SHORT));
    }

    @After
    public void closeInputStreams() throws IOException {
        bisLong.close();
        bisShort.close();
    }

    @Test
    public void testPrimitiveRead() throws IOException {
        assertEquals((byte) 0x00, bisLong.readByte());
        assertEquals((byte) 0xFF, bisLong.readByte());
        assertEquals(0x0, bisLong.readBits(4));
        assertEquals(0x5, bisLong.readBits(4));
        assertEquals((char) 0x0AF0, bisLong.readChar());
        assertEquals(0xABCDEFFF, bisLong.readInt());
        assertEquals(0x0123456789ABCDEFL, bisLong.readLong());
        assertEquals(0x0, bisLong.readBits(3));
        assertEquals(0x11, bisLong.readBits(5));
        assertEquals(0x0, bisLong.readBits(2));
        assertEquals(0x21, bisLong.readBits(6));
        assertEquals(0x0, bisLong.readBits(3));
        assertEquals(0, bisLong.readBit(), 0);
        assertEquals(0, bisLong.readBit(), 0);
        assertEquals(0, bisLong.readBit(), 0);
        assertEquals(1, bisLong.readBit(), 1);
        assertEquals(1, bisLong.readBit(), 1);
        assertEquals("00111010", bisLong.readBitsAsString(8));
        assertEquals(-1, bisLong.readBit());
        try {
            bisLong.readBoolean();
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
        try {
            bisLong.readByte();
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
        try {
            bisLong.readChar();
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
        try {
            bisLong.readInt();
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
        try {
            bisLong.readLong();
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
        try {
            bisLong.readBits(1);
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
        try {
            bisLong.readBits(5);
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
        try {
            bisLong.readBitsAsString(5);
            fail();
        } catch (final IllegalStateException e) {
            // OK
        }
    }

    @Test
    public void testObjectRead() throws IOException {
        assertEquals((byte) 0x00, (byte) bisLong.readByteOrNull());
        assertEquals((byte) 0xFF, (byte) bisLong.readByteOrNull());
        assertEquals(0x0, bisLong.readBits(4));
        assertEquals(0x5, bisLong.readBits(4));
        assertEquals((char) 0x0AF0, (char) bisLong.readCharacter());
        assertEquals(0xABCDEFFF, (int) bisLong.readInteger());
        assertEquals(0x0123456789ABCDEFL, (long) bisLong.readLongOrNull());
        assertEquals(0x0, bisLong.readBits(3));
        assertEquals(0x11, bisLong.readBits(5));
        assertEquals(0x0, bisLong.readBits(2));
        assertEquals(0x21, bisLong.readBits(6));
        assertEquals(0x0, bisLong.readBits(3));
        assertEquals(0, bisLong.readBit(), 0);
        assertEquals(0, bisLong.readBit(), 0);
        assertEquals(0, bisLong.readBit(), 0);
        assertEquals(1, bisLong.readBit(), 1);
        assertEquals(1, bisLong.readBit(), 1);
        assertEquals("00111010", bisLong.readBitsAsString(8));
        assertEquals(-1, bisLong.readBit());
        assertEquals(null, bisLong.readBooleanOrNull());
        assertEquals(null, bisLong.readByteOrNull());
        assertEquals(null, bisLong.readCharacter());
        assertEquals(null, bisLong.readInteger());
        assertEquals(null, bisLong.readLongOrNull());
    }
}
