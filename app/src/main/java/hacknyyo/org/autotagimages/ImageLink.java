package hacknyyo.org.autotagimages;

/**
 * Created by Maclyn on 10/19/2014.
 */
public class ImageLink {
    private String name;
    private String path;
    private String thumbnailId;

    public ImageLink(String path, String thumbnailId, String name){
        this.name = name;
        this.path = path;
        this.thumbnailId = thumbnailId;
    }

    public String getName() { return name; }

    public String getPath() {
        return path;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }
}
