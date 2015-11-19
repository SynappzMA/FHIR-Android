package nl.synappz.fhir;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ca.uhn.fhir.model.api.BundleEntry;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import nl.synappz.fhir.adapters.SearchListAdapter;
import nl.synappz.fhir.webservice.RestClient;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    private SearchListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.fhirlogo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SearchView search = (SearchView)findViewById(R.id.searchView);
        search.setOnQueryTextListener(this);
        search.onActionViewExpanded();
        try {
            Field searchField = SearchView.class.getDeclaredField("mCloseButton");
            searchField.setAccessible(true);
            ImageView closeBtn = (ImageView) searchField.get(search);
            closeBtn.setEnabled(false);
            closeBtn.setImageDrawable(getResources().getDrawable(R.color.transparant));
        } catch (NoSuchFieldException e) {
            Log.e(FhirApp.TAG, e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Log.e(FhirApp.TAG, e.getMessage(), e);
        }

        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        adapter = new SearchListAdapter(this,new ArrayList<BundleEntry>());
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if (newText.length()>0) {
            Thread searchThread = new Thread() {
                @Override
                public void run() {
                    final ca.uhn.fhir.model.api.Bundle results =  RestClient.getClient().search().forResource(Patient.class).limitTo(100).where(Patient.FAMILY.matches().value(newText)).execute();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clear();
                            if (results.getEntries().size()>0) {
                                adapter.addAll(results.getEntries());
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };
            searchThread.start();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       BundleEntry entry = adapter.getItem(position);
        if (entry != null) {
            Patient patient = (Patient) entry.getResource();
            Intent patientIntent = new Intent(this,PatientActivity.class);
            patientIntent.putExtra("patientID",patient.getId().getValue());
            startActivity(patientIntent);
        }
    }
}
