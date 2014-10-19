package hacknyyo.org.autotagimages;

import java.util.List;

public class Tag {
    private List<ImageLink> files;
    private String name;

    public Tag(List<ImageLink> files, String name){
        this.files = files;
        this.name = name;
    }

    public List<ImageLink> getFiles() {
        return files;
    }

    public String getName() {
        return name;
    }
}
