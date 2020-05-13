package org.chobit.ar4j.core;


import org.chobit.ar4j.core.cache.Cache;
import org.chobit.ar4j.core.exception.ArException;
import org.chobit.ar4j.core.exception.ArSQLException;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Model类，封装了一系列对表的操作方法
 *
 * @param <M>
 */
public abstract class Model<M extends Model> {

    private final Map<String, Object> props = new TreeMap<String, Object>();


    Map<String, Object> getProps() {
        return this.props;
    }


    public M set(String attr, Object value) {
        this.props.put(attr, value);
        return (M) this;
    }


    public void batchSave(Collection<M> models) {
        if (null == models || models.isEmpty()) return;
        Connection conn = null;
        try {
            conn = Ar.getConnection(getClass());
            StringBuilder sql = new StringBuilder();
            String tableName = getTableName();
            M m = models.iterator().next();
            ArHelper.buildBatchInsert(tableName, m.getProps(), sql);
            batchSave(conn, sql.toString(), models);
        } catch (Exception e) {
            throw new ArSQLException("batch insert error", e);
        } finally {
            ArHelper.closeConnection(conn);
        }
    }


    private void batchSave(Connection conn, String sql, Collection<M> models) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for (M m : models) {
                Object[] params = ArHelper.genBatchParams(m.getProps());
                ArHelper.prepare(ps, params);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            ArHelper.closePreparedStatement(ps);
        }
    }


    public boolean save() {
        Connection conn = null;
        StringBuilder sql = new StringBuilder();
        try {
            conn = Ar.getConnection(getClass());
            List<Object> params = new ArrayList<Object>();
            String tableName = getTableName();
            ArHelper.buildInsert(tableName, getProps(), sql, params);
            return save(conn, sql.toString(), params);
        } catch (Exception e) {
            throw new ArSQLException(sql.toString(), e);
        } finally {
            ArHelper.closeConnection(conn);
        }
    }


    private boolean save(Connection conn, String sql, List<Object> params) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ArHelper.prepare(ps, params.toArray());
            return ArHelper.executeInsert(getClass(), getProps(), ps);
        } finally {
            ArHelper.closePreparedStatement(ps);
        }
    }


    public boolean update() {
        Connection conn = null;
        StringBuilder sql = new StringBuilder();
        try {
            conn = Ar.getConnection(getClass());
            return update(conn, sql);
        } catch (Exception e) {
            throw new ArSQLException(sql.toString(), e);
        } finally {
            ArHelper.closeConnection(conn);
        }
    }


    private boolean update(Connection conn, StringBuilder sql) throws SQLException {
        PreparedStatement ps = null;
        try {
            List<Object> params = new ArrayList<Object>();
            String tableName = getTableName();
            String primaryKey = Ar.getPrimaryKey(getClass());
            ArHelper.buildUpdate(tableName, getProps(), sql, params, primaryKey);
            ps = conn.prepareStatement(sql.toString());
            ArHelper.prepare(ps, params.toArray());
            return ps.executeUpdate() >= 1;
        } finally {
            ArHelper.closePreparedStatement(ps);
        }
    }


    protected boolean delete() {
        Connection conn = null;
        try {
            conn = Ar.getConnection(getClass());
            return delete(conn);
        } catch (Exception e) {
            throw new ArException("execute delete error.", e);
        } finally {
            ArHelper.closeConnection(conn);
        }
    }


    private boolean delete(Connection conn) throws SQLException {
        String primaryKey = Ar.getPrimaryKey(getClass());
        Object id = getObject(primaryKey);
        return deleteByPrimaryKey(conn, id);
    }


    protected boolean deleteByPrimaryKey(Object value) {
        Connection conn = null;
        try {
            conn = Ar.getConnection(getClass());
            return deleteByPrimaryKey(conn, value);
        } catch (Exception e) {
            throw new ArException("execute delete error.", e);
        } finally {
            ArHelper.closeConnection(conn);
        }

    }

    private boolean deleteByPrimaryKey(Connection conn, Object value) throws SQLException {
        PreparedStatement ps = null;
        try {
            String sql = ArHelper.buildDeleteByPrimaryKey(getClass(), getTableName());
            ps = conn.prepareStatement(sql);
            ArHelper.prepare(ps, value);
            return ps.executeUpdate() >= 1;
        } finally {
            ArHelper.closePreparedStatement(ps);
        }
    }


    public M findByPrimaryKey(Cache cache, String key, Object id) {
        M m = cache.get(key);
        if (null != m) {
            return m;
        }
        m = findByPrimaryKey(id);
        cache.put(key, m);
        return m;
    }

    public M findByPrimaryKey(Object id) {
        String tableName = getTableName();
        String sql = ArHelper.buildGet(getClass(), tableName);
        return findFirst(sql, id);
    }


    protected M findFirst(Cache cache, String key, String sql, Object... params) {
        M m = cache.get(key);
        if (null != m) {
            return m;
        }
        m = findFirst(sql, params);
        cache.put(key, m);
        return m;
    }

    protected M findFirst(String sql, Object... params) {
        List<M> result = find(sql, params);
        return (null == result || result.isEmpty()) ? null : result.get(0);
    }


    protected List<M> find(Cache cache, String key, String sql, Object... params) {
        List<M> result = cache.get(key);
        if (null != result) {
            return result;
        }
        result = find(sql, params);
        cache.put(key, result);
        return result;
    }


    protected List<M> find(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = Ar.getConnection(getClass());
            return find(conn, sql, params);
        } catch (Exception e) {
            throw new ArSQLException(sql, e);
        } finally {
            ArHelper.closeConnection(conn);
        }
    }


    private List<M> find(Connection conn, String sql, Object... params) throws SQLException, InstantiationException, IllegalAccessException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ArHelper.prepare(ps, params);
            rs = ps.executeQuery();
            return ArHelper.obtainResult(getClass(), rs);
        } finally {
            ArHelper.close(ps, rs);
        }
    }


    protected String buildInClause(String column, Collection<Object> params) {
        return ArHelper.buildInClause(getClass(), column, params);
    }


    public String getTableName() {
        return Ar.getTableFlag(getClass());
    }

    public Object getObject(String key) {
        return props.get(key);
    }

    public String getString(String key) {
        return (String) props.get(key);
    }


    public Integer getInt(String key) {
        return (Integer) props.get(key);
    }


    public Long getLong(String key) {
        return (Long) props.get(key);
    }


    public Double getDouble(String key) {
        return (Double) props.get(key);
    }


    public Float getFloat(String key) {
        return (Float) props.get(key);
    }


    public Boolean getBoolean(String key) {
        return (Boolean) props.get(key);
    }


    public BigInteger getBigInteger(String key) {
        return (BigInteger) props.get(key);
    }


    public Date getDate(String key) {
        return (Date) props.get(key);
    }


    public Timestamp getTimestamp(String key) {
        return (Timestamp) props.get(key);
    }
}
