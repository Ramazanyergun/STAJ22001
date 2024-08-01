package com.probox3d.stajprojesi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonCreateModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sistem çubukları için padding ayarlama
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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
}
