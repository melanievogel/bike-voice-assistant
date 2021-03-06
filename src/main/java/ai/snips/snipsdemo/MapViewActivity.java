package ai.snips.snipsdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import static org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay.backgroundColor;
import static org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay.fontColor;
import static org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay.fontSizeDp;

//import org.osmdroid.config.Configuration;

/*
Offline map implemenation adopted from:
https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/tileproviders/SampleOfflineOnly.java
 */

public class MapViewActivity extends Activity {
   MapView map = null;
    private MapTileProviderArray mapProvider;

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
        String[] urls = {};
        // map.setTileSource(new XYTileSource(Environment.getExternalStorageDirectory() + "osm/droid/OpenStreetMap Hikebikemap.de", 2, 17, 256, ".PNG", urls));

        IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(ctx);
/*
        String[] urls = {};
        //XYTileSource source3 = new XYTileSource(Environment.getExternalStorageDirectory() + "/osmdroid/u/tiles/tiles.sqlite", 2, 17, 256, ".PNG", urls);
        XYTileSource source3 = new XYTileSource("OpenStreetMap Hikebikemap.de", 2, 17, 256, ".PNG", urls);
        IArchiveFile[] archives2 = { ArchiveFileFactory.getArchiveFile(new File(Environment.getExternalStorageDirectory() + "/osmdroid/u/tiles/tiles.sqlite"))};
        MapTileModuleProviderBase moduleProvider = new MapTileFileArchiveProvider(registerReceiver,source3,archives2);
        this.mapProvider = new MapTileProviderArray(source3, null, new MapTileModuleProviderBase[] { moduleProvider });
        this.map.setTileSource(FileBasedTileSource.getSource(String.valueOf(source3)));
*/
/*
        String source2 = "";
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroid/");
        File[] fk = f.listFiles();
        try {
            OfflineTileProvider offlineTileProvider = new OfflineTileProvider(registerReceiver, new File[]{fk[0]});
            IArchiveFile[] archives = offlineTileProvider.getArchives();
            Set<String> tileSources = archives[0].getTileSources();
            source2 = tileSources.iterator().next();
            this.map.setTileSource(FileBasedTileSource.getSource(source2));
            map.setTileProvider(offlineTileProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

        MapTileProviderBasic mapTileProviderBasic = new MapTileProviderBasic(ctx);
        map.setTileProvider(mapTileProviderBasic);
        map.setTileSource(new XYTileSource("OpenStreetMap Hikebikemap.de", 0, 17, 256, ".PNG", urls));

        map.setUseDataConnection(false);
        mapTileProviderBasic.setUseDataConnection(false);

        IMapController mapController = map.getController();
        mapController.setZoom(17);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController.setZoom(17);
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