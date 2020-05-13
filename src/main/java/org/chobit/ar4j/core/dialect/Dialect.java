package org.chobit.ar4j.core.dialect;


public interface Dialect {

    String defaultPrimaryKey();

    String sqlShowColumns();

    String sqlShowTables();
}
