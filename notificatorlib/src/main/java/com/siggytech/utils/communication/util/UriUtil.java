package com.siggytech.utils.communication.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;

public class UriUtil {

    public static String getRealPathFromURI(Context context, Uri uri) {
        Uri queryUri = MediaStore.Files.getContentUri("external");
        if(uri.toString().contains("/storage/emulated/0")
                || uri.toString().contains("/storage/sdcard0"))
            queryUri = MediaStore.Files.getContentUri("internal");

        String columnData = MediaStore.Files.FileColumns.DATA;
        String columnSize = MediaStore.Files.FileColumns.SIZE;

        String[] projectionData = {MediaStore.Files.FileColumns.DATA};


        String name = null;
        String size = null;

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if ((cursor != null) && (cursor.getCount() > 0)) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

            cursor.moveToFirst();

            name = cursor.getString(nameIndex);
            size = cursor.getString(sizeIndex);

            cursor.close();
        }

        String imagePath = "";
        if ((name != null) && (size != null)) {
            String selectionNS = columnData + " LIKE '%" + name + "' AND " + columnSize + "='" + size + "'";

            Cursor cursorLike = context.getContentResolver().query(queryUri, projectionData, selectionNS, null, null);

            if ((cursorLike != null) && (cursorLike.getCount() > 0)) {
                cursorLike.moveToFirst();
                int indexData = cursorLike.getColumnIndex(columnData);
                if (cursorLike.getString(indexData) != null) {
                    imagePath = cursorLike.getString(indexData);
                }
                cursorLike.close();
            }
        }

        return imagePath;
    }

    /**
     * Converts base64 to file and returns file absolute path
     * @param encoded base64 file
     * @param fileName name of file with extension
     * @return file absolute path
     */
    public static Uri base64ToUri(String encoded, String fileName) throws Exception {
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        return byteToUri(decoded,fileName);
    }

    /**
     * Converts byte [] to file and returns file absolute path
     * @param decoded data
     * @param fileName name of file with extension
     * @return file absolute path
     */
    public static Uri byteToUri(byte[] decoded, String fileName) throws Exception {

        try {
            File file2 = FileUtil.getFile(Conf.ROOT_FOLDER,fileName);
            FileOutputStream os = new FileOutputStream(file2, true);
            os.write(decoded);
            os.close();

            return Uri.fromFile(file2);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
