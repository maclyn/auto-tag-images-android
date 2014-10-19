package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements ActionBar.OnNavigationListener {
    public static final String TAG = "MainActivity";

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    ImageTagger t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        actionBar.setListNavigationCallbacks(
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[] {
                                "Tagged",
                                "Untagged"
                        }), this);
        t = new ImageTagger();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_tag_all) {
            return true;
        } else if (id == R.id.action_test) {
            t.setAccessToken();
        } else if (id == R.id.action_test_2) {
            t.getTag();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        if(position == 0) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, TaggedFragment.newInstance())
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, UntaggedFragment.newInstance())
                    .commit();
        }
        return true;
    }

    public static class TaggedFragment extends Fragment {
        public class TagAdapter extends BaseAdapter {
            List<Tag> tags;
            LayoutInflater li;
            Context context;

            public TagAdapter(Context context, List<Tag> tags){
                this.context = context;
                this.tags = tags;
                this.li = ((Activity)context).getLayoutInflater();
            }

            @Override
            public int getCount() {
                return tags.size();
            }

            @Override
            public Object getItem(int position) {
                return tags.get(position);
            }

            @Override
            public long getItemId(int position) {
                return -1;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                RelativeLayout tagRow = (RelativeLayout) convertView;
                if(convertView == null){
                    tagRow = (RelativeLayout) li.inflate(R.layout.tagged_row, null);
                }

                //Set images
                //Get images from within tags
                List<String> files = tags.get(position).getFiles();
                for(int i = 0; i < 3 && i < tags.size(); i++){
                    ImageView iv;
                    switch(i){
                        case 0:
                            iv = (ImageView) tagRow.findViewById(R.id.rowThumb1);
                            break;
                        case 1:
                            iv = (ImageView) tagRow.findViewById(R.id.rowThumb2);
                            break;
                        case 2:
                            iv = (ImageView) tagRow.findViewById(R.id.rowThumb3);
                            break;
                        case 3:
                            iv = (ImageView) tagRow.findViewById(R.id.rowThumb4);
                            break;
                    }
                    Picasso.with(context).load("file:" + files.get(i));
                }

                //Set text
                ((TextView)tagRow.findViewById(R.id.tagName)).setText(tags.get(position).getName());
                return tagRow;
            }
        }

        ListView view;

        public static TaggedFragment newInstance() {
            TaggedFragment fragment = new TaggedFragment();
            return fragment;
        }

        public TaggedFragment() {
        }

        @Override
        public void onCreate(Bundle savedState){
            super.onCreate(savedState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            ListView rootView = (ListView) inflater.inflate(R.layout.fragment_tagged, container, false);
            view = rootView;
            SQLiteDatabase database = ((AutotagApp) this.getActivity().getApplication()).getDatabase();
            new AsyncTask<Object, Void, TagAdapter>() {
                @Override
                protected TagAdapter doInBackground(Object... params) {
                    SQLiteDatabase d = (SQLiteDatabase) params[0];
                    List<Tag> tags = new ArrayList<Tag>();
                    Cursor c = d.query(DatabaseHelper.TABLE_TAGS, null, null, null, null, null, DatabaseHelper.COLUMN_TAG_NAME + " desc");
                    if(c.moveToFirst()){
                        int nameColumn = c.getColumnIndex(DatabaseHelper.COLUMN_TAG_NAME);
                        int fileColumn = c.getColumnIndex(DatabaseHelper.COLUMN_FILE_PATHS);
                        while(!c.isAfterLast()){
                            //Convert | delimited file names
                            String files[] = c.getString(fileColumn).split("\\|");
                            List<String> fileList = new ArrayList<String>();
                            fileList.addAll(fileList);
                            tags.add(new Tag(fileList, c.getString(nameColumn)));
                            c.moveToNext();
                        }
                    }
                    return new TagAdapter(((Fragment)params[1]).getActivity(), tags);
                }

                @Override
                protected void onPostExecute(TagAdapter adapter) {
                    if(view != null) view.setAdapter(adapter);
                }
            }.execute(database, this);
            return rootView;
        }
    }

    public static class UntaggedFragment extends Fragment {
        public class ThumbHolder {
            String filePath;
            String thumbPath;
            String name;
        }

        public class PictureAdapter extends BaseAdapter {
            List<ThumbHolder> files;
            LayoutInflater li;
            Context context;

            public PictureAdapter(Context context, List<ThumbHolder> files) {
                this.context = context;
                this.files = files;
                this.li = ((Activity) context).getLayoutInflater();
            }

            @Override
            public int getCount() {
                return files.size();
            }

            @Override
            public Object getItem(int position) {
                return files.get(position);
            }

            @Override
            public long getItemId(int position) {
                return -1;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                SquareHolder sh;
                if (convertView == null) {
                    convertView = li.inflate(R.layout.image_square, null);
                    sh = new SquareHolder();
                    sh.imageView = (ImageView) convertView.findViewById(R.id.squareBg);
                    sh.textView = (TextView) convertView.findViewById(R.id.squareName);
                    convertView.setTag(sh);
                } else {
                    sh = (SquareHolder) convertView.getTag();
                }

                //Set image
                File f = new File(files.get(position).thumbPath);
                Log.d(TAG, "File: " + f.getPath());
                Picasso.with(context)
                        .load(Uri.fromFile(f))
                        .into(sh.imageView);

                sh.textView.setText(files.get(position).name);
                return convertView;
            }

            public class SquareHolder {
                public ImageView imageView;
                public TextView textView;
            }
        }

        public static UntaggedFragment newInstance() {
            UntaggedFragment fragment = new UntaggedFragment();
            return fragment;
        }

        public UntaggedFragment() {
        }

        @Override
        public void onCreate(Bundle state){
            super.onCreate(state);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ListView rootView = (ListView) inflater.inflate(R.layout.fragment_tagged, container, false);
            List<ThumbHolder> thl = new ArrayList<ThumbHolder>();
            //Check to see if files need to be tagged
            ContentResolver cr = this.getActivity().getContentResolver();
            Cursor c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.ImageColumns.DISPLAY_NAME,
                                MediaStore.Images.ImageColumns._ID,
                                MediaStore.Images.ImageColumns.DATA}, null, null,
                                MediaStore.Images.ImageColumns.DATE_TAKEN + " desc");
            if(c.moveToFirst()){
                int filePathColumn = c.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                int nameColumn = c.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                int thumbnail = c.getColumnIndex(MediaStore.Images.ImageColumns._ID); //ID in thumbnail column
                while(!c.isAfterLast()){
                    String path = c.getString(filePathColumn);
                    String name = c.getString(nameColumn);
                    int id = c.getInt(thumbnail);
                    Log.d(TAG, "Name: " + name);
                    Log.d(TAG, "Path: " + path);
                    Log.d(TAG, "Id: " + id);
                    if(thumbnail != 0){
                        //Query for path for thumbnail
                        Cursor c2 = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Images.Thumbnails.DATA},
                                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                                new String[]{String.valueOf(id)}, null);
                        if(c2.moveToFirst()){
                            int dataColumn = c2.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                            //We have a column; add it
                            ThumbHolder th = new ThumbHolder();
                            th.filePath = path;
                            th.name = name;
                            th.thumbPath = c2.getString(dataColumn);
                            thl.add(th);
                        }
                        c2.close();
                    }
                    c.moveToNext();
                }
            }
            c.close();

            //We have all the thumbnails; handle it
            PictureAdapter pa = new PictureAdapter(this.getActivity(), thl);
            rootView.setAdapter(pa);
            return rootView;
        }

        public void addToList(File f, List<String> files) {
            if (f.isDirectory() && f.getName().charAt(0) == '.') {
                return; //Hidden dir; we don't care
            } else if (f.isDirectory()) {
                for (File file : f.listFiles()) {
                    addToList(file, files);
                }
            } else {
                files.add(f.getPath());
            }
        }
    }
}
