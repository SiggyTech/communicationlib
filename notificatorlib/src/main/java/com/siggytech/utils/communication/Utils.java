package com.siggytech.utils.communication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    break;
                case 1:
                    sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
                    break;
                default:
                    sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
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
        String path = Conf.ROOT_PATH +fileName;
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


    /**
     * For files that be selected by file choose.
     * @param context context
     * @param uri file's uri
     * @return File
     */
    public static File CompressImage(Context context, Uri uri){
        return CompressImage(getRealPathFromURI(context, uri));
    }

    /**
     * Compress Image like what's app
     * @param filePath file path
     * @return File
     */
    public static File CompressImage(String filePath) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

    //      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
    //      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

    //      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

    //      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

    //      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

    //      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

    //      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
    //          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

    //      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

    //          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new File(filename);

    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        Uri queryUri = MediaStore.Files.getContentUri("external");
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

    public static String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "SIGGI/");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriString = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriString;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

}