package hacknyyo.org.autotagimages;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";

    //Database basics
    public static final String DATABASE_NAME = "database.db";
    public static final int DATABASE_VERSION = 1;

    //Base columns
    public static final String COLUMN_ID = "_id";

    //FileState table
    public static final String TABLE_FILE_STATE = "file_state";
    public static final String COLUMN_FILE_NAME = "file_name";
    public static final String COLUMN_TAGGED = "tagged";
    public static final String COLUMN_TAG_IDS = "tag_ids";

    //Tag table
    public static final String TABLE_TAGS = "tags";
    public static final String COLUMN_TAG_NAME = "tag_name";
    public static final String COLUMN_FILE_PATHS = "file_paths";

    //Functions for creating it
    private static final String FILE_STATE_TABLE_CREATE = "create table "
            + TABLE_FILE_STATE +
            "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_FILE_NAME + " text not null, "
            + COLUMN_TAG_IDS + " text not null, "
            + COLUMN_FILE_PATHS + " integer not null" + ");";
    private static final String TAGS_TABLE_CREATE = "create table "
            + TABLE_TAGS +
            "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TAG_NAME + " text not null, "
            + COLUMN_FILE_PATHS + " text not null" + ");";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FILE_STATE_TABLE_CREATE);
        db.execSQL(TAGS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrades. Whoopdy-doo.");
    }
}
