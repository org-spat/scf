/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spat.scf.serializer.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class FileHelper {

    /**
     * get all file which in dir
     * @param dir
     * @param extension
     * @return
     */
    public static List<File> getFiles(String dir, String... extension) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            return null;
        }

        List<File> fileList = new ArrayList<File>();
        getFiles(f, fileList, extension);

        return fileList;
    }

    /**
     *
     * @param file
     * @param fileList
     * @param extension
     */
    private static void getFiles(File f, List<File> fileList, String... extension) {
        File[] files = f.listFiles();
        if(files==null){
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                getFiles(files[i], fileList, extension);
            } else if (files[i].isFile()) {

                String fileName = files[i].getName().toLowerCase();
                boolean isAdd = false;
                if (extension != null) {
                    for (String ext : extension) {
                        if (fileName.lastIndexOf(ext) == fileName.length() - ext.length()) {
                            isAdd = true;
                            break;
                        }
                    }
                }

                if (isAdd) {
                    fileList.add(files[i]);
                }
            }
        }
    }
}
