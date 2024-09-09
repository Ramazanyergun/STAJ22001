package com.probox3d.stajprojesi;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class ModelDetailsActivity extends AppCompatActivity {

    private ImageView modelView;
    private TextView modelName;
    private ImageButton downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_details);

        // UI elemanlarını tanımla
        modelView = findViewById(R.id.modelView);

        modelName = findViewById(R.id.modelName);
        downloadButton = findViewById(R.id.downloadModel);

        // Intent ile gelen model bilgilerini al
        String modelFilePath = getIntent().getStringExtra("model_file");


        // Gelen bilgiyi UI'da göster (örnek olarak TextView'de gösteriliyor)
        if (modelFilePath != null) {
            modelName.setText(modelFilePath);
        }

        downloadButton.setOnClickListener(v -> donwloadModels(modelFilePath));
    }

    private void donwloadModels(String modelFilePath) {
        // Firebase Storage referansı
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child(modelFilePath);

        // İndirilecek dosyanın cihazda kaydedileceği yer
        File localFile = new File(getExternalFilesDir(null), "downloaded_model.obj");  // ".obj" dosya türü, model dosyasına göre değişebilir

        // Dosyayı indir
        storageReference.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            // Başarıyla indirildi
            Toast.makeText(ModelDetailsActivity.this, "Dosya indirildi: " + localFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            // İndirilen dosyayı kullan
            displayModel(localFile);

        }).addOnFailureListener(exception -> {
            // İndirme işlemi başarısız oldu
            Toast.makeText(ModelDetailsActivity.this, "İndirme başarısız: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void displayModel(File modelFile) {
        // ModelRenderable kullanarak modeli yükleme ve gösterme
        ModelRenderable.builder()
                .setSource(this, Uri.fromFile(modelFile)) // İndirilen dosyayı kaynak olarak belirle
                .build()
                .thenAccept(renderable -> {
                    // Modeli Sceneform sahnesinde gösterin
                    ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
                    if (arFragment != null) {
                        // AR sahnesine modeli ekle
                        AnchorNode anchorNode = new AnchorNode();
                        anchorNode.setRenderable(renderable);
                        arFragment.getArSceneView().getScene().addChild(anchorNode);
                    }
                })
                .exceptionally(throwable -> {
                    // Model yüklenirken bir hata olursa bunu ele alın
                    Toast.makeText(this, "Model yüklenemedi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

}
