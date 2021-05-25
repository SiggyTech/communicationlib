package com.siggytech.utils.communication.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;

public class FilePath {

    public static String getPath(Context context, Uri uri) {

        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            String[] split;
            if (isExternalStorageDocument(uri)) {
                split = DocumentsContract.getDocumentId(uri).split(":");
                if ("primary".equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                return null;
            } else if (isDownloadsDocument(uri)) {
                return getDataColumn(context, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(DocumentsContract.getDocumentId(uri))), null, null);
            } else if (!isMediaDocument(uri)) {
                return null;
            } else {
                split = DocumentsContract.getDocumentId(uri).split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                            Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                            :Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                            Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                            :Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                            Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                            :Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                return getDataColumn(context, contentUri, selection, new String[]{split[1]});
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else {
            return null;
        }
    }

    public static Uri getUriPath(Context context, Uri uri) {
       if(VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           String[] split = DocumentsContract.getDocumentId(uri).split(":");
           String type = split[0];
           Uri contentUri = null;
           if ("image".equals(type)) {
               contentUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                       Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                       : Media.EXTERNAL_CONTENT_URI;
           } else if ("video".equals(type)) {
               contentUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                       Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                       : Video.Media.EXTERNAL_CONTENT_URI;
           } else if ("audio".equals(type)) {
               contentUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                       Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                       : Audio.Media.EXTERNAL_CONTENT_URI;
           }
           String selection = "_id=?";
           return ContentUris.withAppendedId(contentUri,getIDColumn(context, contentUri, selection, new String[]{split[1]}));
       }
        return uri;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String DATA = "_data";
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{DATA}, selection, selectionArgs, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }

            return cursor.getString(cursor.getColumnIndexOrThrow(DATA));
        }
    }


    public static Long getIDColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String _ID = "_id";
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{_ID}, selection, selectionArgs, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }

            return cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
        }
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}