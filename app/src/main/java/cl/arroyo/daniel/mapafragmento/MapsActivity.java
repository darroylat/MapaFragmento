package cl.arroyo.daniel.mapafragmento;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import cl.arroyo.daniel.mapafragmento.libreria.DatabaseHandler;
import cl.arroyo.daniel.mapafragmento.libreria.UserFunctions;

public class MapsActivity extends FragmentActivity implements LocationListener{
    private GoogleMap map;
    private static final LatLng ROMA = new LatLng(42.093230818037,11.7971813678741);
    private LocationManager locationManager;
    private String provider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabledGPS = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = service
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabledGPS) {
            Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        final Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            Toast.makeText(this, "Selected Provider " + provider,
                    Toast.LENGTH_SHORT).show();
            //onLocationChanged(location);
            getLocation(location);
        } else {

            //do something
        }
        final Button btnLocation = (Button) findViewById(R.id.btnLocation);

        btnLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLocation.setText(getEmail());
                onResume();

                // create a Dialog component
                final Dialog dialog = new Dialog(MapsActivity.this);
                //tell the Dialog to use the dialog.xml as it's layout description
                dialog.setContentView(R.layout.dialogcomment);
               // dialog.setTitle("Android Custom Dialog Box");
                TextView txt = (TextView) dialog.findViewById(R.id.txt);
                txt.setText("This is an Android custom Dialog Box Example! Enjoy!");
                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
        }
        });
        final Button btnupdate = (Button) findViewById(R.id.btnupdate);
        btnupdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                getLocation(location);
            }
        });
    }








    //Función que obtiene el correo del usuario activo,
    public String getEmail(){
        UserFunctions userF = new UserFunctions();
        String email = userF.getUserLoggedIn(getApplicationContext());
        return email;
    }

    public void getLocation(Location location) {

        double lat =  location.getLatitude();
        double lng = location.getLongitude();
        Toast.makeText(this, "Location " + lat+","+lng,
                Toast.LENGTH_LONG).show();
        LatLng coordinate = new LatLng(lat, lng);
        Toast.makeText(this, "Location " + coordinate.latitude+","+coordinate.longitude,
                Toast.LENGTH_LONG).show();
        Marker startPerc;

        startPerc = map.addMarker(new MarkerOptions()
                .position(coordinate)
                .title("Aqui estas")
                .snippet("Disfruta")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
        map.animateCamera(cameraUpdate);
    }
    public void getNewLocation(){
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

        locationManager.removeUpdates(this);
    }


    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

}