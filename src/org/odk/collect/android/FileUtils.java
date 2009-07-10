package org.odk.collect.android;

import java.io.File;
import java.util.ArrayList;

public class FileUtils {
    
    
    public static ArrayList<String> getFilesAsArrayList(String path) {
        ArrayList<String> mFileList = new ArrayList<String>();
        File root = new File(path);
        
        if (root.isDirectory()) {
            File[] children = root.listFiles();
            for (File child : children) {
                mFileList.add(child.getName());
            }
        } else {
            String filename = root.getName();
            mFileList.add(filename);
        }
        return mFileList;
    }
    
    
    public static ArrayList<String> getFilesAsArrayListRecursive(String path) {
        ArrayList<String> mFileList = new ArrayList<String>();
        File root = new File(path);
        getFilesAsArrayListRecursiveHelper(root, mFileList);        
        return mFileList;
    }
    
    private static void getFilesAsArrayListRecursiveHelper(File f, ArrayList<String> filelist) {
        if (f.isDirectory()) {
            File[] childs = f.listFiles();
            for (File child : childs) {
                getFilesAsArrayListRecursiveHelper(child, filelist);
            }
            return;
        }
        filelist.add(f.getName());
    }
    
    
    public static boolean deleteFile(String path) {
        File f = new File(path);
        return f.delete();
    }

    

/*
    private static boolean getDirectory(String path) {

        // check to see if there's an sd card.
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return false;
        }

        // if storage directory does not exist, create it.
        boolean made = true;
        mRoot = new File(path);
        if (!mRoot.exists()) {
            made = mRoot.mkdirs();
        }

        if (!made) {
            return false;
        } else {
            return true;
        }
    }
*/
    
   

   
    
    
    }
