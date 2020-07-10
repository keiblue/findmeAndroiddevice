package com.example.findmedevice;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.example.findmedevice.connection.Connections;
import com.example.findmedevice.models.DataExport;
import com.example.findmedevice.models.Person;
import com.example.findmedevice.utils.ConstantSQLite;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;



import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CODE_QR_SCAN = 101;
    private final int TIEMPO = 5000;
    LocationManager locationManager;
    TextView longitudeValueGPS, latitudeValueGPS, QR;
    TextView contadorProceso;
    Location location;
    int contador;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyPermission();
        btnEntrar = findViewById(R.id.buttonEntrar);
        QR = findViewById(R.id.textViewQr);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeValueGPS = findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = findViewById(R.id.latitudeValueGPS);
        contadorProceso = findViewById(R.id.cantidadLlamadas);
        contador =1;
        Handler handler = new Handler();
        actualizarpos();
        contadorProceso.setText("1");
        ejecutarTarea(handler);

        Log.d("DATA LATITUD", latitudeValueGPS.getText().toString());
        Log.d("DATA LONGITUD", longitudeValueGPS.getText().toString());
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeAtivity.class);
                startActivity(intent);
            }
        });
    }

    public void ejecutarTarea(final Handler handler) {

        handler.postDelayed(new Runnable() {

            public void run() {

                actualizarpos(); // función para refrescar la ubicación del conductor, creada en otra línea de código
                actualizaContador();
                handler.postDelayed(this, TIEMPO);
            }

        }, TIEMPO);

    }

    public void actualizarpos() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location == null){
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            latitudeValueGPS.setText(String.valueOf(location.getLatitude()));
            longitudeValueGPS.setText(String.valueOf(location.getLongitude()));
        }
        //TODO: actualizar registro en DB , tabla ubicacion con IdTelefono

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

    public void actualizaContador(){
        contador+=1;
        contadorProceso.setText(String.valueOf(contador));
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
                    ConstantSQLite.RegisterPersonSQL(person, this);
                    QR.setText(person.getName());
                }
            }
        }catch (Exception ex){
             ex.getMessage();
        }
    }
}