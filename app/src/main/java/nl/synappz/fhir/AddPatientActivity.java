package nl.synappz.fhir;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.UUID;

import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import nl.synappz.fhir.webservice.RestClient;

public class AddPatientActivity extends AppCompatActivity {

    private EditText surname;
    private EditText givenname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.fhirlogo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        surname = (EditText)findViewById(R.id.surname);
        givenname = (EditText)findViewById(R.id.givenname);
    }

    public void add(View view) {

        if (givenname.getText().length()==0||surname.getText().length()==0) {
            new AlertDialog.Builder(AddPatientActivity.this).setTitle(getResources().getString(R.string.error)).setMessage(getResources().getString(R.string.inputerror)).setNegativeButton("OK", null).show();
            return;
        }

        final Patient newPatient = new Patient();
        newPatient.addName().addFamily(surname.getText().toString()).addGiven(givenname.getText().toString());

        Thread validateThread = new Thread() {
            @Override
            public void run() {

                MethodOutcome outcome = RestClient.getClient().create()
                        .resource(newPatient)
                        .prettyPrint()
                        .encodedJson()
                        .execute();

                OperationOutcome operationOutcome = (OperationOutcome) outcome.getOperationOutcome();

                Boolean hasError = false;
                for (OperationOutcome.Issue nextIssue : operationOutcome.getIssue()) {
                    if (nextIssue.getSeverityElement().getValueAsEnum().ordinal() == IssueSeverityEnum.ERROR.ordinal()) {
                        Log.e(FhirApp.TAG, nextIssue.getDiagnostics());
                        hasError =true;
                    }
                }

                if (hasError) {
                    showErrorAlert();
                    return;
                }

                showDoneAlert("Created a new patient:\n" + outcome.getId().getValue());

            }
        };
        validateThread.start();
    }

    void showErrorAlert() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(AddPatientActivity.this).setTitle(getResources().getString(R.string.error)).setMessage(getResources().getString(R.string.failedvalidation)).setNegativeButton("OK", null).show();
            }
        });
    }

    void showDoneAlert(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(AddPatientActivity.this).setTitle(getResources().getString(R.string.done)).setMessage(message).setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                }).show();
            }

        });
    }
}
