package hacknyyo.org.autotagimages;

import java.util.List;

public class Tag {
    private List<String> files;
    private String name;

    public Tag(List<String> files, String name){
        this.files = files;
        this.name = name;
    }

    public List<String> getFiles() {
        return files;
    }

    public String getName() {
        return name;
    }
}
