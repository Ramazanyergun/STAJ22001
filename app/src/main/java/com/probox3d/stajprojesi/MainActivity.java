package com.probox3d.stajprojesi;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonCreateModel;
    private LinearLayout modelContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        modelContainer = findViewById(R.id.modelContainer); // LinearLayout'un id'sini kontrol edin

        // assets/models klasöründeki dosyaları al
        AssetManager assetManager = getAssets();
        try {
            String[] modelFiles = assetManager.list("models"); // assets/models dizinini okur

            if (modelFiles != null) {
                for (String modelFile : modelFiles) {
                    // Yeni bir ImageButton oluştur
                    ImageButton imageButton = new ImageButton(this);

                    // ConstraintLayout parametrelerini ayarlayın
                    ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.MATCH_PARENT, // genişlik MATCH_PARENT
                            500 // Yükseklik 500dp
                    );

                    // Her ImageButton'a 20dp alt margin ekleyin
                    int bottomMarginInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                    layoutParams.setMargins(0, 0, 0, bottomMarginInDp); // Sol, üst, sağ, alt margin

                    // Her ImageButton'a 20dp üst padding ekleyin
                    int topPaddingInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                    imageButton.setPadding(0, topPaddingInDp, 0, 0); // Sol, üst, sağ, alt padding

                    // Bu parametreleri imageButton'a ekleyin
                    imageButton.setLayoutParams(layoutParams);

                    // Görsel kaynağını ayarlayın (her model için placeholder olabilir)
                    imageButton.setImageResource(R.drawable.model_placeholder);
                    imageButton.setScaleType(ImageButton.ScaleType.CENTER_CROP); // Görüntüleme ölçeklendirme

                    // ImageButton'a tıklama işlevi ekleyin
                    imageButton.setOnClickListener(v -> {
                        showModelDetails(modelFile);
                    });

                    // ImageButton'ı layout'a ekleyin
                    modelContainer.addView(imageButton);

                    // Log ile dosya ismini göster
                    Log.d("ModelFiles", modelFile);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Sistem çubukları için padding ayarlama
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.modelContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI elemanlarının tanımlanması
        buttonCreateModel = findViewById(R.id.button_createModel);

        // Butona tıklama işlemi
        buttonCreateModel.setOnClickListener(v -> openCamera());
    }

    private void openCamera() {
        // CameraActivity'ye geçiş
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }

    private void showModelDetails(String modelFile) {

        Intent intent = new Intent(MainActivity.this, ModelDetailsActivity.class);
        intent.putExtra("model_file", "models/" + modelFile); // Model yolunu aktar
        startActivity(intent);
    }
}
