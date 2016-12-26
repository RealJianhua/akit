package wenjh.akit.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import wenjh.akit.common.util.LogUtil;
import wenjh.akit.common.util.StringUtil;

public abstract class AKitDBOpenHandler extends SQLiteOpenHelper {
	private String mDatabaseName = null;
	private LogUtil log = new LogUtil(getClass().getSimpleName());
	public String getName() {
		return mDatabaseName;
	}
	private SQLiteDatabase exector = null;
	private List<String> onTableCreatedSqlList;

	public AKitDBOpenHandler(Context context, String databaseName, int dbVersion) {
		super(context, databaseName, null, dbVersion);
		this.mDatabaseName = databaseName;
	}

	public String getDatabaseName() {
		return mDatabaseName;
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		this.onCreate(db);
	}

	@Override
	public final void onCreate(SQLiteDatabase db) {
		this.exector = db;
		this.onTableCreatedSqlList = new ArrayList<>();
		createTable(db);
		db.beginTransaction();
		try {
			for (String createDbSql : onTableCreatedSqlList) {
				db.execSQL(createDbSql);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		this.exector = null;
		onTableCreatedSqlList.clear();
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		log.i("db opened!");
	}

	protected abstract void createTable(SQLiteDatabase db);

	/**
	 * 构建默认builder，默认规则是：a) 创建自增长的int主键 '_id'  b) 创建30个备用字段
	 * @param tableName
	 * @return
     */
	protected TableBuilder newTableBuilder(String tableName) {
		TableBuilder builder = new TableBuilder(onTableCreatedSqlList);
		builder.tableName = tableName;
		builder.primaryKeyName = ITable.DBFIELD_ID;
		builder.primaryFieldType = FieldType.Integer;
		builder.autoincrement = true;
		builder.tempFieldCount = 30;
		return builder;
	}

	protected static class TableBuilder {
		private FieldType primaryFieldType;
		private String tableName;
		private int tempFieldCount;
		private  String primaryKeyName;
		private boolean autoincrement;
		private List<String> execSqlList;
		private List<String> buildIndexSql;

		private TableBuilder(List<String> sqlList){
			this.execSqlList = sqlList;
			buildIndexSql = new ArrayList<>();
		}

		public TableBuilder primaryKey(@NonNull String primaryKeyName, @NonNull FieldType fieldType) {
			this.primaryKeyName = primaryKeyName;
			this.primaryFieldType = fieldType;
			return this;
		}

		public TableBuilder tableName(@NonNull String tableName) {
			this.tableName = tableName;
			return this;
		}

		public TableBuilder tempFieldCount(int count) {
			this.tempFieldCount = count;
			return this;
		}

		public TableBuilder autoincrement(boolean autoincrement) {
			this.autoincrement = autoincrement;
			return this;
		}

		public TableIndexBuilder newIndexBuilder() {
			return new TableIndexBuilder(this).tableName(tableName);
		}

		public void buildIndex() {
			for (String sql : buildIndexSql) {
				LogUtil.printLog("akitdb, " + sql);
				execSqlList.add(sql);
			}
			buildIndexSql.clear();
		}

		void addToIndexSqlList(String sql) {
			buildIndexSql.add(sql);
		}

		public TableBuilder build() {
			if (StringUtil.isEmpty(tableName)) {
				throw new IllegalArgumentException("tablename is null");
			}

			if (StringUtil.isEmpty(primaryKeyName)) {
				throw new IllegalArgumentException("primary key is null");
			}

			if (primaryFieldType == null) {
				throw new IllegalArgumentException("fieldType is null");
			}

			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS ")
					.append(tableName)
					.append("(")
					.append(primaryKeyName)
					.append(" ");

			switch (primaryFieldType) {
				case Integer:
					sb.append("INTEGER ");
					if(autoincrement) {
						sb.append("AUTO_INCREMENT ");
					}
					sb.append("primary key");
					break;
				case Text:
				case Varchar:
					sb.append("VARCHAR(50) primary key");
					break;
				default:
					throw new IllegalArgumentException("fieldType not support");
			}

			for (int i = 1; i < tempFieldCount; i++) {
				if(i == 1) {
					sb.append(",");
				}

				String field = ITable.DBFIELD_TMP + i + " NUMERIC";
				if (i < tempFieldCount - 1) {
					field += ",";
				}

				sb.append(field);
			}
			sb.append(")");
			LogUtil.printLog("akitdb, " + sb.toString());
			execSqlList.add(sb.toString());
			buildIndex();
			return this;
		}
	}

	protected static class TableIndexBuilder {
		private String tableName;
		private  String indexFieldName;
		private boolean unique;
		private String indexName;
		private TableBuilder tableBuilder;

		private TableIndexBuilder(TableBuilder tableBuilder){
			this.tableBuilder = tableBuilder;
		}

		public TableIndexBuilder tableName(String tableName) {
			this.tableName = tableName;
			return this;
		}

		public TableIndexBuilder nameIndex(@NonNull String indexName) {
			this.indexName = indexName;
			return this;
		}

		public TableIndexBuilder nameField(@NonNull String fieldName) {
			this.indexFieldName = fieldName;
			return this;
		}

		public TableIndexBuilder unique(boolean unique) {
			this.unique = unique;
			return this;
		}

		public TableBuilder build() {
			if (StringUtil.isEmpty(tableName)) {
				throw new IllegalArgumentException("tablename is null");
			}

			if (StringUtil.isEmpty(indexFieldName)) {
				throw new IllegalArgumentException("primary key is null");
			}

			if (StringUtil.isEmpty(indexName)) {
				throw new IllegalArgumentException("primary key is null");
			}

			StringBuilder sb = new StringBuilder();
			sb.append("CREATE ");
			if (unique) {
				sb.append("UNIQUE ");
			}
			sb.append("INDEX IF NOT EXISTS ")
					.append(indexName)
					.append(" ON ")
					.append(tableName)
					.append(" (")
					.append(indexFieldName)
					.append(")");
			tableBuilder.addToIndexSqlList(sb.toString());
			return tableBuilder;
		}
	}

	protected enum FieldType {
		Null,
		Integer,
		Real,
		Text,
		Blob,
		Varchar,
		Float,
		Date,
		Datetime,
		Boolean
	}

}
