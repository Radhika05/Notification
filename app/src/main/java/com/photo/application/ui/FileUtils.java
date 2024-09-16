package com.photo.application.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;


public class FileUtils {

        static String getFolderPathFromTreeUri(Uri treeUri, Context context) {
            String decodedUri = Uri.decode(treeUri.toString());


            Log.d("Decoded URI", decodedUri);

            String volumePath = getVolumePath(treeUri,context);
            if (volumePath != null) {
                String[] parts = treeUri.getPath().split("%3A"); // Split the path using "%3A" as separator
                if (parts.length > 1) {
                    String[] pathSegments = parts[1].split("/");
                    StringBuilder fullPathBuilder = new StringBuilder(volumePath);
                    for (String segment : pathSegments) {
                        if (!segment.isEmpty()) {
                            fullPathBuilder.append(File.separator).append(segment);
                        }
                    }
                    return fullPathBuilder.toString();
                }
            } else {

            }
            return null; //null if path segments are not found
        }





            private  static String getVolumePath(Uri treeUri,Context context) {
                String volumePath = null;
                String[] projection = { DocumentsContract.Root.COLUMN_DOCUMENT_ID, DocumentsContract.Root.COLUMN_ROOT_ID };
                try (Cursor cursor = context.getContentResolver().query(treeUri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        String rootId = cursor.getString(cursor.getColumnIndexOrThrow(DocumentsContract.Root.COLUMN_ROOT_ID));
                        if ("primary".equalsIgnoreCase(rootId)) {
                            volumePath = Environment.getExternalStorageDirectory().getPath();
                        } else {
                            volumePath = "/storage/" + rootId.split(":")[1];
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return volumePath;
            }





    }


