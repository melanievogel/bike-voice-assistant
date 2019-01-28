package ai.snips.snipsdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class AddDangerZoneActivity extends AppCompatActivity {

    EditText nameNewDz;
    EditText longi = null;
    EditText lati = null;
    Button save;
    ArrayList<DangerZoneObject> testList;

    DangerZoneObject myNewObj;
    ArrayList<DangerZoneObject> passObj;
    LocationManager location;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_danger_zone);

        nameNewDz = findViewById(R.id.namedz);
        longi = findViewById(R.id.longidz);
        lati = findViewById(R.id.latidz);
        location = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    public ArrayList<DangerZoneObject> saveNewDZ() {
        testList = (ArrayList<DangerZoneObject>) getIntent().getSerializableExtra("objList");
        myNewObj = new DangerZoneObject("", 0.0, 0.0, "");
        myNewObj.setName(nameNewDz.getText().toString());
        myNewObj.setLongi(Double.parseDouble(String.valueOf(longi.getText())));
        myNewObj.setLati(Double.parseDouble(String.valueOf(lati.getText())));
        testList.add(myNewObj);
        Log.d("TEST", "AAAAAAAAAAAAAAAAAAAAAAAAA: " + myNewObj.getName());
        return testList;
    }

    @SuppressLint("MissingPermission")
    public void getMyPosition(View view) {
        Log.d("Test", "Succeed");

        double longitude = location.getLastKnownLocation("gps").getLongitude();
        double latitude =  location.getLastKnownLocation("gps").getLatitude();
        longi.setText(Double.toString(longitude));
        lati.setText(Double.toString(latitude));
    }

    public void directBackToDangerZoneActivity(View view) {
        passObj = saveNewDZ();
        Intent intent = new Intent(AddDangerZoneActivity.this, DangerZoneActivity.class);
        intent.putExtra("serialize_data", passObj);
        startActivity(intent);
    }
}
