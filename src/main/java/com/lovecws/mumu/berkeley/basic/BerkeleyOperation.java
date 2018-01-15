package com.lovecws.mumu.berkeley.basic;

import com.sleepycat.je.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author babymm
 * @version 1.0-SNAPSHOT
 * @Description: berkeley db basi operation
 * @date 2018-01-15 10:37:
 */
public class BerkeleyOperation {

    private static final Logger log = Logger.getLogger(BerkeleyOperation.class);

    public BerkeleyDatabase berkeleyDatabase;

    public BerkeleyOperation(BerkeleyDatabase berkeleyDatabase) {
        this.berkeleyDatabase = berkeleyDatabase;
    }

    /**
     * 数据库写入数据
     *
     * @param key
     * @param value
     * @param overwrite
     * @return
     */
    public boolean put(String key, String value, boolean overwrite) {
        DatabaseEntry entryKey = new DatabaseEntry(key.getBytes());
        DatabaseEntry entryValue = new DatabaseEntry(value.getBytes());

        TransactionConfig transactionConfig = new TransactionConfig();
        transactionConfig.setReadCommitted(true);
        Transaction transaction = berkeleyDatabase.environment().beginTransaction(null, transactionConfig);

        Database database = berkeleyDatabase.database();
        OperationStatus operationStatus = null;
        if (overwrite) {
            operationStatus = database.put(transaction, entryKey, entryValue);
        } else {
            operationStatus = database.putNoOverwrite(transaction, entryKey, entryValue);
        }
        transaction.commit();
        if (operationStatus == OperationStatus.SUCCESS) {
            log.info("数据库" + database.getDatabaseName() + "中写入:" + key + "," + value);
            return true;
        } else if (operationStatus == OperationStatus.KEYEXIST) {
            log.info("数据库" + database.getDatabaseName() + "写入:" + key + "," + value + "失败,该值已经存在");
            return false;
        } else {
            log.info("数据库" + database.getDatabaseName() + "写入:" + key + "," + value + "失败");
            return false;
        }
    }

    /**
     * 批量添加数据
     *
     * @param dataMap
     * @param overwrite
     * @return
     */
    public boolean batchPut(Map<String, String> dataMap, boolean overwrite) {
        TransactionConfig transactionConfig = new TransactionConfig();
        transactionConfig.setReadCommitted(true);
        Transaction transaction = berkeleyDatabase.environment().beginTransaction(null, transactionConfig);

        boolean success = true;
        Database database = berkeleyDatabase.database();
        for (String key : dataMap.keySet()) {
            DatabaseEntry entryKey = new DatabaseEntry(key.getBytes());
            DatabaseEntry entryValue = new DatabaseEntry(dataMap.get(key).getBytes());
            OperationStatus operationStatus = null;
            if (overwrite) {
                operationStatus = database.put(transaction, entryKey, entryValue);
            } else {
                operationStatus = database.putNoOverwrite(transaction, entryKey, entryValue);
            }
            if (operationStatus != OperationStatus.SUCCESS) {
                success = false;
                transaction.abort();
                break;
            }
        }
        transaction.commit();
        return success;
    }

    /**
     * 更新
     *
     * @param key
     * @param value
     * @return
     */
    public boolean update(String key, String value) {
        return put(key, value, true);
    }

    /**
     * 从数据库中获取数据
     *
     * @param key
     * @return
     */
    public byte[] get(String key) {
        DatabaseEntry entryKey = new DatabaseEntry(key.getBytes());
        DatabaseEntry entryValue = new DatabaseEntry();

        Database database = berkeleyDatabase.database();
        OperationStatus operationStatus = database.get(null, entryKey, entryValue, LockMode.DEFAULT);

        if (operationStatus == OperationStatus.SUCCESS) {
            log.info("从数据库" + database.getDatabaseName() + "中读取:" + key + "," + new String(entryValue.getData()));
            return entryValue.getData();
        } else {
            log.info("No record found for key '" + key + "'.");
            return null;
        }
    }

    /**
     * 获取数据库的所有数据
     *
     * @return
     */
    public Map<String, String> list() {
        Cursor cursor = berkeleyDatabase.database().openCursor(null, CursorConfig.READ_COMMITTED);


        DatabaseEntry entryKey = new DatabaseEntry();
        DatabaseEntry entryValue = new DatabaseEntry();

        Map<String, String> dataMap = new HashMap<String, String>();

        while (cursor.getNext(entryKey, entryValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            dataMap.put(new String(entryKey.getData()), new String(entryValue.getData()));
        }
        cursor.close();
        return dataMap;
    }

    /**
     * 删除
     *
     * @param key
     * @return
     */
    public boolean delete(String key) {
        TransactionConfig transactionConfig = new TransactionConfig();
        transactionConfig.setReadCommitted(true);
        Transaction transaction = berkeleyDatabase.environment().beginTransaction(null, transactionConfig);

        Database database = berkeleyDatabase.database();
        OperationStatus operationStatus = database.delete(transaction, new DatabaseEntry(key.getBytes()));
        transaction.commit();

        if (operationStatus == OperationStatus.SUCCESS) {
            System.out.println("数据库" + database.getDatabaseName() + "中删除:" + key);
            return true;
        } else if (operationStatus == OperationStatus.KEYEMPTY) {
            System.out.println("没有从数据库" + database.getDatabaseName() + "中找到:" + key + "。无法删除");
        } else {
            System.out.println("删除操作失败，由于" + operationStatus.toString());
        }
        return false;
    }

    public BerkeleyDatabase getBerkeleyDatabase() {
        return berkeleyDatabase;
    }

    public void setBerkeleyDatabase(BerkeleyDatabase berkeleyDatabase) {
        this.berkeleyDatabase = berkeleyDatabase;
    }
}
