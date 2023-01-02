package com.jpoole.service_novigrad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class PermissionManager extends AppCompatActivity {

    public static final int PERMISSION_REQUEST_FILESYSTEM_READ = 12345;
    public static final int PERMISSION_REQUEST_FILESYSTEM_WRITE = 12346;
    public static final int PERMISSION_REQUEST_CAMERA = 12347;

    Button continueButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_manager);

        continueButton = findViewById(R.id.continueButton);


        continueButton.setOnClickListener( (v) -> {

            if(!PermissionManager.checkPermissions(this)){
                //Prompt for permissions
                String permissionRead = android.Manifest.permission.READ_EXTERNAL_STORAGE;
                String permissionWrite = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
                String permissionCamera = android.Manifest.permission.CAMERA;

                if (ActivityCompat.checkSelfPermission(this, permissionRead) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permissionRead}, PERMISSION_REQUEST_FILESYSTEM_READ);
                }
                else if (ActivityCompat.checkSelfPermission(this, permissionWrite) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permissionWrite}, PERMISSION_REQUEST_FILESYSTEM_WRITE);
                }
                else if (ActivityCompat.checkSelfPermission(this, permissionCamera) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permissionCamera}, PERMISSION_REQUEST_CAMERA);
                }
            }else{
                //all permissions gained
                finish();
            }
        });
    }


    public static boolean checkPermissions(Activity activity){
        boolean storageRead = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean storageWrite = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean camera = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return storageRead && storageWrite && camera;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //If grantResults is null then permission request was cancelled
        if (requestCode == PERMISSION_REQUEST_FILESYSTEM_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Filesystem read permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Filesystem read permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == PERMISSION_REQUEST_FILESYSTEM_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Filesystem write permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Filesystem write permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}