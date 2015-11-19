package nl.synappz.fhir;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ca.uhn.fhir.model.base.resource.BaseOperationOutcome;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import nl.synappz.fhir.webservice.RestClient;

public class PatientActivity extends AppCompatActivity {

    private EditText surname;
    private EditText givenname;
    private TextView dump;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.fhirlogo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        surname = (EditText)findViewById(R.id.surname);
        givenname = (EditText)findViewById(R.id.givenname);
        dump = (TextView)findViewById(R.id.textViewDump);

        Bundle bundle =  getIntent().getExtras();
        final String patientID = bundle.getString("patientID");

        Thread getPatient = new Thread() {
            @Override
            public void run() {
                UriDt uri = new UriDt(patientID);
                patient = (Patient)RestClient.getClient().read(uri);

                IParser parser = RestClient.getContext().newXmlParser();
                parser.setPrettyPrint(true);

                final String xmldump = parser.encodeResourceToString(patient);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        surname.setText(patient.getName().get(0).getFamily().get(0).getValueNotNull());
                        givenname.setText(patient.getName().get(0).getGivenFirstRep().getValueNotNull());
                        dump.setText(xmldump);
                    }
                });

            }
        };
        getPatient.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.appbar_del, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_patient:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.action_delete)).setMessage(getResources().getString(R.string.deletepatient)).setPositiveButton("Delete",new   DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      delete();
                    }}).setNegativeButton("Cancel", null).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void update(View view) {

        StringDt familyname = patient.getName().get(0).getFamily().get(0);
        familyname.setValue(surname.getText().toString());

        StringDt given = patient.getName().get(0).getGivenFirstRep();
        given.setValue(givenname.getText().toString());

        Thread updatePatient = new Thread() {
            @Override
            public void run() {
                final MethodOutcome outcome = RestClient.getClient().update().resource(patient).execute();

                OperationOutcome operationOutcome = (OperationOutcome) outcome.getOperationOutcome();

                Boolean hasError = false;
                for (OperationOutcome.Issue nextIssue : operationOutcome.getIssue()) {
                    if (nextIssue.getSeverityElement().getValueAsEnum().ordinal() == IssueSeverityEnum.ERROR.ordinal()) {
                        Log.e(FhirApp.TAG, nextIssue.getDiagnostics());
                        hasError=true;
                    }
                }

                if (hasError) {
                    showErrorAlert();
                    return;
                }

                showDoneAlert("Updated patient:\n" + outcome.getId().getValue());
            }
        };
        updatePatient.start();

    }

    public void delete() {
        Thread deletePatient = new Thread() {
            @Override
            public void run() {
                BaseOperationOutcome deleteResponse = RestClient.getClient().delete().resource(patient).execute();
                if (deleteResponse!=null) {
                    showDoneAlert("Patient deleted");
                }
            }
        };
        deletePatient.start();
    }

    void showErrorAlert() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(PatientActivity.this).setTitle(getResources().getString(R.string.error)).setMessage(getResources().getString(R.string.failedvalidation)).setNegativeButton("OK", null).show();
            }
        });
    }

    void showDoneAlert(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(PatientActivity.this).setTitle(getResources().getString(R.string.done)).setMessage(message).setNegativeButton("OK",null).show();
            }

        });
    }
}
