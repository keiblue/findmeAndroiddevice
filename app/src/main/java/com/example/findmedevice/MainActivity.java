package com.example.findmedevice;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, BeaconConsumer, RangeNotifier {

    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int TIEMPO = 1000;

    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    TextView longitudeValueBest, latitudeValueBest;
    TextView longitudeValueGPS, latitudeValueGPS;
    TextView contadorProceso, latitudeValueNetwork;
    Location location;
    int contador;

    protected final String TAG = MainActivity.this.getClass().getSimpleName();;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final long DEFAULT_SCAN_PERIOD_MS = 5000;
    private static final String ALL_BEACONS_REGION = "AllBeaconsRegion";
    private EditText idTxt;

    // Para interactuar con los beacons desde una actividad
    private BeaconManager mBeaconManager;

    // Representa el criterio de campos con los que buscar beacons
    private Region mRegion;

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

        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        // Fijar un protocolo beacon, Eddystone en este caso
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        ArrayList<Identifier> identifiers = new ArrayList<>();

        mRegion = new Region(ALL_BEACONS_REGION, identifiers);

        idTxt = (EditText) findViewById(R.id.editTextBeacon);

        prepareDetection();
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


    private void prepareDetection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Si los permisos de localización todavía no se han concedido, solicitarlos
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                askForLocationPermissions();
            } else { // Permisos de localización concedidos
                if (!isLocationEnabled()) {
                    askToTurnOnLocation();
                } else { // Localización activada, comprobemos el bluetooth
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
            }
        } else { // Versiones de Android < 6
            if (!isLocationEnabled()) {
                askToTurnOnLocation();
            } else { // Localización activada, comprobemos el bluetooth
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            // Usuario ha activado el bluetooth
            if (resultCode == RESULT_OK) {
                startDetectingBeacons();
            } else if (resultCode == RESULT_CANCELED) { // User refuses to enable bluetooth
                showToastMessage("No hay mensaje de bluethooth");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Empezar a detectar los beacons, ocultando o mostrando los botones correspondientes
     */
    private void startDetectingBeacons() {
        // Fijar un periodo de escaneo
        mBeaconManager.setForegroundScanPeriod(DEFAULT_SCAN_PERIOD_MS);

        // Enlazar al servicio de beacons. Obtiene un callback cuando esté listo para ser usado
        mBeaconManager.bind(this);
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


    /**
     * Método llamado cada DEFAULT_SCAN_PERIOD_MS segundos con los beacons detectados durante ese
     * periodo
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        if (beacons.size() == 0) {
            showToastMessage("No se detectó beacon");
        }

        for (Beacon beacon : beacons) {
            idTxt.setText(String.valueOf(beacon.getId1()));
            showToastMessage("encontrado");
            stopDetectingBeacons();
        }
    }

    private void stopDetectingBeacons() {
        try {
            mBeaconManager.stopMonitoringBeaconsInRegion(mRegion);
            showToastMessage("Deteniendo");
        } catch (RemoteException e) {
            Log.d(TAG, "Se ha producido una excepción al parar de buscar beacons " + e.getMessage());
        }

        mBeaconManager.removeAllRangeNotifiers();

        // Desenlazar servicio de beacons
        mBeaconManager.unbind(this);
    }

    /**
     * Comprobar permisión de localización para Android >= M
     */
    private void askForLocationPermissions() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Necesita acceso de locación");
        builder.setMessage("Permitir acceso");
        builder.setPositiveButton(android.R.string.ok, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
        }
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareDetection();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("funcionality_limited");
                    builder.setMessage("location_not_granted, cannot_discover_beacons");
                    builder.setPositiveButton(android.R.string.ok, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {

                            }
                        });
                    }
                    builder.show();
                }
                return;
            }
        }
    }

    /**
     * Comprobar si la localización está activada
     *
     * @return true si la localización esta activada, false en caso contrario
     */
    private boolean isLocationEnabled() {

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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

    /**
     * Abrir ajustes de localización para que el usuario pueda activar los servicios de localización
     */
    private void askToTurnOnLocation() {

        // Notificar al usuario
        android.app.AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("location_disabled");
        dialog.setPositiveButton("location_settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });
        dialog.show();
    }

    /**
     * Mostrar mensaje
     *
     * @param message mensaje a enseñar
     */
    private void showToastMessage (String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.unbind(this);
    }
}