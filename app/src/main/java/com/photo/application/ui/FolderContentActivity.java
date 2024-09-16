package com.photo.application.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.photo.application.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class FolderContentActivity extends AppCompatActivity {

    ImageView imageView;
    String imageUriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_content);
        imageView = findViewById(R.id.imageView);

        imageUriString = getSelectedUri();


        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            downloadAndDisplayImage(imageUri);
        } else {

        }
    }

    private String getSelectedUri() {
        return getSharedPreferences("PREFS_NAME", MODE_PRIVATE)
                .getString("SELECTED_URI_KEY", null);
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
