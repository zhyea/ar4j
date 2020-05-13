package org.chobit.ar4j.core.exception;

public class ArSQLException extends RuntimeException {

    private String sql;

    public ArSQLException(String sql, Throwable cause){
        super(sql, cause);
    }
}
