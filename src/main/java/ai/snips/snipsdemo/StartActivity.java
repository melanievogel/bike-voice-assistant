package ai.snips.snipsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);

    }

    public void directToMainActivity(View view){
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void directToDangerZoneActivity(View view){
        Intent intent = new Intent(StartActivity.this, DangerZoneActivity.class);
        startActivity(intent);
    }
}
