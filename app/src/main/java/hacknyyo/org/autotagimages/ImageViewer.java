package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageViewer extends Activity {
    SQLiteDatabase db;
    ImageView img;
    ImageLink il;

    float startY;

    public static final String TAG = "ImageViewer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        getActionBar().hide();

        //Intent has ImageLink type
        il = DatabaseEditor.fromImageLink(this.getIntent().getStringExtra("il")).get(0);
        img = (ImageView) this.findViewById(R.id.viewImage);
        Picasso.with(this)
                .load(new File(il.getPath()))
                .fit()
                .centerInside()
                .into(img);
        ((TextView)this.findViewById(R.id.viewName)).setText(il.getName());

        LinearLayout viewTagsContainer = (LinearLayout) this.findViewById(R.id.viewTagsContainer);
        //Set tags in the other layout by querying by tag id
        db = ((AutotagApp)this.getApplication()).getDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_FILE_STATE, null, DatabaseHelper.COLUMN_FILE_PATH + "=?",
                new String[]{il.getPath()}, null, null, null);
        if(c.moveToFirst()){
            int idsColumn = c.getColumnIndex(DatabaseHelper.COLUMN_TAG_IDS);
            List<String> ids = DatabaseEditor.fromTagIds(c.getString(idsColumn));
            String[] array = ids.toArray(new String[ids.size()]);
            Cursor c2 = db.query(DatabaseHelper.TABLE_TAGS, null, DatabaseHelper.COLUMN_ID + " IN (" + placeholders(array.length) + ")",
                    array, null, null, null);
            if(c2.moveToFirst()){
                int tagNameColumn = c2.getColumnIndex(DatabaseHelper.COLUMN_TAG_NAME);
                while(!c2.isAfterLast()){
                    TextView t = new TextView(this);
                    t.setTextSize(24f);
                    t.setTextColor(getResources().getColor(android.R.color.white));
                    t.setBackgroundColor(getResources().getColor(R.color.shade2));
                    t.setText(c2.getString(tagNameColumn));
                    viewTagsContainer.addView(t);
                    c2.moveToNext();
                }
            }
            c2.close();
        }
        c.close();
    }

    String placeholders(int l) {
        String s = "";
        s += "?";
        for (int i = 1; i < l; i++) {
            s += ",?";
        }
        return s;
    }
}
