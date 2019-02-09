package ai.snips.snipsdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
    String p;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_danger_zone);

        nameNewDz = findViewById(R.id.namedz);
        longi = findViewById(R.id.longidz);
        lati = findViewById(R.id.latidz);
        location = (LocationManager) getSystemService(LOCATION_SERVICE);

    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
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


    public void getMyPosition(View view) {
        Log.d("Test", "Succeed");

        double longitude = location.getLastKnownLocation("gps").getLongitude();
        double latitude = location.getLastKnownLocation("gps").getLatitude();
        longi.setText(Double.toString(longitude));
        lati.setText(Double.toString(latitude));
    }

    public void directBackToDangerZoneActivity(View view) {
        passObj = saveNewDZ();
        write(getApplicationContext().getFilesDir() + "/zones.bike", passObj);
        Intent intent = new Intent(AddDangerZoneActivity.this, DangerZoneActivity.class);
        intent.putExtra("serialize_data", passObj);
        startActivity(intent);
    }

    public void write(String file, ArrayList<DangerZoneObject> myArrayList) {
        FileOutputStream out;
        myArrayList.addAll(read(getApplicationContext().getFilesDir() + "/myfile"));
        try {
            out = new FileOutputStream(file);
            for (int i = myArrayList.size() - 1; i > -1; i--) {
                String content = "name\n" + myArrayList.get(i).getName() + "\n" + myArrayList.get(i).getLati() + "\n" + myArrayList.get(i).getLongi() + "\n";
                out.write(content.getBytes());
            }
            out.close();
        } catch (Exception e) { //fehlende Permission oder sd an pc gemountet
            e.printStackTrace();
        }
    }

    public ArrayList<DangerZoneObject> read(String file) {
        ArrayList<DangerZoneObject> result = new ArrayList<>();
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line;
            while ((line = buf.readLine()) != null) {
                if (line.equals("name")) {
                    DangerZoneObject object = new DangerZoneObject("", 0.0, 0.0, "");
                    object.setName(buf.readLine());
                    object.setLati(Double.parseDouble(buf.readLine()));
                    object.setLongi(Double.parseDouble(buf.readLine()));
                    result.add(object);
                }
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
