package com.zhyea.ar4j.core.dialect;


public interface Dialect {

    String defaultPrimaryKey();

    String sqlShowColumns();

    String sqlShowTables();
}
