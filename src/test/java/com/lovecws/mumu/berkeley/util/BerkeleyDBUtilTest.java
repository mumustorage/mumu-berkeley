package com.lovecws.mumu.berkeley.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BerkeleyDBUtilTest {

    private BerkeleyDBUtil dbUtil = null;

    @Before
    public void setup() {
        dbUtil = new BerkeleyDBUtil("E:/mumu/berkeley");
    }

    @Test
    public void testWriteToDatabase() {
        for (int i = 0; i < 10; i++) {
            dbUtil.writeToDatabase(i + "", "学生" + i, true);
        }
    }

    @Test
    public void testReadFromDatabase() {
        String value = dbUtil.readFromDatabase("2");
        assertEquals(value, "学生2");
    }

    @Test
    public void testGetEveryItem() {
        int size = dbUtil.getEveryItem().size();
        assertEquals(size, 10);
    }

    @Test
    public void testDeleteFromDatabase() {
        dbUtil.deleteFromDatabase("4");
        assertEquals(9, dbUtil.getEveryItem().size());
    }

    public void cleanup() {
        dbUtil.closeDB();
    }

}

