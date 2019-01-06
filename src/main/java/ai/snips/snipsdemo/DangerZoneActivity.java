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
import android.view.View;
import android.widget.AdapterView;
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
    private LocationManager locationManager;
    private LocationListener listener;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.danger_zones);

        addButton = findViewById(R.id.button);
        inputText = findViewById(R.id.newItem);
        gpsText = findViewById(R.id.gpsdata);

        //list = new ArrayList<String>();
        objList = new ArrayList<DangerZoneObject>();
        //list.add("Gefährliches Gelände in 1km");

        objList.add(new DangerZoneObject(11, "testname", -122.0840, 37.4220, "12"));

        resultStringList = new ArrayList<String>();

        for(DangerZoneObject dz : objList){
            String name = dz.getName().toString();
            String longi = dz.getLongi().toString();
            String lati = dz.getLati().toString();
            String dist = dz.getDistance().toString();
                resultStringList.add(name+ "                                " + dist+" \n" + longi + " " + lati + " ");
        }

            ListView listView = findViewById(R.id.listview);
            //Log.d("hello", "hi" + objList.get(0).getDistance());

        /*
                adapter = new CustomAdapter<DangerZoneObject>(this, android.R.layout.simple_list_item_1, objList);
                        listView.setAdapter(adapter);
                clickItem(listView, adapter);
*/

        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, resultStringList);


        listView.setAdapter(adapter);
        clickItem(listView, adapter);
        //addItem();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gpsText.append("\n " + location.getLongitude() + " " + location.getLatitude());
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

        configure_button();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.

                //noinspection MissingPermission
                locationManager.requestLocationUpdates("gps", 5000, 0, listener);
    }
/*
    public void addItem(){

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newItem = inputText.getText().toString();
                //list.add(newItem);
                list.add(newItem);
                adapter.notifyDataSetChanged();
            }
        });
    }
*/
    public void clickItem(ListView listView, final ArrayAdapter adapter) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                view.animate().setDuration(2000).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                //list.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
            }

        });

    }
}