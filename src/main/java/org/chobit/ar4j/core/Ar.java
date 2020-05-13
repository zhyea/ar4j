package org.chobit.ar4j.core;


import org.chobit.ar4j.core.dialect.Dialect;
import org.chobit.ar4j.core.exception.ArConfigException;
import org.chobit.ar4j.core.exception.DataSourceNotFoundException;
import org.chobit.ar4j.core.exception.ModelUnRegisterException;
import org.chobit.ar4j.core.exception.TableNotFoundException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 另一个配置类。用于维护Model类和ArConfig的逆向关系。主要提供通过model类获取表和数据源等信息的功能。
 */
class Ar {

    private final static Map<Class<? extends Model>, ArConfig> model2Config = new HashMap<Class<? extends Model>, ArConfig>();

    static void regModel2Config(final Class<? extends Model> modelClass, final ArConfig config) {
        if (null == modelClass) {
            throw new IllegalArgumentException("modelClass cannot be null!");
        }
        if (null == config) {
            throw new IllegalArgumentException("config cannot be null!");
        }
        model2Config.put(modelClass, config);
    }


    private static ArConfig getConfig(Class<? extends Model> modelClass) {
        return model2Config.get(modelClass);
    }


    static Connection getConnection(Class<? extends Model> modelClass) {
        ArConfig config = getConfig(modelClass);
        if (null == config) {
            throw new ModelUnRegisterException(modelClass);
        }
        DataSource dataSource = config.getDataSource();
        if (null != dataSource) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new ArConfigException(e);
            }
        } else {
            throw new DataSourceNotFoundException(modelClass);
        }
    }


    static Dialect getDialect(Class<? extends Model> modelClass) {
        ArConfig config = getConfig(modelClass);
        if (null == config) {
            throw new ModelUnRegisterException(modelClass);
        }
        Dialect dialect = config.getDialect();
        if (null != dialect) {
            return dialect;
        } else {
            throw new ArConfigException("none dialect found");
        }
    }

    static String getTableFlag(Class<? extends Model> modelClass) {
        ArConfig config = getConfig(modelClass);
        if (null == config) {
            throw new ModelUnRegisterException(modelClass);
        }
        Table table = config.getTable(modelClass);
        if (null != table) {
            return table.getTableFlag();
        } else {
            throw new TableNotFoundException(modelClass);
        }
    }


    static String getPrimaryKey(Class<? extends Model> modelClass) {
        ArConfig config = getConfig(modelClass);
        if (null == config) {
            throw new ModelUnRegisterException(modelClass);
        }
        Table table = config.getTable(modelClass);
        if (null != table) {
            return table.getPrimaryKey();
        } else {
            throw new TableNotFoundException(modelClass);
        }
    }

    static Class getPrimaryKeyClass(Class<? extends Model> modelClass) {
        ArConfig config = getConfig(modelClass);
        if (null == config) {
            throw new ModelUnRegisterException(modelClass);
        }
        Table table = config.getTable(modelClass);
        if (null != table) {
            return table.getPrimaryKeyClass();
        } else {
            throw new TableNotFoundException(modelClass);
        }
    }


    static Class getColumnClass(Class<? extends Model> modelClass, String columnName) {
        ArConfig config = getConfig(modelClass);
        if (null == config) {
            throw new ModelUnRegisterException(modelClass);
        }
        Table table = config.getTable(modelClass);
        if (null == table) {
            throw new TableNotFoundException(modelClass);
        }
        Class clazz = table.getColumnClass(columnName);
        if (null == clazz) {
            throw new ArConfigException("column not found");
        }
        return table.getPrimaryKeyClass();
    }
}
