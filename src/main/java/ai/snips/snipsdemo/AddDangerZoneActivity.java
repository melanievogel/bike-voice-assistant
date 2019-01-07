package ai.snips.snipsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.w3c.dom.Text;

public class AddDangerZoneActivity extends AppCompatActivity {

    EditText nameNewDz;
    EditText longi;
    EditText lati;
    Button save;

    DangerZoneObject myNewObj;
    DangerZoneObject passObj;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_danger_zone);

        nameNewDz = findViewById(R.id.namedz);
        longi = findViewById(R.id.longidz);
        lati = findViewById(R.id.latidz);

    }

    public DangerZoneObject saveNewDZ(){
        myNewObj = new DangerZoneObject("", 0.0, 0.0, "");
        myNewObj.setName(nameNewDz.getText().toString());
        myNewObj.setLongi(Double.parseDouble(String.valueOf(longi.getText())));
        Log.d("as", "ObjName: " + myNewObj.getName());
        return myNewObj;
    }

    public void directBackToDangerZoneActivity(View view){
        passObj = saveNewDZ();
        Intent intent = new Intent(AddDangerZoneActivity.this, DangerZoneActivity.class);
        //intent.putExtra();
        startActivity(intent);
    }
}
