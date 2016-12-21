package wenjh.akit.demo.account.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import wenjh.akit.demo.account.model.IUserTable;
import wenjh.akit.demo.chat.model.IChatSessionTable;
import wenjh.akit.demo.chat.model.IMessageTable;
import wenjh.akit.common.util.LogUtil;

public class AccountDBOpenHandler extends SQLiteOpenHelper {
	private final static int DB_MIN_VERSION = 4;
	private static final int DB_CURRENT_VERSION = DB_MIN_VERSION;

	private String databaseName = null;
	private LogUtil log = new LogUtil(getClass().getSimpleName());

	public String getName() {
		return databaseName;
	}

	public AccountDBOpenHandler(Context context, String dbpath) {
		super(context, dbpath, null, DB_CURRENT_VERSION);
		this.databaseName = dbpath;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createPEOMessageTableSQL = "CREATE TABLE IF NOT EXISTS " + IMessageTable.PeopleTableName
				+ "(" + IMessageTable.F_MessageID + " VARCHAR(50) primary key, ";
		db.execSQL(addTmpField(createPEOMessageTableSQL, 30));
		
		String createChatSessionTableSQL = "CREATE TABLE IF NOT EXISTS " + IChatSessionTable.TableName 
				+ "(" + IChatSessionTable.F_SessionID + " VARCHAR(50) primary key, ";
		db.execSQL(addTmpField(createChatSessionTableSQL, 30));
		
		String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS " + IUserTable.TableName
				+ "(" + IUserTable.F_UserId + " VARCHAR(50) primary key, ";
		db.execSQL(addTmpField(createUsersTableSQL, 50));
		
		try {
			db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS MessageIdIndex on "+IMessageTable.PeopleTableName+"("+IMessageTable.F_MessageID+");");
			db.execSQL("CREATE INDEX IF NOT EXISTS MessageUserIndex on "+IMessageTable.PeopleTableName+"("+IMessageTable.F_RemoteUserId+");");
			db.execSQL("CREATE INDEX IF NOT EXISTS MessageStatusIndex on "+IMessageTable.PeopleTableName+"("+IMessageTable.F_Status+");");
		} catch (Exception e) {
			log.e(e);
		}
	}

	private String addTmpField(String sql, int filedCount) {
		for (int i = 1; i < filedCount; i++) {
			String field = ITable.DBFIELD_TMP + i + " NUMERIC";
			if (i < filedCount - 1) {
				field += ",";
			}

			sql += field;
		}

		sql += ")";

		return sql;
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		this.onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		log.i("db opened!");
	}

}
