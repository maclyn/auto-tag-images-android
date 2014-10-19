package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImageViewer extends Activity {
    SQLiteDatabase db;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        getActionBar().hide();

        //Intent has file_state type
        FileState fs = (FileState) getIntent().getSerializableExtra("filestate");
        img = (ImageView) this.findViewById(R.id.viewImage);
        Picasso.with(this).load(new File(fs.getPath())).into(img);

        db = ((AutotagApp)this.getApplication()).getDatabase();
        List<Integer> tagIds = fs.getIds();
        String selection = "";
        List<String> selectionArgs = new ArrayList<String>();
        for(int i = 0; i < tagIds.size(); i++){
            selection += DatabaseHelper.COLUMN_ID + "=? AND";
            selectionArgs.add(String.valueOf(i));
        }
        selection = selection.substring(0, selection.length()-4);
        String selectionArgsA[] = new String[selectionArgs.size()];
        Cursor tags =
                db.query(DatabaseHelper.TABLE_TAGS, null, selection, selectionArgs.toArray(selectionArgsA), null, null, null);
        if(tags.moveToFirst()){

        }
        tags.close();
    }
}
