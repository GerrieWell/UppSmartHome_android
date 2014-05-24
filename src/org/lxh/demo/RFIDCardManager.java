package org.lxh.demo;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//使用数据库保存
public class RFIDCardManager {
	private static  HashMap<Long, RFIDCard> idMaps;
	private Context c;
	private String gPassword; //管理员密码
	public static RFIDCardManager builder(){

		idMaps = new HashMap<Long, RFIDCardManager.RFIDCard>();
		return null;
		
	}
	private boolean registerID(long id){
		HashMap<Long, RFIDCard> idMap = null;
		RFIDCard card = new RFIDCard(id);
		String pw = null;
		String alias = null;
		//实例化一个窗口  设置别名
		card.alias = alias;
		idMaps.put(id,card);
		return true;
	}
	
	private boolean deleteCard(long id){
		idMaps.remove(id);
		return true;
	}
	
	private boolean verifyPassword(RFIDCard card,String pw){
		//String right = card.password;
		if(gPassword.equals(pw)){
			deleteCard(card.id);
			return true;
		}else
			return false;
	}
	
	class RFIDCard{
		public RFIDCard(long id) {
			super();
			this.id = id;
		}
		private long id;
		private String alias;
	}
	
	public class BooksDB extends SQLiteOpenHelper {
		private final static String DATABASE_NAME = "BOOKS.db";
		private final static int DATABASE_VERSION = 1;
		private final static String TABLE_NAME = "books_table";
		public final static String BOOK_ID = "book_id";
		public final static String BOOK_NAME = "book_name";
		public final static String BOOK_AUTHOR = "book_author";

		public BooksDB(Context context) {
			// TODO Auto-generated constructor stub
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		// 创建table
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE " + TABLE_NAME + " (" + BOOK_ID
					+ " INTEGER primary key autoincrement, " + BOOK_NAME
					+ " text, " + BOOK_AUTHOR + " text);";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
			db.execSQL(sql);
			onCreate(db);
		}

		public Cursor select() {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null,
					null);
			return cursor;
		}

		// 增加操作
		public long insert(String bookname, String author) {
			SQLiteDatabase db = this.getWritableDatabase();
			/* ContentValues */
			ContentValues cv = new ContentValues();
			cv.put(BOOK_NAME, bookname);
			cv.put(BOOK_AUTHOR, author);
			long row = db.insert(TABLE_NAME, null, cv);
			return row;
		}

		// 删除操作
		public void delete(int id) {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = BOOK_ID + " = ?";
			String[] whereValue = { Integer.toString(id) };
			db.delete(TABLE_NAME, where, whereValue);
		}

		// 修改操作
		public void update(int id, String bookname, String author) {
			SQLiteDatabase db = this.getWritableDatabase();
			String where = BOOK_ID + " = ?";
			String[] whereValue = { Integer.toString(id) };

			ContentValues cv = new ContentValues();
			cv.put(BOOK_NAME, bookname);
			cv.put(BOOK_AUTHOR, author);
			db.update(TABLE_NAME, cv, where, whereValue);
		}
	}
}
