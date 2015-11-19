package nl.synappz.fhir.webservice;

import android.util.Log;

import java.io.IOException;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.rest.client.IGenericClient;
import nl.synappz.fhir.FhirApp;

public class RestClient {

    private static IGenericClient iGenericClient;
    private static FhirContext ctx;
    private static String baseUrl = "http://fhirtest.uhn.ca/baseDstu2" ;

    public static FhirContext getContext() {
        if (ctx==null) {
            ctx = FhirContext.forDstu2();
        }
        return ctx;
    }

    public static IGenericClient getClient() {
        if (ctx == null) {
            ctx = getContext();
        }
        if (iGenericClient == null) {
            iGenericClient = ctx.newRestfulGenericClient(baseUrl);
        }
        return iGenericClient;
    }
}
