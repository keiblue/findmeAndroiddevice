package com.example.findmedevice;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int TIEMPO = 5000;

    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView contadorProceso, latitudeValueNetwork;
    Location location;
    int contador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        longitudeValueGPS = (TextView) findViewById(R.id.longitudeValueGPS);
        latitudeValueGPS = (TextView) findViewById(R.id.latitudeValueGPS);
        contadorProceso = (TextView) findViewById(R.id.cantidadLlamadas);
        contador =1;
        Handler handler = new Handler();
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                actualizarpos();
                //TODO: generar primer registro en DB , tabla ubicacion con IdTelefono
                contadorProceso.setText("1");
                ejecutarTarea(handler);
            }
        } else {
            actualizarpos();
            contadorProceso.setText("1");
            ejecutarTarea(handler);
        }




        Log.d("DATA LATITUD", latitudeValueGPS.getText().toString());
        Log.d("DATA LONGITUD", longitudeValueGPS.getText().toString());


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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
             //                                         int[] grantResults);
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
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

    public void actualizaContador(){
        contador+=1;
        contadorProceso.setText(String.valueOf(contador));
    }




}
