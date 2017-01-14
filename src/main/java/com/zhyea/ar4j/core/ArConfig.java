package com.zhyea.ar4j.core;


import com.zhyea.ar4j.core.datasource.DataSourcePlugin;
import com.zhyea.ar4j.core.dialect.Dialect;
import com.zhyea.ar4j.core.exception.ArConfigException;

import javax.sql.DataSource;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置类，用于维护DataSource、Dialect、Table和Model类的正向关系
 */
public class ArConfig {

    private String id;

    private DataSourcePlugin dsp;

    private Dialect dialect;

    private Map<Class<? extends Model>, Table> tables = new HashMap<Class<? extends Model>, Table>();

    public ArConfig(String id, DataSourcePlugin dsp, Dialect dialect) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (null == dsp) {
            throw new IllegalArgumentException("dsp cannot be null");
        }
        if (null == dialect) {
            throw new IllegalArgumentException("dialect cannot be null");
        }
        this.id = id;
        this.dsp = dsp;
        this.dialect = dialect;
    }

    DataSource getDataSource() {
        return dsp.getDataSource();
    }

    Dialect getDialect() {
        return this.dialect;
    }

    Table getTable(Class<? extends Model> modelCalss) {
        return this.tables.get(modelCalss);
    }

    public void regTable(Class<? extends Model> modelCalss) {
        String tableFlag = classToTableFlag(modelCalss);
        regTable(modelCalss, new Table(modelCalss, tableFlag, this.dialect.defaultPrimaryKey()));
    }

    public void regTable(String tableFlag, Class<? extends Model> modelCalss) {
        regTable(modelCalss, new Table(modelCalss, tableFlag, this.dialect.defaultPrimaryKey()));
    }

    public void regTable(Class<? extends Model> modelCalss, String primaryKey) {
        String tableFlag = classToTableFlag(modelCalss);
        regTable(modelCalss, new Table(modelCalss, tableFlag, primaryKey));
    }

    public void regTable(String tableFlag, Class<? extends Model> modelCalss, String primaryKey) {
        regTable(modelCalss, new Table(modelCalss, tableFlag, primaryKey));
    }

    private void regTable(Class<? extends Model> modelCalss, Table table) {
        if (Modifier.isAbstract(modelCalss.getModifiers())) {
            throw new ArConfigException("Model class cannot be abstract.");
        }
        Ar.regModel2Config(modelCalss, this);
        this.tables.put(modelCalss, table);
        obtainTableColumns(table);
    }


    private void obtainTableColumns(Table table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = this.dialect.sqlShowColumns().replace("${TABLE_NAME}", getTableName(table));
        try {
            conn = getDataSource().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String columnName = meta.getColumnName(i);
                String columnClassName = meta.getColumnClassName(i);
                table.addColumnClass(columnName, Class.forName(columnClassName));
            }
        } catch (Exception e) {
            throw new ArConfigException(e);
        } finally {
            try {
                ArHelper.close(conn, ps, rs);
            } catch (SQLException e) {
                throw new ArConfigException(e);
            }
        }
    }

    private String getTableName(Table table) {
        try {
            Class<? extends Model> modelClass = table.getModelClass();
            if (SeqModel.class.isAssignableFrom(modelClass)) {
                SeqModel m = (SeqModel) modelClass.newInstance();
                return table.getTableFlag() + m.latestSuffix();
            }
            return table.getTableFlag();
        } catch (Exception e) {
            throw new ArConfigException("get table name error", e);
        }
    }


    private String classToTableFlag(Class<? extends Model> clazz) {
        String s = clazz.getSimpleName().replaceAll("[A-Z]", "_$0").toLowerCase();
        if (s.startsWith("_")) {
            s = s.substring(1);
        }
        return s;
    }

    private static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
