package wenjh.akit.common.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.StringUtil;

/**
 *
 * @author jianhua
 *
 * @param <T> 对象泛型
 * @param <PK> 主键泛型
 */
public abstract class BaseDao<T, PK extends Serializable> {
    protected String tableName;
    protected SQLiteDatabase db = null;
    protected LogUtil log = null;
    /*
    * 主键字段名
    */
    private String pk = "_id";

    /**
     * 构造一个数据访问对象，采用此构造器，将默认使用 "_id" 作为主键字段。
     * @param db
     * @param tableName
     */
    public BaseDao(SQLiteDatabase db, String tableName) {
        this.tableName = tableName;
        this.db = db;
        log = new LogUtil(getClass().getSimpleName()).closeDebug();
        log.setMsgPrefix("AKit SQL**==");
    }

    /**
     * 构造一个数据访问对象，并指定主键字段
     * @param db
     * @param tableName
     * @param primaryKey 主键字段
     */
    public BaseDao(SQLiteDatabase db, String tableName, String primaryKey) {
        this(db, tableName);
        this.pk = primaryKey;
    }


    public void closeDebug() {
        log.closeDebug();
    }

    public void openDebug() {
        log.openDebug();
    }

    public SQLiteDatabase getDb() {
        return db;
    }


    /**
     * 执行一条 sql 语句
     * @param sql sql语句
     * @param parameters 参数
     */
    public void executeSQL(String sql, Object[] parameters) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }
        log.d("executeSQL-->" + sql + "   |-------| params:"+Arrays.toString(parameters));
        checkProcessable();


        if(parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                Object parameter = parameters[i];
                if(parameter != null && parameter instanceof Date) {
                    parameters[i] = toDbTime((Date) parameter);
                } else if(parameter != null && parameter instanceof Boolean) {
                    parameters[i] = ((Boolean)parameter) ? 1 : 0;
                } else if(parameter != null && parameter instanceof Object[]) {
                	parameters[i] = StringUtil.join((Object[])parameter, ",");
                } else if(parameter != null && parameter instanceof Collection<?>) {
                	parameters[i] = StringUtil.join((Collection<?>)parameter, ",");
                }
            }
        }
        db.execSQL(sql, parameters);
    }

    /**
     * 执行一条无参 sql 语句
     * @param sql
     */
    public void executeSQL(String sql) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }


        log.d("executeSQL-->" + sql);
        checkProcessable();
        db.execSQL(sql);
    }

    /**
     * 查询，返回结果集游标
     * @param sql
     * @param params
     * @return
     */
    public Cursor query(String sql, String[] params) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        log.d("execute query --> " + sql + "  |-------| params:" + Arrays.toString(params));
        return db.rawQuery(sql, params);
    }

    /**
     * 查询，返回组装后的对象
     * @param sql
     * @param params
     * @return
     */
    public T findUnique(String sql, String[] params) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        Cursor cursor = query(sql, params);
        T t = null;
        if(cursor.moveToFirst()) {
            assemble(cursor);
        }
        cursor.close();
        return t;
    }

    /**
     * 执行 SQL 语句，返回对象列表
     * @param sql
     * @param params
     * @return
     */
    public List<T> list(String sql, String[] params) {
        List<T> list = new ArrayList<T>();
        if(db == null) {
            log.i("db instance is null");
            return list;
        }

        Cursor cursor = query(sql, params);
        while(cursor.moveToNext()) {
            list.add(assemble(cursor));
        }
        cursor.close();
        return list;
    }

    /**
     * 根据键、值集合，返回对象列表。并参照某个字段排序。
     * @param fileds 字段
     * @param params 参数
     * @param orderField 排序字段
     * @param asc 是否为升序。true表示升序，false为降序
     * @return
     */
    public List<T> list(String[] fileds, String[] params, String orderField, boolean asc) {
        List<T> list = new ArrayList<T>();
        if(db == null) {
            log.i("db instance is null");
            return list;
        }

        StringBuilder sql = new StringBuilder("select * from "+ tableName + " ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }

        sql.append(" order by ").append(orderField);
        if(!asc) {
            sql.append(" desc");
        } else {
            sql.append(" asc");
        }

        Cursor cursor = query(sql.toString(), params);

        while(cursor.moveToNext()) {
            list.add(assemble(cursor));
        }
        cursor.close();
        return list;
    }

    /**
     * 根据键、值集合，返回对象列表。并参照某个字段排序。支持分页操作。
     * @param fileds 字段
     * @param params 参数
     * @param orderField 排序字段
     * @param asc 是否为升序。true表示升序，false为降序
     * @param pageStartIndex 分页字段：开始位置
     * @param offset 分页字段：要获取的记录条数
     * @return
     */
    public List<T> list(String[] fileds, String[] params, String orderField, boolean asc, int pageStartIndex, int offset) {
        List<T> list = new ArrayList<T>();

        if(db == null) {
            return list;
        }

        StringBuilder sql = new StringBuilder("select * from "+ tableName + " "); // + " limit " + pageStartIndex + "," + offset +


        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }

        if(orderField != null) {
            sql.append(" order by ").append(orderField);
            if(asc) {
                sql.append(" asc");
            } else {
                sql.append(" desc");
            }
        }

        sql.append(" limit ").append(pageStartIndex).append(",").append(offset);

        Cursor cursor = query(sql.toString(), params);

        while(cursor.moveToNext()) {
            list.add(assemble(cursor));
        }
        cursor.close();
        return list;
    }

    /**
     * 根据键、值集合，返回字段列表。并参照某个字段排序。支持分页操作。
     * @param selectField 需要从数据库查询的字段
     * @param fileds 字段
     * @param params 参数
     * @param orderField 排序字段
     * @param asc 是否为升序。true表示升序，false为降序
     * @param pageStartIndex 分页字段：开始位置
     * @param offset 分页字段：要获取的记录条数
     * @return
     */
    public List<String> list(String selectField, String[] fileds, String[] params, String orderField, boolean asc, int pageStartIndex, int offset) {
        List<String> list = new ArrayList<String>();

        if(db == null) {
            return list;
        }

        StringBuilder sql = new StringBuilder("select "+selectField+" from "+ tableName + " "); // + " limit " + pageStartIndex + "," + offset +


        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }

        sql.append(" order by ").append(orderField);
        if(!asc) {
            sql.append(" asc");
        } else {
            sql.append(" desc");
        }

        sql.append(" limit ").append(pageStartIndex).append(",").append(offset);

        Cursor cursor = query(sql.toString(), params);

        while(cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    public String[] arrayFiled(String filed, String[] fileds, String[] params) {
        StringBuilder sql = new StringBuilder("select "+filed+" from " + tableName + " ");

        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }

        List<String> list = new ArrayList<String>();
        Cursor cursor = query(sql.toString(), params);
        while(cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();

        String[] arrays = new String[list.size()];

        return list.toArray(arrays);
    }

    /**
     * 根据键、值集合，返回对象列表
     * @param params
     * @return
     */
    public List<T> list(String[] fileds, String[] params) {
        List<T> list = new ArrayList<T>();
        if(db == null) {
            log.i("db instance is null");
            return list;
        }

        StringBuilder sql = new StringBuilder("select * from "+ tableName + " ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }
        Cursor cursor = query(sql.toString(), params);

        while(cursor.moveToNext()) {
            list.add(assemble(cursor));
        }
        cursor.close();
        return list;
    }

    /**
     * 根据键、值集合，返回对象列表。支持模糊查询
     * @param fileds
     * @param params
     * @param fuzzy true表示启用模糊查询, false不启用
     * @param and 筛选条件是 and 还是  or。true表示用and取并集,false反之
     * @return
     */
    public List<T> query(String[] fileds, String[] params, boolean fuzzy, boolean and) {
        List<T> list = new ArrayList<T>();
        if(db == null) {
            return list;
        }

        String cs = fuzzy ? " like " : "=";
        String signPre = fuzzy ? "'%" : "";
        String signAfter = fuzzy ? "%'" : "";

        String v = and?" and ":" or ";

        StringBuilder sql = new StringBuilder("select * from "+ tableName + " ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append(cs + signPre + params[i] + signAfter);
            if(++i < filedCount) {
                sql.append(v);
            }
        }

        Cursor cursor = query(sql.toString(), new String[]{});

        while(cursor.moveToNext()) {
            list.add(assemble(cursor));
        }
        cursor.close();
        return list;
    }

    public String minField(String filed) {
    	String sql = "select min("+filed+") from " + tableName;
    	Cursor cursor = query(sql, new String[]{});
    	if(cursor.moveToNext()) {
    		return cursor.getString(0);
    	}
    	return null;
    }
    
    public T max(String filed) {
        return max(filed, new String[]{}, new String[]{});
    }

    public T max(String maxFiled, String[] fileds, String[] values) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        
        StringBuilder sql = new StringBuilder("select * from " + tableName + " where " + maxFiled + "=" + "(select max("+maxFiled+") from "+ tableName +" ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }

        sql.append(")");

        Cursor cursor = query(sql.toString(), values);
        try {
            if(cursor.moveToFirst()) {
                T t = assemble(cursor);
                cursor.close();
                return t;
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public String maxField(String selectedField, String maxFiled, String[] fileds, String[] values) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        StringBuilder sql = new StringBuilder("select "+selectedField+" from " + tableName + " where " + maxFiled + "=" + "(select max("+maxFiled+") from "+ tableName +" ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }

        sql.append(")");

        Cursor cursor = query(sql.toString(), values);
        try {
            if(cursor.moveToFirst()) {
                String value = cursor.getString(0);
                return value;
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /**
     * 根据条件，执行查询语句，返回 List 集合<br/>
     * 例如：listBySelection("username=? and password=?", String[] {"wjh", "wjh123"})
     * @param where 条件
     * @param params 参数，如果没有，则传入一个空的数组
     * @return 对象集合
     */
    public List<T> listBySelection(String where, String[] params) {
        String sql = "select * from "+ tableName +" where " + where;
        return list(sql, params);
    }

    
    /**
     * 根据条件，执行查询语句，返回 List 集合<br/>
     * @param where 条件
     * @param fileds 查询字段
     * @param params 参数，如果没有，则传入一个空的数组
     * @return 对象集合
     */
    public List<T> listByFiledsAndSelection(String where, String[] params,String...fileds) {
        StringBuilder sb=new StringBuilder();
        sb.append("select ");
        if (fileds.length > 1) {
			for (int i = 0; i < fileds.length; i++) {
				if (i < fileds.length - 1) {
					sb.append(fileds[i]+",");
				} else {
					sb.append(fileds[i]);
				}
			}
		} else {
			sb.append(fileds[0]);
		}
    	sb.append(" from "+ tableName +" where " + where);
        return list(sb.toString(), params);
    }
    
    /**
     * 与params里的值相等的字段的所有记录:select from users where id in (111,222,333);
     * @param filed
     * @param params 数组，如：{111,222,333},将自动转换为"111,222,333"样式的字符串
     * @return
     */
    public List<T> listIn(String filed, Object[] params, String orderField, boolean asc) {
        StringBuffer sql = new StringBuffer();
        sql.append("select * from ").append(tableName).append(" where ").append(filed).append(" in (").append(StringUtil.join(params, "'", ",")).append(")");
        if(!StringUtil.isEmpty(orderField)){
            sql.append(" order by ").append(orderField);
            if(!asc) {
                sql.append(" asc");
            } else {
                sql.append(" desc");
            }
        }
        return list(sql.toString(), new String[]{});
    }

    /**
     * 验证数据库是否可访问和可写
     */
    protected void checkProcessable() {
        checkAccessable();
        if(db.isReadOnly()) {
           throw new RuntimeException(new SQLiteException("db is read only"));
        }
    }

    /**
     * 验证数据库是否可访问（未建立连接或连接已关闭）
     */
    protected void checkAccessable() {
        if(db == null) {
           throw new RuntimeException(new NullPointerException("db is null"));
        }
        if(!db.isOpen()) {
        	throw new RuntimeException(new SQLiteException("db is already closed"));
        }
    }

    /**
     * 获取指定主键值对应的对象
     * @param parimaryKey
     * @return
     */
    public T get(PK parimaryKey) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        String sql = "select * from "+ tableName +" where "+ this.pk +"=?";
        Cursor cursor = query(sql, new String[]{parimaryKey+""});
        if(cursor.moveToNext()) {
            T t =  assemble(cursor);
            cursor.close();
            return t;
        }
        cursor.close();
        return null;
    }

    public boolean get(T obj, PK parimaryKey) {
        boolean result = false;

        if(db == null) {
            log.i("db instance is null");
        }

        String sql = "select * from "+ tableName +" where "+ this.pk +"=?";
        Cursor cursor = query(sql, new String[]{parimaryKey+""});
        if(cursor.moveToNext()) {
            assemble(obj, cursor);
            result = true;
        }
        cursor.close();
        return result;
    }

    public String getFiled(String getFiledName, String[] fileds, String[] values) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        StringBuilder sql = new StringBuilder("select "+getFiledName+" from "+ tableName + " ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }
        Cursor cursor = query(sql.toString(), values);
        String value = null;
        if(cursor.moveToNext()) {
            value =  cursor.getString(0);
        }
        cursor.close();
        return value;
    }

    /**
     * 删除主键值对应的对象
     * @param primaryKey
     */
    public void  delete(PK primaryKey) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        String sql = "delete from "+ tableName +" where "+ this.pk +"=?";
        executeSQL(sql, new Object[]{primaryKey});
    }

    /**
     * 删除，按指定字段
     * @param filed
     * @param value
     */
    public void delete(String filed, Object value) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        String sql = "delete from "+ tableName +" where "+ filed +"=?";
        executeSQL(sql, new Object[]{value.toString()});
    }

    /**
     * 删除，按指定字段
     * @param fileds
     * @param values
     */
    public void delete(String[] fileds, Object[] values) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        StringBuilder sql = new StringBuilder("delete from "+ tableName + " ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }
        executeSQL(sql.toString(), values);
    }


    /**
     * 获取对象，按指定字段
     * @param filed
     * @param value
     */
    public T get(String filed, Object value) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        String sql = "select * from "+ tableName +" where "+ filed +"=?";
        Cursor cursor = query(sql, new String[]{value.toString()});
        T t = null;
        if(cursor.moveToFirst()) {
            t =  assemble(cursor);
        }
        cursor.close();
        return t;
    }

    /**
     * 获取对象，按指定字段
     * @param fileds
     * @param values
     */
    public T get(String[] fileds, String[] values) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        StringBuilder sql = new StringBuilder("select * from "+ tableName + " ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }
        Cursor cursor = query(sql.toString(), values);
        if(cursor.moveToNext()) {
            T t =  assemble(cursor);
            cursor.close();
            return t;
        }
        //fix "Application did not close the cursor or database object that was opened here" bug
        else {
            cursor.close();
        }
        //
        return null;
    }

    public List<T> getAll() {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        List<T> lists = new ArrayList<T>();
        String sql = "select * from " + tableName;
        Cursor cursor = query(sql, new String[]{});
        while(cursor.moveToNext()) {
            lists.add(assemble(cursor));
        }
        cursor.close();
        return lists;
    }


    /**
     * 返回所有，并参照某个字段排序
     * @param orderField 排序字段
     * @param asc 是否为升序。true表示升序，false为降序
     * @return
     */
    public List<T> getAll(String orderField, boolean asc) {
        if(db == null) {
            log.i("db instance is null");
            return null;
        }

        List<T> lists = new ArrayList<T>();
        String sql = "select * from " + tableName + " order by " + orderField;
        if(!asc) {
            sql += " desc";
        }
        Cursor cursor = query(sql, new String[]{});
        while(cursor.moveToNext()) {
            lists.add(assemble(cursor));
        }
        cursor.close();
        return lists;
    }

    /**
     *  按照条件删除对象
     *  例如：listBySelection("username=? and password=?", String[] {"wjh", "wjh123"})
     *
     * @param where 条件
     * @param params 参数，如果没有，则传入一个空的数组
     */
    public void deleteBySelection(String where, Object[] params) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        StringBuilder sql = new StringBuilder("delete from "+ tableName + " ");

        sql.append(" where ").append(where);

        executeSQL(sql.toString(), params);
    }

    /**
     * 删除与values串里的值相等的字段的所有记录:delete from users where id in (111,222,333);
     *
     * @param filed 字段
     * @param values 数组，如：{111,222,333},将自动转换为"111,222,333"样式的字符串
     */
    public void delelteIn(String filed, Object[] values) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        StringBuilder sql = new StringBuilder("delete from "+ tableName + " ");
        sql.append(" where ").append(filed)
                .append(" in ").append("(").append(StringUtil.join(values, "'", ",")).append(")");
        executeSQL(sql.toString());
    }

    /**
     * 删除所有记录
     */
    public void deleteAll() {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        executeSQL("delete from " + tableName);
    }

    public int sum(String filed, String[] where, String[] values) {
    	 if(db == null) {
             log.i("db instance is null");
             return 0;
         }
         StringBuilder sql = new StringBuilder("select sum("+filed+") s from "+ tableName + " ");
         int filedCount = where.length;
         if(filedCount > 0) {
             sql.append("where ");
         }
         for(int i=0; i<filedCount;) {
             sql.append(where[i]).append("=? ");
             if(++i < filedCount) {
                 sql.append("and ");
             }
         }
         int count = 0;
         Cursor c = query(sql.toString(), values);
         if(c.moveToFirst()) {
         	count = c.getInt(0);
         }
         c.close();
         return count;
    }
    
    public void updateFiled(String filed, Object value, PK primaryKey) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        updateFiled(new String[]{filed}, new Object[]{value}, new String[]{pk}, new Object[]{primaryKey});
    }
    
    public void updateFileds(String[] fileds, Object[] values, PK primaryKey) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        updateFiled(fileds, values, new String[]{pk}, new Object[]{primaryKey});
    }

    public void updateFileds(Map<String, Object> map, String[] whereFileds, Object[] wherevalues) {
    	String[] fileds = new String[map.size()];
    	Object[] values = new Object[map.size()];
    	
    	Set<Entry<String,Object>> entrySet = map.entrySet();
    	int i=0;
    	for (Entry<String, Object> entry : entrySet) {
    		fileds[i] = entry.getKey();
    		values[i] = entry.getValue();
    		i++;
		}
    	
    	updateFiled(fileds, values, whereFileds, wherevalues);
    }
    
    public void updateFiled(String[] fileds, Object[] values, String[] whereFileds, Object[] wherevalues) {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        if(fileds.length != values.length) {
            throw new SQLiteException("fileds.length != values.length");
        }

        if(whereFileds.length != wherevalues.length) {
            throw new SQLiteException("whereFileds.length != wherevalues.length");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("update "+ tableName +" set ");

        Object[] params = new Object[fileds.length + whereFileds.length];

        for(int i=0; i<fileds.length;) {
            params[i] = values[i];

            sql.append(fileds[i]).append("=? ");
            if(++i < fileds.length) {
                sql.append(", ");
            }

        }

        int filedCount = whereFileds.length;
        if(filedCount > 0) {
            sql.append(" where ");
        }
        for(int i=0; i<filedCount;) {
            params[fileds.length + i] = wherevalues[i];

            sql.append(whereFileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }


        executeSQL(sql.toString(), params);
    }
    
    public void insertFileds(Map<String, Object> map) {
    	String[] fileds = new String[map.size()];
    	Object[] values = new Object[map.size()];
    	
    	Set<Entry<String,Object>> entrySet = map.entrySet();
    	int i=0;
    	for (Entry<String, Object> entry : entrySet) {
    		fileds[i] = entry.getKey();
    		values[i] = entry.getValue();
    		i++;
		}
    	
    	insert(fileds, values);
    }

    public void insert(String[] fileds, Object[] values) {

        if(fileds.length != values.length) {
            throw new SQLiteException("fileds.length != values.length");
        }

        StringBuilder sql = new StringBuilder();
        StringBuilder params = new StringBuilder();

        sql.append("insert into "+ tableName +" (");

        for(int i=0; i<fileds.length; i++) {
            sql.append(fileds[i]);
            params.append("?");

            if(i < fileds.length-1) {
                sql.append(", ");
                params.append(",");
            }
        }

        sql.append(") values (");
        sql.append(params);
        sql.append(") ");

        executeSQL(sql.toString(), values);
    }
    
    public void updateIn(String filed, Object newValue, Object oldValue, String arrayField, Object[] arrayValues) {
        StringBuilder sql = new StringBuilder();
        sql.append("update "+ tableName +" set ")
                .append(filed +"=? ")
                .append(" where " + filed + "=? ")
                .append(" and "+ arrayField +" in (" + StringUtil.join(arrayValues, "'", ",") +")");

        executeSQL(sql.toString(), new Object[]{newValue, oldValue});
    }

    public void updateIn(String filed, Object value, String arrayField, Object[] arrayValues) {
        StringBuilder sql = new StringBuilder();
        sql.append("update "+ tableName +" set ")
                .append(filed +"=? ")
                .append(" where "+ arrayField +" in (" + StringUtil.join(arrayValues, "'", ",") +")");

        executeSQL(sql.toString(), new Object[]{value});
    }

    private T t = null;

    /**
     * 组装数据为对象，子类重写此方法是要注意“不要关闭 Cursor 对象”
     * @param cursor
     * @return
     * @throws Throwable
     */
    protected abstract T assemble(Cursor cursor);

    private Field getDeclaredField(Class clazz, String name) {
        Field filed= null;
        try {
            filed = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
        }

        if(filed == null && clazz.getSuperclass() != null) {
            return getDeclaredField(clazz.getSuperclass(), name);
        }
        return filed;
    }

    protected abstract void assemble(T obj, Cursor cursor);

    /**
     * 插入数据
     */
    public abstract void insert(T t);

    /**
     * 修改
     */
    public abstract void update(T t);

    /**
     * 删除
     * @param obj
     */
    public abstract void deleteInstence(T obj);

    /**
     *  直接删除表
     */
    public void drop() {
        if(db == null) {
            log.i("db instance is null");
            return;
        }

        executeSQL("DROP TABLE IF EXISTS " + tableName);
    }

    /**
     * 验证记录是否存在
     * @return
     */
    public boolean checkExist(PK primaryKey) {
        return checkExsit(this.pk, primaryKey.toString());
    }

    public boolean checkExsit(String filed, String value) {
        if(db == null) {
            log.i("db instance is null");
            return false;
        }

        return count(new String[]{filed}, new String[]{value}) > 0;
    }

    /**
     * 根据筛选条件，获取记录条数
     *
     * @param fileds 筛选字段集合
     * @param params 筛选字段对应的值的集合
     *
     * @return 记录条数。没有对应记录返回为0
     */
    public int count(String[] fileds, String[] params) {
        if(db == null) {
            log.i("db instance is null");
            return 0;
        }

        StringBuilder sql = new StringBuilder("select count(*) c from "+ tableName + " ");
        int filedCount = fileds.length;
        if(filedCount > 0) {
            sql.append("where ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(fileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append("and ");
            }
        }

        int count = 0;
        Cursor c = query(sql.toString(), params);
        if(c.moveToFirst()) {
        	count = c.getInt(0);
        }
        c.close();
        return count;
    }


    public int countIn(String filed, String[] params) {
        String sql = "select count(*) c from "+ tableName +" where " + filed + " in ("+StringUtil.join(params, "'", ",")+") ;";
        Cursor c = query(sql.toString(), new String[]{});
        int count = 0;
        if(c.moveToFirst()) {
        	count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public int countIn(String filed, String[] params, String[] whereFileds, String[] whereParams) {
        StringBuilder sql = new StringBuilder("select count(*) c from "+ tableName +" where " + filed + " in (" + StringUtil.join(params, "'", ",") + ") ");
        int filedCount = whereFileds.length;
        if(filedCount > 0) {
            sql.append(" and ");
        }
        for(int i=0; i<filedCount;) {
            sql.append(whereFileds[i]).append("=? ");
            if(++i < filedCount) {
                sql.append(" and ");
            }
        }

        Cursor c = query(sql.toString(), whereParams);
        int count = 0;
        if(c.moveToFirst()) {
        	count = c.getInt(0);
        }
        c.close();
        return count;
    }

    /**
     * 日期转换成毫秒数值
     * @param date
     * @return
     */
    public static long toDbTime(Date date) {
        if(date == null) {
            return 0L;
        }

        return date.getTime();
    }

    /**
     * 毫秒数值转换成日期
     * @param time
     * @return
     */
    public static Date toDate(long time) {
        if(time <= 0) {
            return null;
        }

        return new Date(time);
    }

    public void beginTransaction() {
        db.beginTransaction();
    }
    public void endTransaction() {
        db.endTransaction();
    }
    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    public static int getInt(Cursor cursor, String filed) {
        int c = cursor.getColumnIndex(filed);
    	if(c >= 0) {
    		return cursor.getInt(c);
    	}
    	return -1;
    }

    public static long getLong(Cursor cursor, String filed) {
    	int c = cursor.getColumnIndex(filed);
    	if(c >= 0) {
    		return cursor.getLong(c);
    	}
    	return -1;
    }

    public static String getString(Cursor cursor, String filed) {
    	int c = cursor.getColumnIndex(filed);
    	if(c >= 0) {
    		return cursor.getString(c);
    	}
    	return null;
    }
    
    public static Date getDate(Cursor cursor, String filed) {
    	return toDate(getLong(cursor, filed));
    }
    
    public static boolean getBoolean(Cursor cursor, String filed) {
    	return getInt(cursor, filed) == 1;
    }
    
    public static float getFloat(Cursor cursor, String filed) {
    	int c = cursor.getColumnIndex(filed);
    	if(c >= 0) {
    		return cursor.getFloat(c);
    	}
    	return -1;
    }
    
    public static String[] getStringArray(Cursor cursor, String filed) {
    	String string = getString(cursor, filed);
    	if(string != null && string.length() > 0) {
    		return StringUtil.str2Arr(string, ",");
    	} else {
    		return null;
    	}
    }
    
    public static List<String> getStringList(Cursor cursor, String filed) {
    	String[] array = getStringArray(cursor, filed);
    	return array != null ? Arrays.asList(array) : null;
    }

}