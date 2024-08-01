// CameraActivity.java
package com.probox3d.stajprojesi;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

enum CameraMode {
    PHOTO,
    VIDEO
}


public class CameraActivity extends AppCompatActivity {

    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private ImageButton button_takePicture;
    private SwitchCompat switchCompat;
    private TextView photoCount;

    private CameraMode cameraMode = CameraMode.PHOTO; // Varsayılan olarak fotoğraf modu

    // Firebase Storage referansı
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private StorageReference storageReference;
    private Recorder recorder;
    private Recording recording;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Firebase Storage referansını başlat
        storageReference = FirebaseStorage.getInstance().getReference();
        button_takePicture = findViewById(R.id.button_takePicture);
        switchCompat = findViewById(R.id.switch1);
        photoCount = findViewById(R.id.photoCount);
        previewView = findViewById(R.id.pvPreview);


        // Switch için değişiklik dinleyicisi
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cameraMode = CameraMode.VIDEO; // Video moduna geç
                photoCount.setText("Video");
                button_takePicture.setImageResource(R.drawable.round_fiber_manual_record_24);

            } else {
                cameraMode = CameraMode.PHOTO; // Fotoğraf moduna geç
                photoCount.setText("Foto");
                button_takePicture.setImageResource(R.drawable.baseline_camera_48);
            }
        });

        // Fotoğraf çekme veya video kaydı yapma işlemi
        button_takePicture.setOnClickListener(view -> {

            if (cameraMode == CameraMode.PHOTO) {
                capturePhoto();
            } else if (cameraMode == CameraMode.VIDEO) {
                recordVideo();
            }
        });

        setupCamera();
    }

    private void recordVideo() {

        button_takePicture.setImageResource(R.drawable.round_fiber_manual_record_24);
        Recording recording1 = recording;
        if (recording1 != null) {
            recording1.stop();
            recording = null;
            return;
        }
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        recording = videoCapture.getOutput().prepareRecording(CameraActivity.this, options).withAudioEnabled().start(ContextCompat.getMainExecutor(CameraActivity.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                button_takePicture.setEnabled(true);
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    String msg = "Video capture succeeded: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } else {
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                button_takePicture.setImageResource(R.drawable.baseline_stop_circle_24);
            }
        });
    }


    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderListenableFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        String name = System.currentTimeMillis() + "";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraXStable");
        }
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                imageUri = outputFileResults.getSavedUri();
                Toast.makeText(CameraActivity.this, "Image Captured: " + imageUri, Toast.LENGTH_SHORT).show();
                uploadImage();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }


    private void uploadImage() {
        if (imageUri == null) return;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Dosya yükleniyor...");
        progressDialog.show();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = formatter.format(now);
        StorageReference fileRef = storageReference.child("images/" + fileName + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(CameraActivity.this, "Başarıyla Yüklendi", Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        // İndirilen URL'yi kullanın
                    });
                }).addOnFailureListener(e -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(CameraActivity.this, "Yükleme Başarısız", Toast.LENGTH_SHORT).show();
                });
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build();
        videoCapture = VideoCapture.withOutput(recorder);

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
