package ai.snips.snipsdemo;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomAdapter<D> extends ArrayAdapter<DangerZoneObject> {

    private final Context context;
    private final int no;
    private final ArrayList<DangerZoneObject> obj;

    public CustomAdapter(Context context, int no, ArrayList<DangerZoneObject> obj){
        super(context, -1, obj);
        //super(context, -1, obj);

        this.context = context;
        this.no = no;
        this.obj = obj;
    }
}
