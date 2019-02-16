package ai.snips.snipsdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class DangerZoneActivity extends AppCompatActivity {
    static double PI_RAD = Math.PI / 180.0;
    Button addButton;
    Button show_DZ;
    TextView gpsText;
    ArrayList<DangerZoneObject> objList;
    ArrayAdapter<String> adapter;
    ArrayList<String> resultStringList;
    ListView listView;
    String p;
    Double lati;
    Double longi;
    private LocationManager m;
    private LocationListener l;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.danger_zones);

        addButton = findViewById(R.id.button);
        gpsText = findViewById(R.id.gpsdata);
        show_DZ = findViewById(R.id.show_dangerzones);
        listView = findViewById(R.id.listview);

        m = (LocationManager) getSystemService(LOCATION_SERVICE);
        doIt();
        m.requestLocationUpdates(p, 0, (float) 0.5, l);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        objList = new ArrayList<DangerZoneObject>();

     /*   ArrayList<DangerZoneObject> test = (ArrayList<DangerZoneObject>) getIntent().getSerializableExtra("serialize_data");

        if (test == null) {
            //   objList.add(new DangerZoneObject("Berg", -122.0840, 37.4220, "12"));
        } else {
            for (DangerZoneObject i : test) {
                objList.add(i);
            }
        }*/
        objList.addAll(read(getApplicationContext().getFilesDir() + "/zones.bike", null));
        resultStringList = new ArrayList<String>(5);


        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, resultStringList);
        listView.setAdapter(adapter);

        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                ArrayList<DangerZoneObject> list = new ArrayList<>();
                write(getApplicationContext().getFilesDir() + "/zones.bike", list,item);
                Log.d("Tag", item);
                view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        resultStringList.remove(item);
                        adapter.notifyDataSetChanged();
                        view.setAlpha(1);
                    }
                });
            }
        });
*/

        //      myLong_round = Math.round(myLong * 100.0) / 100.0;
        //    myLat_round = Math.round(myLat * 100.0) / 100.0;
        registerForContextMenu(listView);

        /*
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    final String item = (String) parent.getItemAtPosition(position);
                    view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
                        @Override
                        public void run() {

                            resultStringList.remove(item);
                            //File file = new File(getApplicationContext().getFilesDir() + "/zones.bike");
                            // file.delete();
                            adapter.notifyDataSetChanged();
                            view.setAlpha(1);
                        }
                    });
                }
            });

            //      myLong_round = Math.round(myLong * 100.0) / 100.0;
            //    myLat_round = Math.round(myLat * 100.0) / 100.0;
        }*/
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        switch (item.getItemId()){
            /*case R.id.action_daten_aktualisieren:
                Toast.makeText(this, "Aktualisieren", Toast.LENGTH_SHORT).show();
                return true;*/
            case R.id.action_daten_loeschen:
                String itemt = resultStringList.get(position);
                //objList.remove(itemt);
                resultStringList.remove(itemt.toString());
                adapter.notifyDataSetChanged();
                /*
                ArrayList<DangerZoneObject> list = new ArrayList<>();
                write(getApplicationContext().getFilesDir() + "/zones.bike", list,item);
               */
                Toast.makeText(this, "Gelöscht", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                m.removeUpdates(l);
            }
        }
    }

    public void directToAddNewDangerZone(View view) {
        Intent intent = new Intent(DangerZoneActivity.this, AddDangerZoneActivity.class);
        intent.putExtra("objList", objList);
        startActivity(intent);
    }

    public void directToMapActivity(View v) {
        Intent intent = new Intent(DangerZoneActivity.this, MapViewActivity.class);
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

    public void write(String file, ArrayList<DangerZoneObject> myArrayList, String delete) {
        FileOutputStream out;
        myArrayList.addAll(read(getApplicationContext().getFilesDir() + "/zones.bike", delete));
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

    public ArrayList<DangerZoneObject> read(String file, String delete) {
        ArrayList<DangerZoneObject> result = new ArrayList<>();
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line;
            String temp;
            while ((line = buf.readLine()) != null) {
                if (line.equals("name")) {
                    DangerZoneObject object = new DangerZoneObject("", 0.0, 0.0, "");
                    object.setName(buf.readLine());
                    object.setLati(Double.parseDouble(buf.readLine()));
                    object.setLongi(Double.parseDouble(buf.readLine()));
                    temp = object.getName() + "\n" + "LG: " + object.getLongi() + ", BG: " + object.getLati() + " \nDist: ";
                    if (delete != null) {
                        if (!delete.contains(temp)) {
                            result.add(object);
                        }
                    } else {
                        result.add(object);
                    }
                }
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    private void doIt() {
        // LocationManager-Instanz ermitteln
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            m = getSystemService(LocationManager.class);
        }
        if (m == null) {
            finish();
        }
        // Provider mit genauer Auflösung
        // und mittlerem Energieverbrauch
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        p = m.getBestProvider(criteria, true);
        // LocationListener-Objekt erzeugen
        l = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                lati = location.getLatitude();
                longi = location.getLongitude();
                gpsText.setText("Meine aktuellen Koordinaten:" + "\n" + "  Längengrad: " +
                        longi.toString() + "\n" + "  Längengrad: " + lati.toString());
               /* gpsText.append("\n" + "  Längengrad: " + longi.toString());
                gpsText.append("\n" + "  Breitengrad: " + lati.toString());*/
                resultStringList.clear();
                for (DangerZoneObject dz : objList) {
                    String name = dz.getName();
                    Double longi2 = dz.getLongi();
                    Double lati2 = dz.getLati();
                    Double dist2 = greatCircleInKilometers(lati2, longi2, lati, longi);
                    resultStringList.add(name + "\n" + "LG: " + longi2 + ", BG: " + lati2 + " " + "\n" + "Dist: " + Math.round(dist2) + " km");
                }
                adapter.notifyDataSetChanged();
            }
        };
    }

    public class MyUndoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            // Code to undo the user's last action
        }
    }
}
