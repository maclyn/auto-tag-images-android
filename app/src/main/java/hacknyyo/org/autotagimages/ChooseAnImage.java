package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ChooseAnImage extends Activity {
    public class TaggedImageAdapter extends BaseAdapter {
        List<ImageLink> files;
        LayoutInflater li;
        Context context;

        public TaggedImageAdapter(Context context, List<ImageLink> files) {
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
            File f = new File(files.get(position).getThumbnailId());
            Picasso.with(context)
                    .load(Uri.fromFile(f))
                    .into(sh.imageView);

            final String thumbId = files.get(position).getThumbnailId();
            final String path = files.get(position).getPath();
            final String name = files.get(position).getName();
            sh.textView.setText(files.get(position).getName());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //View the file
                    Intent i = new Intent(v.getContext(), ImageViewer.class);
                    List<ImageLink> il = new ArrayList<ImageLink>();
                    il.add(files.get(position));
                    i.putExtra("il", DatabaseEditor.toImageLink(il));
                    v.getContext().startActivity(i);
                }
            });
            return convertView;
        }

        public class SquareHolder {
            public ImageView imageView;
            public TextView textView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tagged);

        //Get a list of tags from intent
        String name = getIntent().getStringExtra("tag_name");
        List<ImageLink> links = DatabaseEditor.fromImageLink(getIntent().getStringExtra("tag_files"));
        Tag t = new Tag(links, name);
        this.getActionBar().setTitle(t.getName().substring(0, 1).toUpperCase() + t.getName().substring(1));
        ((ListView)this.findViewById(R.id.main_lv)).setAdapter(new TaggedImageAdapter(this, t.getFiles()));
    }
}
