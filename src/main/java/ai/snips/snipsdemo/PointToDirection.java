package ai.snips.snipsdemo;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import utils.ActionsUtil;

public class PointToDirection extends AppCompatActivity implements SensorEventListener {

    TextView DegreeTV;
    Sensor mMagnetic;
    Sensor mAccelero;
    LocationManager lm;
    LocationListener ls;
    private SensorManager SensorManage;
    private ImageView compassImage;
    private float DegreeStart = 0f;
    private float[] mGravity;
    private float[] mGeomagnetic;
    int times = 0;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_to_direction);
        compassImage = findViewById(R.id.compass_image);
        DegreeTV = findViewById(R.id.DegreeTV);
        SensorManage = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMagnetic = SensorManage.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelero = SensorManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        doIt();
        lm.requestLocationUpdates(ActionsUtil.getCriteria(lm), 0, (float) 0.5, ls);

    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorManage.unregisterListener(this);
        lm.removeUpdates(ls);

    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        lm.requestLocationUpdates(ActionsUtil.getCriteria(lm), 0, (float) 0.5, ls);
        SensorManage.registerListener(this, mAccelero, SensorManager.SENSOR_DELAY_NORMAL);
        SensorManage.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                if((Math.abs(mGravity[0])+Math.abs(mGravity[1])+Math.abs(mGravity[2]))<12){
                    times++;
                    if(times<40) {
                        return;
                    }
                }
                times = 0;
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                float degree = Math.round(Math.toDegrees(orientation[0]));

                DegreeTV.setText("Heading: " + degree + " degrees");
                // rotation animation - reverse turn degree degrees
                RotateAnimation ra = new RotateAnimation(
                        DegreeStart,
                        -degree,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                // set the compass animation after the end of the reservation status
                ra.setFillAfter(true);
                // set how long the animation for the compass image will take place
                ra.setDuration(1000);
                // Start animation of compass image
                compassImage.startAnimation(ra);
                DegreeStart = -degree;

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("MissingPermission")
    private void doIt() {
        // LocationManager-Instanz ermitteln
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lm = getSystemService(LocationManager.class);
        }
        if (lm == null) {
            finish();
        }
        // Provider mit genauer Auflösung
        // und mittlerem Energieverbrauch
        // LocationListener-Objekt erzeugen
        ls = new LocationListener() {
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
            }
        };
    }
}
