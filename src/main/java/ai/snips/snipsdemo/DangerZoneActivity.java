package ai.snips.snipsdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DangerZoneActivity extends AppCompatActivity {
    // String[] values = new String[]{"Steil abfallendes Gelände", "Brücke", "Steil abfallender Hang"};

    Button addButton;
    EditText inputText;
    TextView gpsText;
    //ArrayList<String> list;
    ArrayList<DangerZoneObject> objList;
    ArrayAdapter<String> adapter;
    //CustomAdapter<DangerZoneObject> adapter;
    ArrayList<String> resultStringList;
    //DangerZoneObject test;
    Double myLong;
    Double myLat;
    String dummyDistance = "12";
   // Location what;
    private LocationManager locationManager;
    private LocationListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.danger_zones);

        addButton = findViewById(R.id.button);
        gpsText = findViewById(R.id.gpsdata);


        objList = new ArrayList<DangerZoneObject>();

        DangerZoneObject test = (DangerZoneObject) getIntent().getSerializableExtra("serialize_data");
        if (test == null) {
            Log.d("NEXT: ", "No object created yet.");
        } else {
            Log.d("NEXT: ", "OBJL: " + test.getName());
            objList.add(test);
        }

        objList.add(new DangerZoneObject("Berg", -122.0840, 37.4220, "12"));

        resultStringList = new ArrayList<String>();

        for (DangerZoneObject dz : objList) {
            String name = dz.getName().toString();
            String longi = dz.getLongi().toString();
            String lati = dz.getLati().toString();
            String dist = dummyDistance;
            resultStringList.add(name + "                           h     " + dist + " \n" + longi + " \n " + lati + " ");
        }

        ListView listView = findViewById(R.id.listview);

        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, resultStringList);

        listView.setAdapter(adapter);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        myLong = locationManager.getLastKnownLocation("gps").getLongitude();
        myLat = locationManager.getLastKnownLocation("gps").getLatitude();

        Log.d("LM: ", "mylong " + myLong);
        Log.d("LM: ", "mylat " + myLat);
        gpsText.append(myLong.toString());
        gpsText.append(myLat.toString());



        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //myLong = location.getLongitude();
                //myLat = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.getLastKnownLocation("gps");
    }

    public void directToAddNewDangerZone(View view){
        Intent intent = new Intent(DangerZoneActivity.this, AddDangerZoneActivity.class);
        startActivity(intent);
    }
}