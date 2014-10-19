package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements ActionBar.OnNavigationListener {
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
                        }),
                this);
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

                return new View(context);
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
        public static UntaggedFragment newInstance() {
            UntaggedFragment fragment = new UntaggedFragment();
            return fragment;
        }

        public UntaggedFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
