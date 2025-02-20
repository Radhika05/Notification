package com.photo.application.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.photo.application.R;
import com.squareup.picasso.Picasso;

public class FolderContentActivity extends AppCompatActivity {

    ImageView imageView;
    String imageUriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_content);
        imageView = findViewById(R.id.imageView);

        // Retrieve the saved image file URI from SharedPreferences
        imageUriString = getSelectedImageUri();

        if (imageUriString != null) {
            // Directly display the image
            downloadAndDisplayImage(Uri.parse(imageUriString));
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedImageUri() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("selected_image_uri", null);
    }

    private void downloadAndDisplayImage(Uri imageUri) {
        Picasso.get()
                .load(imageUri)
                .into(imageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
