package com.siggytech.utils.communication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.view.View;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public class MESSAGE_TYPE{
        public static final String MESSAGE = "Message";
        public static final String AUDIO = "audio";
        public static final String PHOTO = "photo";
        public static final String VIDEO = "video";
        public static final String FILE = "file";
    }

    @SuppressLint("NewApi")
    public static int GenerateViewId() {

        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    /**
     * Method that gets a name for a picture
     * @return name of picture
     */
    public static String GetDateName(){
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.getTimeInMillis());
    }

    public static String GetStringDate(){
        String strDate = "";
        try {
            SimpleDateFormat sdf;
            switch (Conf.DATE_FORMAT) {
                case 0:
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    break;
                case 1:
                    sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
                    break;
                default:
                    sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
                    break;
            }

            Calendar cal = Calendar.getInstance();
            strDate = sdf.format(cal.getTime());
        }catch (Exception e){
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * Converts a File to Base64
     * @param file file
     * @return base64
     */
    public static String FileToBase64(File file){
        String base64="";
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();

            base64 = ToBase64(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }


    /**
     * Converts byte[] to base 64
     * @param byteArray data
     * @return base64
     */
    public static String ToBase64(byte[] byteArray){
        return Base64.encodeToString(byteArray,Base64.DEFAULT);
    }

    /**
     * Converts base64 to file and returns file absolute path
     * @param encoded base64 file
     * @param fileName name of file with extension
     * @return file absolute path
     */
    public static Uri Base64ToUrl(String encoded, String fileName) throws Exception {
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        String path = Conf.ROOT_PATH + "/"+fileName;
        try {
            File file2 = new File(path);
            FileOutputStream os = new FileOutputStream(file2, true);
            os.write(decoded);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return Uri.parse(path);
    }
    public static File getCompressedImageFile(File file, Context mContext) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            if (getFileExt(file.getName()).equals("png") || getFileExt(file.getName()).equals("PNG")) {
                o.inSampleSize = 6;
            } else {
                o.inSampleSize = 6;
            }

            FileInputStream inputStream = new FileInputStream(file);
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 100;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);

            ExifInterface ei = new ExifInterface(file.getAbsolutePath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    selectedBitmap = rotateImage(selectedBitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    selectedBitmap = rotateImage(selectedBitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    selectedBitmap = rotateImage(selectedBitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:

                default:
                    break;
            }
            inputStream.close();


            // here i override the original image file
            File folder = new File(Environment.getExternalStorageDirectory() + "/FolderName");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (success) {
                File newFile = new File(new File(folder.getAbsolutePath()), file.getName());
                if (newFile.exists()) {
                    newFile.delete();
                }
                FileOutputStream outputStream = new FileOutputStream(newFile);

                if (getFileExt(file.getName()).equals("png") || getFileExt(file.getName()).equals("PNG")) {
                    selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } else {
                    selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }

                return newFile;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}