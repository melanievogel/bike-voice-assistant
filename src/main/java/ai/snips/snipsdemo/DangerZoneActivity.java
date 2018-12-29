package ai.snips.snipsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class DangerZoneActivity extends AppCompatActivity {
   // String[] values = new String[]{"Steil abfallendes Gelände", "Brücke", "Steil abfallender Hang"};

    Button addButton;
    EditText inputText;
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.danger_zones);

        addButton = findViewById(R.id.button);
        inputText = findViewById(R.id.newItem);

        list = new ArrayList<String>();
        list.add("Gefährliches Gelände in 1km");

        ListView listView = findViewById(R.id.listview);


       // for (int i = 0; i < values.length; ++i) {
         //   list.add(values[i]);
       // }

        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        clickItem(listView, adapter);
        addItem();

    }

    public void addItem(){

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newItem = inputText.getText().toString();
                list.add(newItem);
                adapter.notifyDataSetChanged();
            }
        });
    }

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