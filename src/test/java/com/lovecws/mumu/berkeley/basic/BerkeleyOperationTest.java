package com.lovecws.mumu.berkeley.basic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: berkeley db 增删改查基本操作
 * @date 2018-01-15 11:05:
 */
public class BerkeleyOperationTest {

    public BerkeleyOperation berkeleyOperation;

    @Before
    public void before() {
        BerkeleyDatabase berkeleyDatabase = new BerkeleyDatabase("testdatabase", "E:\\mumu\\berkeley\\testdatabase");
        berkeleyOperation = new BerkeleyOperation(berkeleyDatabase);
    }

    @After
    public void after() {
        berkeleyOperation.getBerkeleyDatabase().close();
    }

    @Test
    public void put() {
        berkeleyOperation.put("lover", "cws", true);
    }

    @Test
    public void batchPut() {
        Map<String, String> dataMap = new HashMap<String, String>();
        for (int i = 0; i < 1000000; i++) {
            dataMap.put("cws" + i, "5211315" + i);
        }
        berkeleyOperation.batchPut(dataMap, true);
    }

    @Test
    public void update() {
        berkeleyOperation.update("lover", "cws5211314");
    }

    @Test
    public void get() {
        berkeleyOperation.get("lover");
    }

    @Test
    public void list() {
        Map<String, String> stringMap = berkeleyOperation.list();
        System.out.println(stringMap);
    }

    @Test
    public void delete() {
        berkeleyOperation.delete("lover");
    }
}
