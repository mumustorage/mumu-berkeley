package com.lovecws.mumu.berkeley.basic;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import java.io.File;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: Berkeley基本操作
 * @date 2018-01-12 14:17:
 */
public class BerkeleyDatabase {

    private Database database;
    private String databaseName;
    private Environment environment;

    public BerkeleyDatabase(String databaseName, String homeDirectory) {
        this.databaseName = databaseName;

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);
        environmentConfig.setCacheSize(100000000);

        File file = new File(homeDirectory);
        if (!file.exists()) {
            file.mkdirs();
        }
        environment = new Environment(file, environmentConfig);

        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setAllowCreate(true);
        databaseConfig.setTransactional(true);

        environment.printStartupInfo(System.out);

        database = environment.openDatabase(null, this.databaseName, databaseConfig);
    }

    /**
     * 删除数据库
     *
     * @return
     */
    public boolean removeDatabase() {
        environment.removeDatabase(null, databaseName);

        return true;
    }

    public Database database() {
        return database;
    }

    public Environment environment() {
        return environment;
    }

    public void close() {
        if (database != null) {
            database.close();
        }
        if (environment != null) {
            environment.close();
        }
    }
}
