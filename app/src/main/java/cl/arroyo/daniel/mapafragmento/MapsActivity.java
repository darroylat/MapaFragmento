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

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cl.arroyo.daniel.mapafragmento.libreria.UserFunctions;

public class MapsActivity extends FragmentActivity implements LocationListener {
    private GoogleMap map;
    private static final LatLng ROMA = new LatLng(42.093230818037, 11.7971813678741);
    private LocationManager locationManager;
    private String provider;
    long idemotion;
    String emotion;
    UserFunctions userFunctions;

    // JSON Response node names
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_COMMENT = "comment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();

        StrictMode.setThreadPolicy(policy);

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

                //btnLocation.setText(getEmail());
                onResume();
                getLocation(location);


                //Inicio comentario, Crea comonente dialog
                final Dialog dialog = new Dialog(MapsActivity.this);
                //tell the Dialog to use the dialog.xml as it's layout description
                dialog.setContentView(R.layout.dialogcomment);
                dialog.setTitle("Que piensas de este lugar");

                final EditText txtComment = (EditText) dialog.findViewById(R.id.txtComment);


                final Spinner emotionSpinner = (Spinner) dialog.findViewById(R.id.emotionSpinner);
                List<String> listEmotion = new ArrayList<String>();

                ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(MapsActivity.this, R.array.emotion, android.R.layout.simple_spinner_item);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                emotionSpinner.setAdapter(dataAdapter);
                emotionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        emotion = adapterView.getItemAtPosition(i).toString();
                        idemotion = adapterView.getItemIdAtPosition(i);
                        Toast.makeText(adapterView.getContext(), "La emocion " + emotion + " id " + idemotion, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                //Boton cancelar del comentario
                Button btnCancelComment = (Button) dialog.findViewById(R.id.btnCancelComment);
                btnCancelComment.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //Boton enviar del comentario
                Button btnAcceptComment = (Button) dialog.findViewById(R.id.btnAcceptComment);
                btnAcceptComment.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String commnet = txtComment.getText().toString();
                        String email = getEmail();
                        String emotion = String.valueOf(idemotion);
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());

                        UserFunctions userFunction = new UserFunctions();
                        JSONObject json = userFunction.registerComment(commnet, email, emotion, latitude, longitude);
                        Log.e("JSON", commnet.toString());
                        Log.e("JSON", email.toString());
                        Log.e("JSON", emotion.toString());
                        Log.e("JSON", latitude.toString());
                        Log.e("JSON", longitude.toString());
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
            //Fin Comentario
        });


        final Button btnupdate = (Button) findViewById(R.id.btnupdate);
        btnupdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                getNewLocation();
            }
        });
    }


    public void getComments(String hora) {
        UserFunctions userFunction = new UserFunctions();
        JSONObject json = userFunction.getComments(hora);

        try {

            //recibe en formato JSON los comentarios
            JSONArray jArray = json.optJSONArray("comentarios");

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonProductObject = jArray.getJSONObject(i);
                String id = jsonProductObject.getString("commentid");
                String emotion = jsonProductObject.getString("commentemotion");
                String comment = jsonProductObject.getString("commentcomment");
                String latitude = jsonProductObject.getString("commentlatitude");
                String longitude = jsonProductObject.getString("commentlongitude");
                String created = jsonProductObject.getString("commentcreated");
                TextView prueba = (TextView) findViewById(R.id.txtProbando);
                prueba.setText(latitude);
                setLocationComment(Integer.parseInt(id),Integer.parseInt(emotion),created,comment,Double.valueOf(latitude).doubleValue(),Double.valueOf(longitude).doubleValue());
                Log.i("Json", String.valueOf(jsonProductObject));
            }


        }catch (Exception e){
            e.printStackTrace();
        }
   }


                
        //Función que obtiene el correo del usuario activo,
    public String getEmail(){
        UserFunctions userF = new UserFunctions();
        String email = userF.getUserLoggedIn(getApplicationContext());
        return email;
    }
    public void setLocationComment(Integer id, Integer emotion, String created, String comment, Double latitude, Double longitude){

        double lat =  latitude;
        double lng = longitude;
        //Toast.makeText(this, "Location " + lat+","+lng, Toast.LENGTH_LONG).show();
        LatLng coordinate = new LatLng(lat, lng);
        //Toast.makeText(this, "Location " + coordinate.latitude+","+coordinate.longitude, Toast.LENGTH_LONG).show();
        Marker startPerc;

        startPerc = map.addMarker(new MarkerOptions()
                .position(coordinate)
                .title(comment + " " + emotion + " " + id)
                .snippet(created)
                .icon(BitmapDescriptorFactory.defaultMarker()));
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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location_found)));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
        map.animateCamera(cameraUpdate);
    }
    public void getNewLocation(){

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabledGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!enabledGPS) {
            Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        LocationListener gpsListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(this );
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                locationManager.removeUpdates(this );
            }

            @Override
            public void onProviderEnabled(String s) {
                locationManager.removeUpdates(this );
            }

            @Override
            public void onProviderDisabled(String s) {
                locationManager.removeUpdates(this );
            }
        };

        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsListener, null);

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

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
        locationManager.removeUpdates(this );
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_refresh:

                        getComments("-12");


                return true;
            case R.id.action_loguot:
                userFunctions = new UserFunctions();
                userFunctions.logoutUser(getApplicationContext());
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                // Closing dashboard screen
                finish();
            case R.id.action_location:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates(this);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}