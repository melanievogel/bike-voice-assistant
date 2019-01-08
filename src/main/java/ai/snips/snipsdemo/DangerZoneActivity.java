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
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.security.Key;
import java.util.ArrayList;

public class DangerZoneActivity extends AppCompatActivity {
    Button addButton;
    TextView gpsText;
    ArrayList<DangerZoneObject> objList;
    ArrayAdapter<String> adapter;
    ArrayList<String> resultStringList;
    Double myLong;
    Double myLat;
    Double distance;
    Long distance_round;
    Double myLong_round;
    Double myLat_round;
    private LocationManager locationManager;
    private LocationListener listener;
    static double PI_RAD = Math.PI / 180.0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.danger_zones);
        //onBackPressed();

       // getCallingActivity().onBackPressed();
        addButton = findViewById(R.id.button);
        gpsText = findViewById(R.id.gpsdata);

       // distance = greatCircleInKilometers(37.4220, -122.084, 98.5580, 125.001);
       // distance_round =  Math.round(distance);
        //Log.d("DISTANCE: ", "distance " + Math.round(distance));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        myLong = locationManager.getLastKnownLocation("gps").getLongitude();
        myLat = locationManager.getLastKnownLocation("gps").getLatitude();

        objList = new ArrayList<DangerZoneObject>();

        ArrayList<DangerZoneObject> test = (ArrayList<DangerZoneObject>) getIntent().getSerializableExtra("serialize_data");

        if (test == null) {
            objList.add(new DangerZoneObject("Berg", -122.0840, 37.4220, "12"));
            Log.d("NEXT: ", "No object created yet.");
        } else {
            //objList.add(new DangerZoneObject("Berg2", -122.0840, 38.4220, "12"));
            //Log.d("NEXT: ", "OBJL: " + test.getName());
            for(DangerZoneObject i : test){
                objList.add(i);

            }
        }

        resultStringList = new ArrayList<String>(5);

        for (DangerZoneObject dz : objList) {
            String name = dz.getName().toString();
           // String longi = dz.getLongi().toString();
          //  String lati = dz.getLati().toString();
            Double longi2 = dz.getLongi();
            Double lati2 = dz.getLati();
         //   String dist = distance.toString();
            Double dist2 = greatCircleInKilometers(lati2,longi2,myLat,myLong);
            Log.d("LATI: ", "lati2 " + lati2);
            resultStringList.add(name + "\n"  + "LG: " + longi2 + ", BG: " + lati2 + " " + "\n" + "Dist: " + Math.round(dist2) + " km");
        }

        ListView listView = findViewById(R.id.listview);
        //TODO: Adapter zu spät aufgerufen?
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, resultStringList);

        listView.setAdapter(adapter);

        //Log.d("LM: ", "mylong " + myLong);
        //Log.d("LM: ", "mylat " + myLat);
        myLong_round = Math.round(myLong * 100.0)/100.0;
        myLat_round = Math.round(myLat * 100.0)/100.0;

        gpsText.append("\n" + "  Längengrad: " + myLong_round.toString());
        gpsText.append("\n" + "  Breitengrad: " + myLat_round.toString());

    }

    public void directToAddNewDangerZone(View view){
        Intent intent = new Intent(DangerZoneActivity.this, AddDangerZoneActivity.class);
        intent.putExtra("objList", objList);
        startActivity(intent);
    }

    public double greatCircleInKilometers(double lat1, double long1, double lat2, double long2) {
        double phi1 = lat1 * PI_RAD;
        double phi2 = lat2 * PI_RAD;
        double lam1 = long1 * PI_RAD;
        double lam2 = long2 * PI_RAD;

        return 6371.01 * acos(sin(phi1) * sin(phi2) + cos(phi1) * cos(phi2) * cos(lam2 - lam1));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DangerZoneActivity.this, StartActivity.class);
        startActivity(intent);
    }
}
