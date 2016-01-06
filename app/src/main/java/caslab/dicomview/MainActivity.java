package caslab.dicomview;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchDICOMFragment sf;
        Intent mitnet = getIntent();

        Log.d(LOG_TAG, "TYPE:" + mitnet.getStringExtra("TYPE")+",ID:"+mitnet.getStringExtra("ID"));

        if( mitnet.getStringExtra("TYPE") != null) {
            sf = SearchDICOMFragment.newInstance(mitnet.getStringExtra("TYPE"), mitnet.getStringExtra("ID"), mitnet.getStringExtra("OPTION"));
            switch (mitnet.getStringExtra("TYPE")){
                case "patients":
                    setTitle("Studies");
                    break;
                case "studies":
                    setTitle("Series");
                    break;
                case "series":
                    setTitle("Instances");
                    break;
            }

        }
        else {
            sf = SearchDICOMFragment.newInstance("patients", null, null);
            setTitle("Patients");
        }


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.container, sf);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();


    }

    private void openSearchFragment(){

    }
}
