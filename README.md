## mumu-berkeley 嵌入式k/V数据库

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/mumustorage/mumu-berkeley/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/mumustorage/mumu-kite.svg?branch=master)](https://travis-ci.org/mumustorage/mumu-berkeley)
[![codecov](https://codecov.io/gh/mumustorage/mumu-berkeley/branch/master/graph/badge.svg)](https://codecov.io/gh/mumustorage/mumu-berkeley)

mumu-berkeley是一个demo项目，主要通过这个项目学习和了解berkeley db的使用方式和实现原理。berkeley db是一个key/value 数据库(key、value可以是任意类型)，而且同一个key可以存储不能的值。berkeleydb是一个内嵌入式数据库，是一个简单、
小巧、可靠、高性能，但是功能丰富多彩的嵌入式数据库。

## DB的设计思想
DB的设计思想是简单、小巧、可靠、高性能。如果说一些主流数据库系统是大而全的话，那么DB就可称为小而精。DB提供了一系列应用程序接口（API），调用本身很简单，应用程序和DB所提供的库在一起编译成为可执行程序。这种方式从两方面极大提高了DB的效率。第一：DB库和应用程序运行在同一个地址空间，没有客户端程序和数据库服务器之间昂贵的网络通讯开销，也没有本地主机进程之间的通讯；第二：不需要对SQL代码解码，对数据的访问直截了当。
DB对需要管理的数据看法很简单，DB数据库包含若干条记录，每一个记录由关键字和数据（KEY/VALUE）构成。数据可以是简单的数据类型，也可以是复杂的数据类型，例如C语言中结构。DB对数据类型不做任何解释, 完全由程序员自行处理，典型的C语言指针的"自由"风格。如果把记录看成一个有n个字段的表，那么第1个字段为表的主键，第2--n个字段对应了其它数据。DB应用程序通常使用多个DB数据库，从某种意义上看，也就是关系数据库中的多个表。DB库非常紧凑，不超过500K，但可以管理大至256T的数据量。
DB的设计充分体现了UNIX的基于工具的哲学，即若干简单工具的组合可以实现强大的功能。DB的每一个基础功能模块都被设计为独立的,也即意味着其使用领域并不局限于DB本身。例如加锁子系统可以用于非DB应用程序的通用操作，内存共享缓冲池子系统可以用于在内存中基于页面的文件缓冲。

## DB核心数据结构
数据库句柄结构DB：包含了若干描述数据库属性的参数，如数据库访问方法类型、逻辑页面大小、数据库名称等；同时，DB结构中包含了大量的数据库处理函数指针，大多数形式为 （*dosomething）(DB *, arg1, arg2, …)。其中最重要的有open,close,put,get等函数。
数据库记录结构DBT：DB中的记录由关键字和数据构成，关键字和数据都用结构DBT表示。实际上完全可以把关键字看成特殊的数据。结构中最重要的两个字段是 void * data和u_int32_t size，分别对应数据本身和数据的长度。
数据库游标结构DBC：游标（cursor）是数据库应用中常见概念，其本质上就是一个关于特定记录的遍历器。注意到DB支持多重记录（duplicate records），即多条记录有相同关键字，在对多重记录的处理中，使用游标是最容易的方式。
数据库环境句柄结构DB_ENV：环境在DB中属于高级特性，本质上看，环境是多个数据库的包装器。当一个或多个数据库在环境中打开后，环境可以为这些数据库提供多种子系统服务，例如多线/进程处理支持、事务处理支持、高性能支持、日志恢复支持等。
DB中核心数据结构在使用前都要初始化，随后可以调用结构中的函数（指针）完成各种操作，最后必须关闭数据结构。从设计思想的层面上看，这种设计方法是利用面向过程语言实现面对对象编程的一个典范。

## DB数据访问算法
在数据库领域中,数据访问算法对应了数据在硬盘上的存储格式和操作方法。在编写应用程序时，选择合适的算法可能会在运算速度上提高1个甚至多个数量级。大多数数据库都选用B+树算法，DB也不例外，同时还支持HASH算法、Recno算法和Queue算法。接下来，我们将讨论这些算法的特点以及如何根据需要存储数据的特点进行选择。
B+树算法：B+树是一个平衡树，关键字有序存储，并且其结构能随数据的插入和删除进行动态调整。为了代码的简单，DB没有实现对关键字的前缀码压缩。B+树支持对数据查询、插入、删除的常数级速度。关键字可以为任意的数据结构。
HASH算法：DB中实际使用的是扩展线性HASH算法（extended linear hashing），可以根据HASH表的增长进行适当的调整。关键字可以为任意的数据结构。
Recno算法： 要求每一个记录都有一个逻辑纪录号，逻辑纪录号由算法本身生成。实际上，这和关系型数据库中逻辑主键通常定义为int AUTO型是同一个概念。Recho建立在B+树算法之上，提供了一个存储有序数据的接口。记录的长度可以为定长或不定长。
Queue算法：和Recno方式接近, 只不过记录的长度为定长。数据以定长记录方式存储在队列中，插入操作把记录插入到队列的尾部，相比之下插入速度是最快的。
对算法的选择首先要看关键字的类型，如果为复杂类型，则只能选择B+树或HASH算法，如果关键字为逻辑记录号，则应该选择Recno或Queue算法。当工作集关键字有序时，B+树算法比较合适；如果工作集比较大且基本上关键字为随机分布时，选择HASH算法。Queue算法只能存储定长的记录，在高的并发处理情况下，Queue算法效率较高；如果是其它情况，则选择Recno算法，Recno算法把数据存储为平面文件格式。

DB是一个具有工业强度的嵌入式数据库系统，数据处理的效率很高。DB功能的稳定性历经时间的考验，在大量应用程序中使用便是明证。可以想见，在同等代码质量的条件下，软件的BUG数和代码的长度是成正比的，相对几十兆、几百兆大型数据库软件，DB的只有不到500K的大小！


## DB 基本操作
### 创建数据库
```
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
```

### 添加数据
```
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
```

### 获取数据
```
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
```

### 数据列表
```
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
```

### 删除数据
```
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
```
## 相关阅读

[嵌入式数据库系统Berkeley DB](https://www.ibm.com/developerworks/cn/linux/l-embdb/index.html)


## 联系方式

以上观点纯属个人看法，如有不同，欢迎指正。

email:<babymm@aliyun.com>

github:[https://github.com/babymm](https://github.com/babymm)