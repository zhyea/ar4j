package com.zhyea.ar4j.core;


import com.zhyea.ar4j.core.cache.Cache;
import com.zhyea.ar4j.core.dialect.Dialect;
import com.zhyea.ar4j.core.exception.ArException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 用来适配分表场景
 */
public abstract class SeqModel<M extends SeqModel> extends Model<M> {


    private String tableName = getTableName();

    public abstract String latestSuffix();

    public abstract String suffixRegex();

    protected List<M> findInSeq(Cache cache, String key, String sqlSelect, String sqlWhere, Object... params) {
        List<M> result = cache.get(key);
        if (null != result) {
            return result;
        }
        result = findInSeq(sqlSelect, sqlWhere, params);
        cache.put(key, result);
        return result;
    }

    protected List<M> findInSeq(String sqlSelect, String sqlWhere, Object... params) {
        Set<String> tables = findTableNames();
        List<M> result = new ArrayList<M>();
        for (String s : tables) {
            String sql = sqlSelect + " from " + s + " where " + sqlWhere;
            List<M> list = find(sql, params);
            for (M m : list) {
                m.setTableName(s);
                result.add(m);
            }
        }
        return result;
    }

    protected M findFirstInSeq(Cache cache, String key, String sqlSelect, String sqlWhere, Object... params) {
        M m = cache.get(key);
        if (null != m) {
            return m;
        }
        m = findFirstInSeq(sqlSelect, sqlWhere, params);
        cache.put(key, m);
        return m;
    }

    protected M findFirstInSeq(String sqlSelect, String sqlWhere, Object... params) {
        Set<String> tables = findTableNames();
        for (String s : tables) {
            String sql = sqlSelect + " from " + s + " where " + sqlWhere;
            M m = findFirst(sql, params);
            if (null != m) {
                m.setTableName(s);
                return m;
            }
        }
        return null;
    }


    private Set<String> findTableNames() {
        if (null == suffixRegex()) {
            throw new ArException("suffixRex() need to return a not null value.");
        }
        try {
            Dialect dialect = Ar.getDialect(getClass());
            String sql = dialect.sqlShowTables();
            String suffixRegex = suffixRegex();
            return ArHelper.findTableNames(getClass(), sql, suffixRegex);
        } catch (Exception e) {
            throw new ArException("find seq tableNames error...", e);
        }
    }

    @Override
    public String getTableName() {
        if (null != this.tableName) {
            return this.tableName;
        }
        return Ar.getTableFlag(getClass()) + latestSuffix();
    }


    void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
