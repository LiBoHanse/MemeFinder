package com.cs125.memefinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PicManage {

    public static List<PicEntry> entryList = new LinkedList<>();
    public static Context context;
    public static File dirPic;

    public static void init(Context toContext) {
        context = toContext;
        dirPic = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MemeFinder/");
        dirPic.mkdir();
        readFromFile();
        fillURI();
    }

    public static void readFromFile() {
        Gson gson = new Gson();
        try (InputStreamReader inputStreamReader = new InputStreamReader(context.openFileInput("textfile.json"))) {
            entryList = gson.fromJson(inputStreamReader, new TypeToken<LinkedList<PicEntry>>() {}.getType());
        } catch (Throwable e) {
            e.printStackTrace();
            saveToFile();
        }
    }

    public static void saveToFile() {
        Gson gson = new Gson();
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("textfile.json", Context.MODE_PRIVATE))) {
            gson.toJson(entryList, outputStreamWriter);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static List<PicEntry> search(String toSearch) {
        List<PicEntry> toR = new LinkedList<>();
        for (PicEntry entry : entryList) {
            if (entry.fileText.contains(toSearch)) {
                toR.add(entry);
            }
        }
        return toR;
    }

    public static void fillURI() {
        for (PicEntry entry : entryList) {
            try {
                File theFile = new File(dirPic, entry.fileName);
                //entry.fileUri = Uri.fromFile(theFile);
                entry.fileUri = getImageContentUri(theFile);
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    public static void addPic(Uri picToAdd, String toText) {
        try {
            InputStream in = context.getContentResolver().openInputStream(picToAdd);
            String mimeType = context.getContentResolver().getType(picToAdd);
            String suffix = mimeType.substring(mimeType.lastIndexOf("/") + 1);
            String filename = Integer.toHexString(picToAdd.hashCode()) + "." + suffix;
            File file = new File(dirPic, filename);
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            entryList.add(new PicEntry(filename, toText, picToAdd));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        saveToFile();
    }

    public static void removePic(PicEntry toRemove) {
        entryList.remove(toRemove);
        saveToFile();
        File theFile = new File(dirPic, toRemove.fileName);
        theFile.delete();
    }

    public static void editText(PicEntry targetEntry, String newText) {
        targetEntry.fileText = newText;
        saveToFile();
    }

    public static Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
