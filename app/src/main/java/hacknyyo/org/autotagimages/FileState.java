package hacknyyo.org.autotagimages;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class FileState {
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
}
