package com.siggytech.utils.communication.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "siggyCommunication.db";
    private static final String TABLE_MESSAGE = "message_table";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_GROUP_ID = "group_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_FROM = "name_from";
    private static final String COLUMN_MINE = "mine";
    private static final String COLUMN_KEY_USER = "key_user";


    public DbHelper(Context context){
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table "+ TABLE_MESSAGE + "(" +
                        COLUMN_ID + " integer primary key autoincrement, "+
                        COLUMN_KEY_USER + " text , " +
                        COLUMN_GROUP_ID + " text , " +
                        COLUMN_FROM + " text , " +
                        COLUMN_MINE + " integer , "+
                        COLUMN_DATE + " text , "+
                        COLUMN_MESSAGE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_MESSAGE);
        onCreate(db);
    }

    public long insertMessage(MessageRaw messageRaw){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_ID,messageRaw.getIdGroup());
        values.put(COLUMN_KEY_USER,messageRaw.getUserKey());
        values.put(COLUMN_FROM,messageRaw.getFrom());
        values.put(COLUMN_MINE,messageRaw.getMine());
        values.put(COLUMN_DATE,messageRaw.getDate());
        values.put(COLUMN_MESSAGE,messageRaw.getMessage());

        long result  = db.insert(TABLE_MESSAGE,null, values);
        db.close();
        return result;
    }

    public List<MessageRaw> getMessage(long idGroup, String userKey, int offset, int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        List<MessageRaw> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                "(SELECT * FROM "+ TABLE_MESSAGE
                +" WHERE "+COLUMN_GROUP_ID+" = '"+idGroup+"'"
                +" AND "+COLUMN_KEY_USER+" = '"+userKey+"'"
                +" order by id DESC limit "+limit
                +" offset "+offset
                +" ) order by id ASC";

        Cursor cursor = db.rawQuery(query,null);

        if(cursor!=null && cursor.moveToFirst()){
            do{
                MessageRaw raw = new MessageRaw();
                raw.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                raw.setIdGroup(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_ID)));
                raw.setFrom(cursor.getString(cursor.getColumnIndex(COLUMN_FROM)));
                raw.setMine(cursor.getInt(cursor.getColumnIndex(COLUMN_MINE)));
                raw.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                raw.setMessage(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)));
                list.add(raw);
            }while (cursor.moveToNext());
        }
        db.close();
        return list;
    }

    public long getMessageCount(long idGroup, String userKey) {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(
                db,
                TABLE_MESSAGE,
                COLUMN_GROUP_ID+" = '"+idGroup+"'"
                +" AND "+COLUMN_KEY_USER+" = '"+userKey+"'");
        db.close();
        return count;
    }

    public void deleteHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGE,null,null);
    }
}
