package android.ua.pp.msk.locationtry;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Boolean authenticated;
    protected GoogleApiClient googleApiClient;
    protected Location lastLocation;
    protected String latitudeLabel;
    protected String longitudeLabel;
    protected String accuracyLabel;
    protected TextView latitudeText;
    protected TextView longitudeText;
    protected TextView accuracyText;

    public static final String AUTHENTICATED = "android.ua.pp.msk.locationtry.MainActivity.AUTHENTICATED";

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
        latitudeText = (TextView) findViewById(R.id.latitude_text);
        longitudeText = (TextView) findViewById(R.id.longitude_text);
        accuracyText = (TextView) findViewById(R.id.accuracy_text);

        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
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
}
