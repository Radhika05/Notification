package com.photo.application.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;


public class PathHelper {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(Context context, Uri uri) {

        if (DocumentsContract.isDocumentUri(context, uri)) {
            // Check if the URI is a document URI

            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                // If the authority is com.android.externalstorage.documents,
                // it's likely a storage access framework URI

                // Extract the primary volume name
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String primaryVolume = split[0];

                // Handle primary volume "primary" (internal storage)
                if ("primary".equalsIgnoreCase(primaryVolume)) {
                    return context.getExternalFilesDir(null) + "/" + split[1];
                }
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                // Handle downloads provider URI

                // Extract the document ID
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = Uri.parse("content://downloads/public_downloads");

                // Append the document ID to the content URI
                final Uri downloadUri = Uri.withAppendedPath(contentUri, id);

                // Retrieve the file path using DocumentFile
                return getPathFromDocumentUri(context, downloadUri);
            } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                // Handle media provider URI

                // Extract the document ID
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                // Handle audio files
                if ("audio".equals(type)) {
                    final Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getPathFromContentUri(context, contentUri, selection, selectionArgs);
                }
            }
        }

        return null;
    }

    public static String getPathFromContentUri(Context context, Uri contentUri, String selection, String[] selectionArgs) {
        try (Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e("PathHelper", "Error getting path from content URI", e);
        }
        return null;
    }

    public static String getPathFromDocumentUri(Context context, Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null) {
            return documentFile.getUri().getPath();
        }
        return null;
    }
}
