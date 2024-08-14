package com.probox3d.stajprojesi;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    private Recorder recorder;
    private Recording recording;

    private ImageButton button_takePicture, button_pointCloud, button_showPictures;
    private SwitchCompat switchCompat;


    private CameraMode cameraMode = CameraMode.PHOTO; // Varsayılan olarak fotoğraf modu

    // Firebase Storage referansı
    private ProgressDialog progressDialog;
    private Uri imageUri;
    private StorageReference storageReference;

    private Handler handler;
    private Runnable capturePhotoRunnable;
    private boolean isRecording = false;
    private int captureInterval = 1000; // 1 saniyede bir fotoğraf çek


    private ThumbnailAdapter thumbnailAdapter;
    private List<Uri> thumbnailUris;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Firebase Storage referansını başlat
        storageReference = FirebaseStorage.getInstance().getReference();
        button_takePicture = findViewById(R.id.button_takePicture);
        button_pointCloud = findViewById(R.id.button_pointCloud);
        button_showPictures = findViewById(R.id.button_showPictures);
        switchCompat = findViewById(R.id.switch1);
        previewView = findViewById(R.id.pvPreview);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        thumbnailUris = new ArrayList<>();
        thumbnailAdapter = new ThumbnailAdapter(this, thumbnailUris);
        recyclerView.setAdapter(thumbnailAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        setupCamera();





        // Switch için değişiklik dinleyicisi
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cameraMode = CameraMode.VIDEO; // Video moduna geç
                button_takePicture.setImageResource(R.drawable.round_fiber_manual_record_24);

            } else {
                cameraMode = CameraMode.PHOTO; // Fotoğraf moduna geç
                button_takePicture.setImageResource(R.drawable.baseline_camera_48);
            }
        });

        // Fotoğraf çekme veya video kaydı yapma işlemi
        button_takePicture.setOnClickListener(view -> {

            if (cameraMode == CameraMode.PHOTO) {
                capturePhoto();
            } else if (cameraMode == CameraMode.VIDEO) {
                startRecordingAndCapturingPhotos();
            }
        });


        //Çekilen Fotoğrafların yerinin gösterilmesi
        button_showPictures.setOnClickListener(view -> {
            Toast.makeText(CameraActivity.this, "Çekilen Resimlerin konumunu gösterecek", Toast.LENGTH_SHORT).show();
        });

        //Point Cloud Gösterilmesi
        button_pointCloud.setOnClickListener(view -> {
            Toast.makeText(CameraActivity.this, "Point CLoud Gösterilecek", Toast.LENGTH_SHORT).show();
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
                Uri imageUri = outputFileResults.getSavedUri();
                Toast.makeText(CameraActivity.this, "Image Captured: " + imageUri, Toast.LENGTH_SHORT).show();

                // RecyclerView'a fotoğrafı ekleyin
                thumbnailAdapter.addThumbnail(imageUri);

                //uploadImage(imageUri); // Eğer resmi Firebase'e yükleyecekseniz
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void recordVideo() {
        // Ses kaydı iznini kontrol edin
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        // Eğer zaten kayıt yapılıyorsa durdur
        if (recording != null) {
            stopRecordingAndCapturingPhotos();
            return;
        }

        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        recording = videoCapture.getOutput().prepareRecording(CameraActivity.this, options)
                .withAudioEnabled()  // Ses kaydını etkinleştirir
                .start(ContextCompat.getMainExecutor(CameraActivity.this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        // Video kaydı başlıyor
                        button_takePicture.setEnabled(true);

                        // Buton görünümünü güncelle
                        button_takePicture.setImageResource(R.drawable.baseline_stop_circle_24);
                        Toast.makeText(this, "Video kaydı başladı", Toast.LENGTH_SHORT).show();

                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                            String msg = "Video kaydı başarıyla tamamlandı: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            stopRecordingAndCapturingPhotos();
                        } else {
                            String msg = "Hata: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                        // Buton görünümünü tekrar değiştir
                        button_takePicture.setImageResource(R.drawable.round_fiber_manual_record_24);
                    }
                });
    }

    /*
    private void uploadImage(Uri imageUri) {
        if (this.imageUri == null) return;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Dosya yükleniyor...");
        progressDialog.show();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CANADA);
        Date now = new Date();
        String fileName = formatter.format(now);
        StorageReference fileRef = storageReference.child("images/" + fileName + ".jpg");

        fileRef.putFile(this.imageUri)
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
    */

    private void startRecordingAndCapturingPhotos() {
        // Video kaydını başlat
        recordVideo();

        // Zamanlayıcıyı başlat
        handler = new Handler();
        capturePhotoRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    capturePhoto(); // Fotoğraf çek
                    handler.postDelayed(this, captureInterval); // Zamanlayıcıyı tekrar çalıştır
                }
            }
        };

        // Zamanlayıcıyı başlat
        isRecording = true;
        handler.post(capturePhotoRunnable);
    }

    private void stopRecordingAndCapturingPhotos() {
        if (recording != null) {
            recording.stop();  // Video kaydını durdur
            recording = null;
        }

        isRecording = false;  // Kayıt durumunu kapat
        if (handler != null && capturePhotoRunnable != null) {
            handler.removeCallbacks(capturePhotoRunnable);  // Zamanlayıcıyı durdur
        }

        // Buton görünümünü tekrar güncelle
        button_takePicture.setImageResource(R.drawable.round_fiber_manual_record_24);
    }

}
