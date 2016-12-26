package wenjh.akit.demo.account.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.common.db.AKitDBOpenHandler;
import wenjh.akit.common.db.ITable;
import wenjh.akit.common.util.LogUtil;
import wenjh.akit.demo.account.model.IUserTable;
import wenjh.akit.demo.chat.model.IChatSessionTable;
import wenjh.akit.demo.chat.model.IMessageTable;

public class AccountDBOpenHandler extends AKitDBOpenHandler {
	private final static int DB_MIN_VERSION = 4;
	private static final int DB_CURRENT_VERSION = DB_MIN_VERSION;

	private LogUtil log = new LogUtil(getClass().getSimpleName());


	public AccountDBOpenHandler(Context context, String databaseName) {
		super(context, databaseName, DB_CURRENT_VERSION);
	}

	@Override
	protected void createTable(SQLiteDatabase db) {
		newTableBuilder(IMessageTable.PeopleTableName)
			.primaryKey(IMessageTable.F_MessageID, FieldType.Varchar)
			.newIndexBuilder()
				.nameField(IMessageTable.F_RemoteUserId)
				.nameIndex("MessageUserIndex")
				.build()
			.newIndexBuilder()
				.nameField(IMessageTable.F_Status)
				.nameIndex("MessageStatusIndex")
				.build()
			.build();

		newTableBuilder(IChatSessionTable.TableName)
				.primaryKey(IChatSessionTable.F_SessionID, FieldType.Varchar)
				.build();

		newTableBuilder(IUserTable.TableName)
				.primaryKey(IUserTable.F_UserId, FieldType.Varchar)
				.tempFieldCount(50)
				.build();
// demo
//		newTableBuilder("test1")
//				.primaryKey("v1", FieldType.Integer)
//				.autoincrement(true)
//				.build();
//		newTableBuilder("test2")
//				.primaryKey("v2", FieldType.Integer)
//				.tempFieldCount(55)
//				.build();
//		newTableBuilder("test3")
//			.newIndexBuilder().nameField(ITable.DBFIELD_TMP+"2").nameIndex("_vi3").unique(true).build()
//			.build();
//		newTableBuilder("test4").tempFieldCount(0)
//				.build();
	}
}
