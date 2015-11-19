package nl.synappz.fhir;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.parser.IParser;
import nl.synappz.fhir.webservice.RestClient;

public class PatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.fhirlogo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final TextView dump = (TextView)findViewById(R.id.textViewDump);

        Bundle bundle =  getIntent().getExtras();
        final String patientID = bundle.getString("patientID");

        Thread getPatient = new Thread() {
            @Override
            public void run() {
                UriDt uri = new UriDt(patientID);
                Patient patient = (Patient)RestClient.getClient().read(uri);

                IParser parser = RestClient.getContext().newXmlParser();
                parser.setPrettyPrint(true);

                final String xmldump = parser.encodeResourceToString(patient);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dump.setText(xmldump);
                    }
                });

            }
        };
        getPatient.start();


    }
}
