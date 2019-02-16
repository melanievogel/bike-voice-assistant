package ai.snips.snipsdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import static org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay.backgroundColor;
import static org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay.fontColor;
import static org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay.fontSizeDp;

//import org.osmdroid.config.Configuration;

public class MapViewActivity extends Activity {
    MapView map = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<DangerZoneObject> DZs=null;
            DZs = (ArrayList<DangerZoneObject>) getIntent().getSerializableExtra("objList");
            if(DZs.size()==0) {
                Toast toast = Toast.makeText(getApplicationContext(), "Keine Gefahrenzonen in der Nähe", Toast.LENGTH_LONG);
                toast.show();
                Intent intent = new Intent(MapViewActivity.this, DangerZoneActivity.class);
                startActivity(intent);
            }
        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done 
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.map_view);

        map = findViewById(R.id.map);
        map.setUseDataConnection(false);
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        map.setTileSource(new XYTileSource(Environment.getExternalStorageState()+"/osmdroid/Bamberg.sqlite", 2, 17, 256, ".png", new String[] {}));
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController.setZoom((int) 20);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        GeoPoint startPoint = new GeoPoint(locationManager.getLastKnownLocation("gps").getLatitude(), locationManager.getLastKnownLocation("gps").getLongitude());
        mapController.setCenter(startPoint);
        Marker.ENABLE_TEXT_LABELS_WHEN_NO_IMAGE = true;
//build the marker
        Marker m = new Marker(map);
        m.setTextLabelBackgroundColor(backgroundColor);
        m.setTextLabelFontSize(fontSizeDp);
        m.setTextLabelForegroundColor(fontColor);
        m.setTitle("Dein Standort");
//must set the icon to null last
        m.setIcon(null);
        m.setPosition(startPoint);
        map.getOverlays().add(m);
        for (DangerZoneObject DZ : DZs) {
            Marker startMarker = new Marker(map);
            startMarker.setPosition(new GeoPoint(DZ.getLati(), DZ.getLongi()));
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setTitle(DZ.getName());
            map.getOverlays().add(startMarker);
        }

    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use 
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use 
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        //map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

}