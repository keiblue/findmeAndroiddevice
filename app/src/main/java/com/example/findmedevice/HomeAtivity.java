package com.example.findmedevice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findmedevice.connection.ConnectionsBackend;
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

public class HomeAtivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String ALL_BEACONS_REGION = "AllBeaconsRegion";
    private static final long DEFAULT_SCAN_PERIOD_MS = 20000;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String STRING_PREFERENSES = "findeMeDevice";
    private static final String PREFERENCE_ESTADO_SESION = "estado.beacon";
    private boolean estadoBeacon = false;
    private String uid = null;
    TextView uuid, saludo;
    DataExport data = new DataExport();
    private BeaconManager mBeaconManager;
    LocationManager locationManager;
    Location location;
    private Region mRegion;
    protected final String TAG = HomeAtivity.this.getClass().getSimpleName();
    ConnectionsBackend conn = new ConnectionsBackend();
    Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location == null){
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        uuid = findViewById(R.id.tvBeacon);
        saludo = findViewById(R.id.tvSaludo);
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        // Fijar un protocolo beacon, Eddystone en este caso
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        ArrayList<Identifier> identifiers = new ArrayList<>();
        mRegion = new Region(ALL_BEACONS_REGION, identifiers);
        prepareDetection();
        person = ConstantSQLite.ConsultarDatosPerson(getApplicationContext());
        saludo.setText("Hola "+person.getName());
        guardarEstadoButton();
    }

    private void prepareDetection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Si los permisos de localización todavía no se han concedido, solicitarlos
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                askForLocationPermissions();
            } else { // Permisos de localización concedidos
                if (!isLocationEnabled()) {
                    askToTurnOnLocation();
                } else { // Localización activada, comprobemos el bluetooth
                    comprobarBluetooth();
                }
            }
        } else { // Versiones de Android < 6
            if (!isLocationEnabled()) {
                askToTurnOnLocation();
            } else { // Localización activada, comprobemos el bluetooth
                comprobarBluetooth();
            }
        }
    }

    protected void  comprobarBluetooth (){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            showToastMessage("Nulo");
        } else if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.getName();
            startDetectingBeacons();
        } else {
            // Pedir al usuario que active el bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    private void startDetectingBeacons() {
        // Fijar un periodo de escaneo
        mBeaconManager.setForegroundScanPeriod(DEFAULT_SCAN_PERIOD_MS);

        // Enlazar al servicio de beacons. Obtiene un callback cuando esté listo para ser usado
        mBeaconManager.bind(this);
    }

    private void askForLocationPermissions() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Necesita acceso de locación");
        builder.setMessage("Permitir acceso");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        });
        builder.show();
    }

    private boolean isLocationEnabled() {

        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        boolean networkLocationEnabled = false;

        boolean gpsLocationEnabled = false;

        try {
            networkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            gpsLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch (Exception ex) {
            Log.d(TAG, "Excepción al obtener información de localización");
        }

        return networkLocationEnabled || gpsLocationEnabled;
    }

    private void askToTurnOnLocation() {

        // Notificar al usuario
        android.app.AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
        dialog.setMessage("location_disabled");
        dialog.setPositiveButton("location_settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.show();
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            // Empezar a buscar los beacons que encajen con el el objeto Región pasado, incluyendo
            // actualizaciones en la distancia estimada
            mBeaconManager.startRangingBeaconsInRegion(mRegion);
            showToastMessage("Empezando a buscar");

        } catch (RemoteException e) {
            Log.d(TAG, "Se ha producido una excepción al empezar a buscar beacons " + e.getMessage());
        }

        mBeaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if (beacons.size() == 0) {
            uuid.setText("Beacon no encontrado");
            //conexion para mandar info que no hay beacon
        }

        for (Beacon beacon : beacons) {
            uid = (String.valueOf(beacon.getId1()));
            uuid.setText("Beacon encontrado");
        }
        try{
            data.setLatitude(String.valueOf(location.getLatitude()));
            data.setLongitude(String.valueOf(location.getLongitude()));
            data.setBeaconUID(uid);
            conn.createUserLocation("userdevice/"+person.getId()+"/location",data);
            if(data.getBeaconUID() != null){
                conn.updateSmartphone("userdevice/"+person.getId()+"/UpdateBeacon", data, getApplicationContext());//update
                cambiarEstadoButton(this, true);
            }else if(obtenerEstadoButton(this) && data.getBeaconUID() == null){
                data.setBeaconUID("null");
                conn.updateSmartphone("userdevice/"+person.getId()+"/UpdateBeacon", data, getApplicationContext());//update
            }
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
        uid = null;
    }

    // Mostrar mensaje
    private void showToastMessage (String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void cambiarEstadoButton(Context c, boolean b){
        SharedPreferences preferences = c.getSharedPreferences(STRING_PREFERENSES, MODE_PRIVATE);
        preferences.edit().putBoolean(PREFERENCE_ESTADO_SESION, b).apply();
    }

    public void guardarEstadoButton(){
        SharedPreferences preferences = getSharedPreferences(STRING_PREFERENSES, MODE_PRIVATE);
        preferences.edit().putBoolean(PREFERENCE_ESTADO_SESION, estadoBeacon).apply();
    }

    public static boolean obtenerEstadoButton(Context c){
        SharedPreferences preferences = c.getSharedPreferences(STRING_PREFERENSES, MODE_PRIVATE);
        return preferences.getBoolean(PREFERENCE_ESTADO_SESION, false);
    }
}