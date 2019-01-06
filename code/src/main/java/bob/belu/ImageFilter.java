package bob.belu;

import java.io.File;
import java.io.FileFilter;

public class ImageFilter implements FileFilter {

    private final boolean withDirs;

    public ImageFilter(final boolean withDirs) {
        this.withDirs = withDirs;
    }

    @Override
    public boolean accept(File f) {
        final boolean x;
        if (null == f) {
            x = false;
        } else if (withDirs && f.isDirectory()) {				
            x = ! f.getName().matches("\\..+");
        } else {
            x = (null == f ? false : (f.getName().matches(".+\\.[jJ][pP][gG]$") || f.getName().matches(".+\\.[pP][nN][gG]$")));
        }
        return x;
    }

}