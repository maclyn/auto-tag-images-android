package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static hacknyyo.org.autotagimages.MainActivity.UntaggedFragment.*;


public class MainActivity extends Activity implements ActionBar.OnNavigationListener, ImageTagger.BackGroundTaskListener {
    public static final String TAG = "MainActivity";
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private ArrayList<TagInfo> tagInfos;
    Dialog d;
    public ImageTagger t;

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
        t.setAccessToken();
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
            //Tag all the images remaining
            List<ThumbHolder> holders = getHolders(this, this.getContentResolver());
            for(ThumbHolder h : holders) {
                t.getTag(this, ((AutotagApp) this.getApplication()).getDatabase(), h.filePath,
                        h.name, h.thumbPath);
            }
            return true;
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
                    .replace(R.id.container, newInstance())
                    .commit();
        }
        return true;
    }

    @Override
    public void setTagInfos(ArrayList<TagInfo> tags) {
        tagInfos = tags;
        if(d != null){
            d.dismiss();
        }

        //Add it to the database
        Toast.makeText(this, "Tagged.", Toast.LENGTH_SHORT).show();
    }

    public static class TaggedFragment extends Fragment {
        public void tagAll(){

        }

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
            public View getView(final int position, View convertView, ViewGroup parent) {
                RelativeLayout tagRow = (RelativeLayout) convertView;
                if(convertView == null){
                    tagRow = (RelativeLayout) li.inflate(R.layout.tagged_row, null);
                }

                //Get images from within tags
                List<ImageLink> files = tags.get(position).getFiles();
                int i;
                for(i = 0; i < 3 && i < files.size(); i++){
                    ImageView iv = null;
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
                        default:
                            iv = (ImageView) tagRow.findViewById(R.id.rowThumb4);
                            break;
                    }
                    Picasso.with(context).load("file:" + files.get(i).getThumbnailId()).into(iv);
                }
                //Whatever's left, unload the image
                if(i < 3){
                    for(int j = i; j < 3; j++){
                        ImageView iv = null;
                        switch(i){
                            case 0:
                                iv = (ImageView) tagRow.findViewById(R.id.rowThumb1);
                                iv.setImageDrawable(new ColorDrawable(R.color.shade1));
                                break;
                            case 1:
                                iv = (ImageView) tagRow.findViewById(R.id.rowThumb2);
                                iv.setImageDrawable(new ColorDrawable(R.color.shade2));
                                break;
                            case 2:
                                iv = (ImageView) tagRow.findViewById(R.id.rowThumb3);
                                iv.setImageDrawable(new ColorDrawable(R.color.shade3));
                                break;
                            default:
                                iv = (ImageView) tagRow.findViewById(R.id.rowThumb4);
                                iv.setImageDrawable(new ColorDrawable(R.color.shade4));
                                break;
                        }
                    }
                }

                //Set text
                ((TextView)tagRow.findViewById(R.id.tagName)).setText(tags.get(position).getName());
                tagRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), ChooseAnImage.class);
                        i.putExtra("tag_name", tags.get(position).getName());
                        i.putExtra("tag_files", DatabaseEditor.toImageLink(tags.get(position).getFiles()));
                        v.getContext().startActivity(i);
                    }
                });
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
                    Cursor c = d.query(DatabaseHelper.TABLE_TAGS, null, null, null, null, null, DatabaseHelper.COLUMN_TAG_NAME + " asc");
                    if(c.moveToFirst()){
                        int nameColumn = c.getColumnIndex(DatabaseHelper.COLUMN_TAG_NAME);
                        int fileColumn = c.getColumnIndex(DatabaseHelper.COLUMN_FILE_PATHS);
                        while(!c.isAfterLast()){
                            tags.add(new Tag(DatabaseEditor.fromImageLink(c.getString(fileColumn)),
                                    c.getString(nameColumn)));
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

    public void showDialog(){
        d = ProgressDialog.show(this, "Tagging", "Getting tags...");
    }

    public static class UntaggedFragment extends Fragment {
        public class PictureAdapter extends BaseAdapter {
            ImageTagger t;
            List<ThumbHolder> files;
            LayoutInflater li;
            Context context;

            public PictureAdapter(Context context, List<ThumbHolder> files, ImageTagger t) {
                this.context = context;
                this.files = files;
                this.li = ((Activity) context).getLayoutInflater();
                this.t = t;
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
            public View getView(final int position, View convertView, ViewGroup parent) {
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
                Picasso.with(context)
                        .load(Uri.fromFile(f))
                        .into(sh.imageView);

                final String thumbId = files.get(position).thumbPath;
                final String path = files.get(position).filePath;
                final String name = files.get(position).name;
                sh.textView.setText(files.get(position).name);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Upload the file
                        if(files.get(position).hasFired){
                            Toast.makeText(context, "You've already tagged this.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        files.get(position).hasFired = true;
                        String toUploadPath = files.get(position).filePath;
                        ((MainActivity)context).showDialog();
                        t.getTag(context, ((AutotagApp)((MainActivity)context).getApplication()).getDatabase(),
                                toUploadPath, name, thumbId);
                    }
                });
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

            List<ThumbHolder> realHolders = getHolders(getActivity(),
                    getActivity().getContentResolver());
            //We have all the thumbnails; handle it
            PictureAdapter pa = new PictureAdapter(this.getActivity(), realHolders,
                    ((MainActivity)this.getActivity()).t);
            rootView.setAdapter(pa);
            return rootView;
        }
    }

    public static List<ThumbHolder> getHolders(Activity a, ContentResolver cr){
        List<ThumbHolder> thl = new ArrayList<ThumbHolder>();
        //Check to see if files need to be tagged
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
                //Log.d(TAG, "Name: " + name);
                //Log.d(TAG, "Path: " + path);
                //Log.d(TAG, "Id: " + id);
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
                        th.hasFired = false;
                        thl.add(th);
                    }
                    c2.close();
                }
                c.moveToNext();
            }
        }
        c.close();

        //Compare this with file_state table
        List<String> alreadyTagged = new ArrayList<String>();
        Cursor cs = ((AutotagApp)a.getApplication()).getDatabase().query(
                DatabaseHelper.TABLE_FILE_STATE, null, null, null, null, null, null, null);
        if(cs.moveToFirst()){
            int nameField = cs.getColumnIndex(DatabaseHelper.COLUMN_FILE_PATH);
            while(!cs.isAfterLast()){
                alreadyTagged.add(cs.getString(nameField));
                cs.moveToNext();
            }
        }

        List<ThumbHolder> realHolders = new ArrayList<ThumbHolder>();
        for(ThumbHolder thumbHolder : thl){
            if(!alreadyTagged.contains(thumbHolder.filePath)){
                realHolders.add(thumbHolder);
            }
        }
        return realHolders;
    }
}
