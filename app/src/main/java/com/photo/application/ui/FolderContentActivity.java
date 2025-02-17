package com.photo.application.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.photo.application.R;
import com.squareup.picasso.Picasso;

public class FolderContentActivity extends AppCompatActivity {

    ImageView imageView;
    String imageUriString;
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_content);
        imageView = findViewById(R.id.imageView);

        imageUriString = getSelectedUri();


        if (imageUriString != null) {
            //loadImagesFromDirectory(imageUriString);
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
        } else {

        }
    }

    private void loadImagesFromDirectory(String imageUriString) {
        Uri contentUri = Uri.parse(imageUriString);
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, contentUri);

        if (documentFile != null && documentFile.isDirectory()) {
            for (DocumentFile file : documentFile.listFiles()) {
                if (file.isFile() && isImageFile(file)) {
                    // Display all images (Modify this as per your UI)
                    downloadAndDisplayImage(file.getUri());
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri treeUri = data.getData();
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                loadImagesFromDirectory(treeUri.toString());
            }
        }
    }

//    private void loadImagesFromDirectory(String imageUriString) {
//        Uri contentUri = Uri.parse(imageUriString);
//        DocumentFile documentFile = DocumentFile.fromTreeUri(this, contentUri);
//
//        if (documentFile != null && documentFile.isDirectory()) {
//            for (DocumentFile file : documentFile.listFiles()) {
//                if (file.isFile() && isImageFile(file)) {
//                    // Display the first image found
//                    downloadAndDisplayImage(file.getUri());
//                    break; // Exit after displaying the first image
//                }
//            }
//        }
//    }

    private boolean isImageFile(DocumentFile file) {
        String mimeType = getContentResolver().getType(file.getUri());
        return mimeType != null && mimeType.startsWith("image/");
    }

    private String getSelectedUri() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("selected_folder_uri", null);
    }

    private void downloadAndDisplayImage(Uri imageUri) {
        // Use Picasso or Glide library to download and display the image
        Picasso.get().load(imageUri).into(imageView);
        // Or use Glide
        // Glide.with(this).load(imageUri).into(imageView);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
