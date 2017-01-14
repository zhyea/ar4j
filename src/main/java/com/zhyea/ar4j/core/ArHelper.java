package com.zhyea.ar4j.core;


import com.zhyea.ar4j.core.exception.ArException;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 辅助类，提供构建SQL语句、执行数据库操作、关闭连接等功能
 */
class ArHelper {


    static String buildInClause(Class<? extends Model> modelClass, String columnName, Collection<Object> values) {
        Class clazz = Ar.getColumnClass(modelClass, columnName);
        StringBuilder sql = new StringBuilder();
        sql.append(columnName).append(" in (");
        int count = 0;
        for (Object o : values) {
            if (!clazz.isPrimitive()) {
                String t = "'" + o + "'";
                sql.append(t);
            } else {
                sql.append(o);
            }
            if (count++ < values.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");
        return sql.toString();
    }


    static String buildGet(Class<? extends Model> modelClass, String tableName) {
        String primaryKey = Ar.getPrimaryKey(modelClass);
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(tableName).append(" where ").append(primaryKey).append(" = ?");
        return sql.toString();
    }


    static void buildBatchInsert(String tableName, Map<String, Object> attrs, StringBuilder sql) {
        sql.append("insert into ");
        sql.append(tableName).append(" (");
        StringBuilder tmp = new StringBuilder(") values (");
        int i = 0;
        for (String key : attrs.keySet()) {
            if (i++ > 0) {
                sql.append(", ");
                tmp.append(", ");
            }
            sql.append(key);
            tmp.append("?");
        }
        sql.append(tmp).append(")");
    }


    static Object[] genBatchParams(Map<String, Object> props) {
        List<Object> params = new ArrayList<Object>();
        for (String key : props.keySet()) {
            params.add(props.get(key));
        }
        return params.toArray();
    }


    static void buildInsert(String tableName, Map<String, Object> attrs, StringBuilder sql, List<Object> params) throws SQLException {
        sql.append("insert into ");
        sql.append(tableName).append(" (");
        StringBuilder tmp = new StringBuilder(") values (");
        for (String key : attrs.keySet()) {
            if (params.size() > 0) {
                sql.append(", ");
                tmp.append(", ");
            }
            sql.append(key);
            tmp.append("?");
            params.add(attrs.get(key));
        }
        sql.append(tmp).append(")");
    }


    static boolean executeInsert(Class<? extends Model> modelClass, Map<String, Object> attrs, PreparedStatement ps) throws SQLException {
        ResultSet rs = null;
        try {
            String primaryKey = Ar.getPrimaryKey(modelClass);
            Class primaryKeyClass = Ar.getPrimaryKeyClass(modelClass);
            int result = ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            Object generatedPrimaryKey;
            if (rs.next()) {
                if (primaryKeyClass.equals(Integer.class)) {
                    generatedPrimaryKey = rs.getInt(1);
                } else if (primaryKeyClass.equals(Long.class)) {
                    generatedPrimaryKey = rs.getLong(1);
                } else {
                    generatedPrimaryKey = rs.getObject(1);
                }
                attrs.put(primaryKey, generatedPrimaryKey);
            } else {
                throw new ArException("Not received generated primary key.");
            }
            return result >= 1;
        } finally {
            closeResultSet(rs);
        }
    }


    static void buildUpdate(String tableName, Map<String, Object> attrs, StringBuilder sql, List<Object> params, String primaryKey) throws SQLException {
        sql.append("update ");
        sql.append(tableName).append(" set ");
        for (String key : attrs.keySet()) {
            if (params.size() > 0) {
                sql.append(", ");
            }
            sql.append(key).append("=?");
            params.add(attrs.get(key));
        }
        sql.append(" where ").append(primaryKey).append("=?");
        params.add(attrs.get(primaryKey));
    }


    static String buildDeleteByPrimaryKey(Class<? extends Model> modelClass, String tableName) {
        String primaryKey = Ar.getPrimaryKey(modelClass);
        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(tableName)
                .append(" where ")
                .append(primaryKey)
                .append("=? ");
        return sql.toString();
    }


    static void prepare(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }


    static <M extends Model> List<M> obtainResult(Class<? extends Model> modelClass, ResultSet rs) throws SQLException, IllegalAccessException, InstantiationException {
        List<M> result = new ArrayList<M>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        while (rs.next()) {
            Model<?> tmp = modelClass.newInstance();
            for (int i = 1; i <= columnCount; i++) {
                String label = meta.getColumnLabel(i);
                tmp.getProps().put(label, rs.getObject(label));
            }
            result.add((M) tmp);
        }
        return result;
    }


    static void closePreparedStatement(PreparedStatement ps) {
        if (null != ps) {
            try {
                ResultSet rs = ps.getResultSet();
                closeResultSet(rs);
                ps.close();
            } catch (SQLException e) {
                throw new ArException("close PrepareStatement error.", e);
            }
        }
    }


    static void closeResultSet(ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new ArException("Close resultSet error", e);
            }
        }
    }


    static void closeConnection(Connection conn) {
        if (null != conn) {
            try {
                conn.close();
            } catch (Exception e) {
                throw new ArException("close Connection error.", e);
            }
        }
    }


    static void close(PreparedStatement ps, ResultSet rs) throws SQLException {
        if (null != ps) {
            ps.close();
        }
        if (null != rs) {
            rs.close();
        }
    }


    static void close(Connection conn, PreparedStatement ps, ResultSet rs) throws SQLException {
        if (null != conn) {
            conn.close();
        }
        close(ps, rs);
    }


    static Set<String> findTableNames(Class<? extends SeqModel> seqModelClass, String sql, String suffixRegex) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Set<String> tables = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        try {
            conn = Ar.getConnection(seqModelClass);
            String tablePrefix = Ar.getTableFlag(seqModelClass);
            sql = sql.replace("${TABLE_NAME}", tablePrefix);
            String regex = tablePrefix + suffixRegex;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String t = rs.getString(1);
                if (Pattern.matches(regex, t)) {
                    tables.add(t);
                }
            }
        } finally {
            close(conn, ps, rs);
        }
        return tables;
    }

}
