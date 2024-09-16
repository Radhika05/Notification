package com.photo.application.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.photo.application.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {


    private static final int PICK_IMAGE_REQUEST = 1;
    private Button uploadButton;
    private Uri selectedImageUri;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_PICK_FOLDER = 2;
    private static final int REQUEST_TAKE_PHOTO = 3;

    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 100;

    private String selectedFolderPath;
    private Uri selectedUri;
    private Uri photoURI;
    private int selectedHour = 20;

    LinearLayout notification;
    private String currentPhotoPath;

    // Step 5: Save the captured image to the selected folder


    private void saveImageToSelectedFolder(Uri imageUri) {
        // Retrieve the selected folder URI from persistent storage
        Uri folderUri = getSavedFolderUri();
        if (folderUri == null) {
            Toast.makeText(this, "No folder selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a DocumentFile from the folder Uri
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, folderUri);

        if (pickedDir != null && pickedDir.isDirectory()) {
            // Create a unique filename for the image
            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";

            // Create a new image file in the selected folder
            DocumentFile imageFile = pickedDir.createFile("image/jpeg", fileName);

            if (imageFile != null) {
                try {
                    // Write the content from the Uri to the new file
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    OutputStream outputStream = getContentResolver().openOutputStream(imageFile.getUri());

                    if (inputStream != null && outputStream != null) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                        Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                    }

                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    scheduleNotification();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Selected folder is not valid", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveImageToSelectedFolder() {
        // Retrieve the selected folder URI from persistent storage
        Uri folderUri = getSavedFolderUri();
        if (folderUri == null) {
            Toast.makeText(this, "No folder selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a DocumentFile from the folder Uri
        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, folderUri);

        if (pickedDir != null && pickedDir.isDirectory()) {
            // Create a unique filename for the image
            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";

            // Create a new image file in the selected folder
            DocumentFile imageFile = pickedDir.createFile("image/jpeg", fileName);

            if (imageFile != null) {
                try {
                    // Write the bitmap (image) to the file
                    OutputStream outputStream = getContentResolver().openOutputStream(imageFile.getUri());
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);  // Ensure currentPhotoPath holds the file path of the captured image
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                    }
                    outputStream.close();
                    scheduleNotification();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Selected folder is not valid", Toast.LENGTH_SHORT).show();
        }
    }


    // Helper method to save the folder URI persistently (e.g., SharedPreferences)
    private void saveFolderUri(Uri folderUri) {
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_folder_uri", folderUri.toString());
        editor.apply();
    }

    // Helper method to get the saved folder URI from persistent storage
    private Uri getSavedFolderUri() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String uriString = sharedPreferences.getString("selected_folder_uri", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button chooseFolderButton = findViewById(R.id.btnChooseFolder);
        Button captureButton = findViewById(R.id.btnCapture);
        Button btnSetNotification = findViewById(R.id.btnSetNotification);
        Button submitBtn = findViewById(R.id.submitBtn);
        Button uploadButton = findViewById(R.id.btnUpload);
        Spinner durationSpinner = findViewById(R.id.durationSpinner);
        notification = findViewById(R.id.notification);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.duration_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedHour = Integer.parseInt(selectedItem.replaceAll("[^0-9]", ""));
                if (selectedItem.toLowerCase().contains("pm")) {
                    selectedHour += 12;
                }
                Toast.makeText(MainActivity.this, "Selected hour: " + selectedHour, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_POST_NOTIFICATIONS);
            }
        }

        chooseFolderButton.setOnClickListener(v -> openFolderPicker());

        btnSetNotification.setOnClickListener(v -> {
            if (photoURI != null) {
                notification.setVisibility(View.VISIBLE);
            } else {
                notification.setVisibility(View.GONE);
                Toast.makeText(this, "Please Select Image To Set Notification.", Toast.LENGTH_SHORT).show();
            }
        });

        submitBtn.setOnClickListener(v -> scheduleNotification());

        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        captureButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent();
            } else {
                requestCameraPermission();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("ImageCapture", "Error creating file", ex);
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.photo.application.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        //}
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null); // App-specific directory
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_PICK_FOLDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission denied. Cannot open camera.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with creating notifications
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FOLDER && resultCode == RESULT_OK && data != null) {
            if (data != null && data.getData() != null) {
                // Get and save the selected folder URI
                Uri folderUri = data.getData();
                saveFolderUri(folderUri);
            } else {
                Log.e("FolderSelection", "No folder selected");
                Toast.makeText(this, "Folder selection failed.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (photoURI != null) {
                saveImageToSelectedFolder();
            } else {
                Log.e("ImageCapture", "Photo URI is null");
                Toast.makeText(this, "Failed to capture photo.", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            saveImageToSelectedFolder(selectedImageUri);
        }
    }



    private void scheduleNotification() {
        Calendar currentTime = Calendar.getInstance();

        // Schedule 1st notification after 1 hour
        Calendar firstNotificationTime = (Calendar) currentTime.clone();
        firstNotificationTime.add(Calendar.HOUR_OF_DAY, 1);
        scheduleNotification(firstNotificationTime);

        // Schedule 2nd notification after 8 hours
        Calendar secondNotificationTime = (Calendar) currentTime.clone();
        secondNotificationTime.add(Calendar.HOUR_OF_DAY, 8);
        scheduleNotification(secondNotificationTime);

        // Schedule 3rd notification after 1 day
        Calendar thirdNotificationTime = (Calendar) currentTime.clone();
        thirdNotificationTime.add(Calendar.DAY_OF_MONTH, 1);
        secondNotificationTime.add(Calendar.HOUR_OF_DAY, selectedHour);
        scheduleNotification(thirdNotificationTime);

        // Schedule 4th notification after 14 days
        Calendar fourthNotificationTime = (Calendar) currentTime.clone();
        fourthNotificationTime.add(Calendar.DAY_OF_MONTH, 14);
        secondNotificationTime.add(Calendar.HOUR_OF_DAY, selectedHour);
        scheduleNotification(fourthNotificationTime);

        // Schedule 5th notification after 60 days
        Calendar fifthNotificationTime = (Calendar) currentTime.clone();
        fifthNotificationTime.add(Calendar.DAY_OF_MONTH, 60);
        secondNotificationTime.add(Calendar.HOUR_OF_DAY, selectedHour);
        scheduleNotification(fifthNotificationTime);
    }

    private void scheduleNotification(Calendar notificationTime) {
        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setInitialDelay(calculateDelay(notificationTime), TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueue(notificationWorkRequest);
    }

    private long calculateDelay(Calendar notificationTime) {
        long currentTimeInMillis = System.currentTimeMillis();
        long notificationTimeInMillis = notificationTime.getTimeInMillis();
        return notificationTimeInMillis - currentTimeInMillis;
    }
}
