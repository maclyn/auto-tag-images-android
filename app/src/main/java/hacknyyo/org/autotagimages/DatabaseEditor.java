package hacknyyo.org.autotagimages;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseEditor {
    public static final String TAG = "DatabaseEditor";

    public static void addTags(String path, String name, String thumbId, List<TagInfo> tags, SQLiteDatabase db){
        //(1) Check to see if there's already a tag for it, and if so store data
       List<String> ids = new ArrayList<String>();
       for(int i = 0; i < tags.size() && i < 3; i++) {
           TagInfo ti = tags.get(i);
           List<ImageLink> paths;

           //Check if need to merge
           Cursor c = db.query(DatabaseHelper.TABLE_TAGS, null,
                   DatabaseHelper.COLUMN_TAG_NAME + "=?",
                   new String[]{ ti.getClasses() },
                   null, null, null);
           if (c.moveToFirst()) {
               Log.d(TAG, ti.getClasses() + " has data already; merging");
               int filePaths = c.getColumnIndex(DatabaseHelper.COLUMN_FILE_PATHS);
               paths = fromImageLink(c.getString(filePaths));
               db.delete(DatabaseHelper.TABLE_TAGS, DatabaseHelper.COLUMN_TAG_NAME + "=?", new String[]{ti.getClasses()});
           } else {
               Log.d(TAG, ti.getClasses() + " doesn't have data; no merge");
               paths = new ArrayList<ImageLink>();
           }
           c.close();

           paths.add(new ImageLink(path, thumbId, name));

           ContentValues cv = new ContentValues();
           cv.put(DatabaseHelper.COLUMN_TAG_NAME, ti.getClasses());
           cv.put(DatabaseHelper.COLUMN_FILE_PATHS, toImageLink(paths));
           long newId = db.insert(DatabaseHelper.TABLE_TAGS, null, cv);
           ids.add(String.valueOf(newId));
        }

        //(2) Change file state
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_FILE_NAME, name);
        cv.put(DatabaseHelper.COLUMN_FILE_PATH, path);
        cv.put(DatabaseHelper.COLUMN_TAG_IDS, toTagIds(ids));
        db.insert(DatabaseHelper.TABLE_FILE_STATE, null, cv);
    }

    public static List<ImageLink> fromImageLink(String compressed){
        List<ImageLink> links = new ArrayList<ImageLink>();
        String split[] = compressed.split("\n");
        for(String s : split){
            String further[] = s.split("\\|");
            links.add(new ImageLink(further[0], further[1], further[2]));
        }
        return links;
    }

    public static String toImageLink(List<ImageLink> uncompressed){
        String to = "";
        for(ImageLink il : uncompressed){
            to += il.getPath() + "|" + il.getThumbnailId() + "|" + il.getName() + "\n";
        }
        return to.substring(0, to.length()-1);
    }

    public static List<String> fromTagIds(String compressed){
        List<String> ids = new ArrayList<String>();
        String split[] = compressed.split(",");
        for(String s : split){
            ids.add(s);
        }
        return ids;
    }

    public static String toTagIds(List<String> uncompressed){
        String to = "";
        for(String s: uncompressed){
            to += s + ",";
        }
        return to.substring(0, to.length()-1);
    }
}
