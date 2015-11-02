package android.ua.pp.msk.locationtry;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener , LocationListener{

    private Boolean authenticated;
    protected GoogleApiClient googleApiClient;
    protected LocationRequest locationRequest;
    protected Location lastLocation;

    protected String latitudeLabel;
    protected String longitudeLabel;
    protected String accuracyLabel;
    protected String updateTimeLabel;

    protected Button startUpdatesButton;
    protected Button stopUpdatesButton;

    protected TextView latitudeText;
    protected TextView longitudeText;
    protected TextView accuracyText;
    protected TextView updateTimeText;

    protected Boolean requestingLocationUpdates;
    protected String lastUpdateTime;

    //TODO change this assignment when getting server side information
    private static final int DEVICE_ID = 0;

    private LocationDbHelper locationDbHelper;
    private SQLiteDatabase db;


    public static final String AUTHENTICATED = "android.ua.pp.msk.locationtry.MainActivity.AUTHENTICATED";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS/2;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";


    public MainActivity() {
        synchronized (this) {
            authenticated = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!authenticated) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(AUTHENTICATED, authenticated);
            startActivity(intent);
            Log.d(this.getClass().getName(), "Authenticated " + authenticated);
        }
        setContentView(R.layout.activity_main);
        latitudeLabel = getResources().getString(R.string.latitude_label);
        longitudeLabel = getResources().getString(R.string.longitude_label);
        accuracyLabel = getResources().getString(R.string.accuracy_label);
        updateTimeLabel = getResources().getString(R.string.update_time_label);

        startUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        stopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        latitudeText = (TextView) findViewById(R.id.latitude_text);
        longitudeText = (TextView) findViewById(R.id.longitude_text);
        accuracyText = (TextView) findViewById(R.id.accuracy_text);
        updateTimeText = (TextView) findViewById(R.id.update_time_text);

        requestingLocationUpdates = false;
        lastUpdateTime = "";

        updateValueFromBundle(savedInstanceState);

        buildGoogleApiClient();
        locationDbHelper = new LocationDbHelper(getBaseContext());
        db = locationDbHelper.getWritableDatabase();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (googleApiClient.isConnected() && requestingLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (googleApiClient.isConnected()){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle conHint) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            latitudeText.setText(String.format("%s : %f", latitudeLabel, lastLocation.getLatitude()));
            longitudeText.setText(String.format("%s : %f", longitudeLabel, lastLocation.getLongitude()));
            accuracyText.setText(String.format("%s : %f", accuracyLabel, lastLocation.getAccuracy()));
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(this.getClass().getName(), "Connection suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.i(this.getClass().getName(), "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(this.getClass().getName(), String.format("Location changed: lat %f lon %f acc %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
        lastLocation = location;
        lastUpdateTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        updateUI();

        Toast.makeText(this, getResources().getString(R.string.location_updated_message), Toast.LENGTH_SHORT).show();
        updateDb();
    }

    private void updateValueFromBundle(Bundle savedInstanceState){
        Log.i(this.getClass().getName(), "Updating values from bundle");
        if (savedInstanceState != null){
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)){
                requestingLocationUpdates  = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)){
                lastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)){
                lastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
            updateDb();
        }
    }

    private void setButtonsEnabledState() {
        if (requestingLocationUpdates){
            startUpdatesButton.setEnabled(false);
            stopUpdatesButton.setEnabled(true);
        } else {
            startUpdatesButton.setEnabled(true);
            stopUpdatesButton.setEnabled(false);
        }
    }

    private void updateUI(){
        latitudeText.setText(String.format("%s: %f", latitudeLabel, lastLocation.getLatitude()));
        longitudeText.setText(String.format("%s : %f", longitudeLabel, lastLocation.getLongitude()));
        accuracyText.setText(String.format("%s : %f", accuracyLabel, lastLocation.getAccuracy()));
        updateTimeText.setText(String.format("%s: %s", updateTimeLabel, lastUpdateTime));
    }

    private void updateDb(){

        ContentValues values = new ContentValues();
        values.put(LocationDbHelper.LocationEntry.COLUMN_NAME_LATITUDE, lastLocation.getLatitude());
        values.put(LocationDbHelper.LocationEntry.COLUMN_NAME_LONGITUDE, lastLocation.getLongitude());
        values.put(LocationDbHelper.LocationEntry.COLUMN_NAME_ALTITUDE, lastLocation.getAltitude());
        values.put(LocationDbHelper.LocationEntry.COLUMN_NAME_ACCURACY, lastLocation.getAccuracy());
        values.put(LocationDbHelper.LocationEntry.COLUMN_NAME_DEV_ID, DEVICE_ID);
        values.put(LocationDbHelper.LocationEntry.COLUMN_NAME_TIMESTAMP, lastUpdateTime);

        long newRowId;
        newRowId  = db.insert(LocationDbHelper.LocationEntry.TABLE_NAME, LocationDbHelper.LocationEntry.COLUMN_NAME_NULLABLE, values);
        Log.d(this.getClass().getName(), "Location has been stored to the db " + values.toString());
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    protected void startLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    public void startUpdatesButtonHandler(View view){
        if (!requestingLocationUpdates){
            requestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }
    public void stopUpdatesButtonHandler(View view){
        if (requestingLocationUpdates){
            requestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    public void createLocationRequest(){
        locationRequest  = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        saveInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        saveInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, lastUpdateTime);
        saveInstanceState.putParcelable(LOCATION_KEY, lastLocation);
        //Parseable
    }
}
