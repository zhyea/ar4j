package com.zhyea.ar4j.core.exception;


import com.zhyea.ar4j.core.Model;

public class TableNotFoundException extends RuntimeException {

    public TableNotFoundException(Class<? extends Model> modelClass) {
        super(String.format("Table not found. Model class is %s", modelClass.getCanonicalName()));
    }
    public TableNotFoundException(Class<? extends Model> modelClass, Throwable cause) {
        super(String.format("Table not found. Model class is %s", modelClass.getCanonicalName()), cause);
    }
}
