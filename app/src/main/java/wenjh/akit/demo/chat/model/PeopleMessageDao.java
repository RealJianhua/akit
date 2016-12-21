package wenjh.akit.demo.chat.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PeopleMessageDao extends AbsMessageDao {

	public PeopleMessageDao(SQLiteDatabase db) {
		super(db, PeopleTableName);
	}

	@Override
	protected void assemble(Message obj, Cursor cursor) {
		super.assemble(obj, cursor);
		obj.setChatSessionType(Message.CHATTYPE_PEOPLE);
	}
	
}
