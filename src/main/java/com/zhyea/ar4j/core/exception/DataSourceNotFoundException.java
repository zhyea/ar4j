package com.zhyea.ar4j.core.exception;


import com.zhyea.ar4j.core.Model;

public class DataSourceNotFoundException extends RuntimeException {

    public DataSourceNotFoundException(Class<? extends Model> modelClass) {
        super(String.format("DataSource not found. ArConfig error. Model class is %s", modelClass.getCanonicalName()));
    }

}
