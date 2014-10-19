package hacknyyo.org.autotagimages;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Maclyn on 10/19/2014.
 */
public class FileState implements Parcelable {
    private String path;
    private List<Integer> ids;

    public FileState(String path, List<Integer> ids){
        this.path = path;
        this.ids = ids;
    }

    public String getPath() {
        return path;
    }

    public List<Integer> getIds() {
        return ids;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
