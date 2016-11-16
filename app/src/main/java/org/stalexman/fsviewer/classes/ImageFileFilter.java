package org.stalexman.fsviewer.classes;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Алекс on 07.11.2016.
 */

public class ImageFileFilter {
    File file;
    private final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};

    public ImageFileFilter(File file) {
        this.file = file;
    }

    public boolean accept(File file) {
        for (String extension : okFileExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    public boolean accept() {
        for (String extension : okFileExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}