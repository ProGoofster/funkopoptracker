package com.example.funkopoptracker;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.funkopoptracker.database.FunkoContentProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class AddPopFragment extends Fragment {

    private ImageView imageViewPreview;
    private Uri selectedImageUri;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imageViewPreview.setImageURI(selectedImageUri);
                        imageViewPreview.setVisibility(View.VISIBLE); // Show the image
                        cameraImageUri = null; // Clear camera URI since we're using gallery
                    }
                }
            }
        );

        // Register the camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    imageViewPreview.setImageURI(cameraImageUri);
                    imageViewPreview.setVisibility(View.VISIBLE); // Show the image
                    selectedImageUri = cameraImageUri; // Use the camera image
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pop, container, false);

        EditText editTextName = view.findViewById(R.id.editTextName);
        EditText editTextNumber = view.findViewById(R.id.editTextNumber);
        imageViewPreview = view.findViewById(R.id.imageViewPreview);
        Button buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);
        Button buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        Button buttonAdd = view.findViewById(R.id.buttonAdd);

        // Camera button
        buttonTakePhoto.setOnClickListener(v -> {
            try {
                // Create a file to store the photo
                File photoFile = createImageFile();
                if (photoFile != null) {
                    cameraImageUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.funkopoptracker.fileprovider",
                        photoFile
                    );
                    cameraLauncher.launch(cameraImageUri);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to open camera", Toast.LENGTH_SHORT).show();
            }
        });

        // Image selection button
        buttonSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        buttonAdd.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String numberText = editTextNumber.getText().toString();

            if (name.isEmpty() || numberText.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in name and number", Toast.LENGTH_SHORT).show();
                return;
            }

            int number = Integer.parseInt(numberText);

            // Save image to internal storage and get the file path
            String imagePath = null;
            if (selectedImageUri != null) {
                imagePath = saveImageToInternalStorage(selectedImageUri, name + "_" + number);
            }

            int rarity = 0;

            //TODO: implement seeded-weighted rarity generated from number

            FunkoPop newPop = new FunkoPop(name, number, rarity, imagePath);

            //database stuff
            getActivity().getContentResolver().insert(FunkoContentProvider.CONTENT_URI_OWNED, newPop.toContentValues());

            //toast
            String toastText = "FunkoPop: " + newPop.getName() + " #" + newPop.getNumber() + " added to Database!";
            Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();

            //clear the form
            editTextName.setText("");
            editTextNumber.setText("");
            imageViewPreview.setImageURI(null);
            imageViewPreview.setVisibility(View.GONE); // Hide the image again
            selectedImageUri = null;
            cameraImageUri = null;
        });

        return view;
    }

    private File createImageFile() {
        try {
            // Create an image file name
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
            String imageFileName = "FUNKO_" + timeStamp;
            File storageDir = new File(requireContext().getFilesDir(), "funko_images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String saveImageToInternalStorage(Uri imageUri, String fileName) {
        try {
            // Create a directory for funko images
            File directory = new File(requireContext().getFilesDir(), "funko_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Check for duplicates and create a unique filename
            File imageFile = new File(directory, fileName + ".jpg");
            int counter = 1;
            while (imageFile.exists()) {
                imageFile = new File(directory, fileName + counter + ".jpg");
                counter++;
            }

            // Copy the image from URI to internal storage
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
