package com.example.findmedevice;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.example.findmedevice.connection.ConnectionsBackend;
import com.example.findmedevice.models.DataExport;
import com.example.findmedevice.models.Person;
import com.example.findmedevice.utils.ConstantSQLite;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String STRING_PREFERENSES = "findme.device";
    private static final String PREFERENCE_ESTADO_SESION = "estado.sesion";
    private static final int REQUEST_CODE_QR_SCAN = 101;
    private RadioButton rbSesion;
    private boolean isActivatedRadioButton;
    DataExport dates = new DataExport();
    String androidId = null;
    ConnectionsBackend conn = new ConnectionsBackend();
    private Button btnEntrar;
    TextView QR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("IDANDROID",androidId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyPermission();
        if(obtenerEstadoButton(this)){
            Person person = ConstantSQLite.ConsultarDatosPerson(getApplicationContext());
            try {
                if(person.getId() != null) {
                    Intent intent = new Intent(MainActivity.this, HomeAtivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    ConstantSQLite.BorrarDB(getApplicationContext());
                    MainActivity.cambiarEstadoButton(MainActivity.this, false);
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        btnEntrar = findViewById(R.id.buttonEntrar);
        QR = findViewById(R.id.textViewQr);
        rbSesion = findViewById(R.id.radioButtonSesion);
        isActivatedRadioButton = rbSesion.isChecked();
        rbSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isActivatedRadioButton){
                    rbSesion.setChecked(false);
                }
                isActivatedRadioButton = rbSesion.isChecked();
            }
        });
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isActivatedRadioButton){
                    guardarEstadoButton();
                    Intent intent = new Intent(MainActivity.this, HomeAtivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(getApplicationContext(), "active sesión para ingresar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyPermission() {
        int permsRequestCode = 100;
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA};
        int accessFinePermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int accessCoarsePermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);

        if (cameraPermission == PackageManager.PERMISSION_GRANTED && accessFinePermission == PackageManager.PERMISSION_GRANTED && accessCoarsePermission == PackageManager.PERMISSION_GRANTED) {
            //se realiza metodo si es necesario...
        } else {
            requestPermissions(perms, permsRequestCode);
        }
    }

    public void onClick(View v) {
        Intent i = new Intent(MainActivity.this, QrCodeActivity.class);
        startActivityForResult(i, REQUEST_CODE_QR_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "No se pudo obtener una respuesta", Toast.LENGTH_SHORT).show();
                String resultado = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
                if (resultado != null) {
                    Toast.makeText(getApplicationContext(), "No se pudo escanear el código QR", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (requestCode == REQUEST_CODE_QR_SCAN) {
                if (data != null) {
                    String lectura = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                    String [] contenido = lectura.split(",");
                    Person person = new Person();
                    person.setId(contenido[0]);
                    person.setName(contenido[1]);
                    person.setLastName(contenido[2]);
                    dates.setAndroidId(androidId);
                    if(ConstantSQLite.ConsultarDatosPerson(this).getId() == null){
                        ConstantSQLite.RegisterPersonSQL(person, this);
                    }
                    QR.setText("Hola "+person.getName());
                    conn.createSmartphone("userdevice/"+person.getId()+"/createsmartphone", dates, getApplicationContext());
                }
            }
        }catch (Exception ex){
             ex.getMessage();
        }
    }

    public static void cambiarEstadoButton(Context c, boolean b){
        SharedPreferences preferences = c.getSharedPreferences(STRING_PREFERENSES, MODE_PRIVATE);
        preferences.edit().putBoolean(PREFERENCE_ESTADO_SESION, b).apply();
    }

    public void guardarEstadoButton(){
        SharedPreferences preferences = getSharedPreferences(STRING_PREFERENSES, MODE_PRIVATE);
        preferences.edit().putBoolean(PREFERENCE_ESTADO_SESION, rbSesion.isChecked()).apply();
    }

    public static boolean obtenerEstadoButton(Context c){
        SharedPreferences preferences = c.getSharedPreferences(STRING_PREFERENSES, MODE_PRIVATE);
        return preferences.getBoolean(PREFERENCE_ESTADO_SESION, false);
    }
}