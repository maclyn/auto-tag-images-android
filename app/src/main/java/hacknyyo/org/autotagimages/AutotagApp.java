package hacknyyo.org.autotagimages;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class AutotagApp extends Application {
    DatabaseHelper dh;
    SQLiteDatabase db;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    synchronized public SQLiteDatabase getDatabase(){
        if(dh == null){
            dh = new DatabaseHelper(this);
        }
        if(db == null){
            db = dh.getWritableDatabase();
        }

        return db;
    }
}
