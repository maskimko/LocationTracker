package android.ua.pp.msk.locationtry;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private Boolean authenticated;
    public static final String AUTHENTICATED = "android.ua.pp.msk.locationtry.MainActivity.AUTHENTICATED";
    public MainActivity(){
        synchronized (this) {
            authenticated = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!authenticated){
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(AUTHENTICATED, authenticated);
            startActivity(intent);
            Log.d(this.getClass().getName(), "Authenticated " + authenticated.booleanValue());
        }
        setContentView(R.layout.activity_main);
    }
}
