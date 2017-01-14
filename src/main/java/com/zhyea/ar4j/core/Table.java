package com.zhyea.ar4j.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录表的一些元数据
 */
class Table {

    private Class<? extends Model> modelClass;

    private String tableFlag;

    private String primaryKey;

    private Map<String, Class> column2Classes = new HashMap<String, Class>();

    Table(Class<? extends Model> modelClass, String tableFlag, String primaryKey) {
        this.modelClass = modelClass;
        this.tableFlag = tableFlag;
        this.primaryKey = primaryKey;
    }

    String getTableFlag() {
        return this.tableFlag;
    }

    Class getModelClass() {
        return modelClass;
    }

    String getPrimaryKey() {
        return this.primaryKey;
    }

    Class getPrimaryKeyClass() {
        return this.column2Classes.get(this.primaryKey);
    }

    Class getColumnClass(String columnName) {
        return this.column2Classes.get(columnName);
    }

    void addColumnClass(String column, Class columnClass) {
        this.column2Classes.put(column, columnClass);
    }

}
