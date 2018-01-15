package com.lovecws.mumu.berkeley.basic;

import com.sleepycat.je.Database;
import com.sleepycat.je.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: berkeley db数据库创建
 * @date 2018-01-15 10:31:
 */
public class BerkeleyDatabaseTest {

    public BerkeleyDatabase berkeleyDatabase;

    @Before
    public void before() {
        berkeleyDatabase = new BerkeleyDatabase("testdatabase", "E:\\mumu\\berkeley\\testdatabase");
    }

    @After
    public void after() {
        berkeleyDatabase.close();
    }

    @Test
    public void createDatabase() {
        Database database = berkeleyDatabase.database();
        System.out.println(database.count());
    }

    @Test
    public void database() {
        Database database = berkeleyDatabase.database();
        System.out.println(database);
    }

    @Test
    public void environment() {
        Environment environment = berkeleyDatabase.environment();
        System.out.println(environment);
    }

    @Test
    public void removeDatabase() {
        berkeleyDatabase.removeDatabase();
    }
}
