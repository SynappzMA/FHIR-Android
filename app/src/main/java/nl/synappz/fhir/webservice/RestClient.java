package nl.synappz.fhir.webservice;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.rest.client.IGenericClient;
import nl.synappz.fhir.FhirApp;

public class RestClient {

    private static final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    private static IGenericClient iGenericClient;
    private static FhirContext ctx;
    private static String baseUrl = "http://fhirtest.uhn.ca/baseDstu2" ;

    public static FhirContext getContext() {
        if (ctx==null) {
            ctx = FhirContext.forDstu2();
            Map<String,String> proxy = getProxyDetails(FhirApp.mContext);
            if (proxy.containsKey("server")&&proxy.containsKey("port")) {
                ctx.getRestfulClientFactory().setProxy(proxy.get("server"), Integer.parseInt(proxy.get("port")));
            }
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

    private static Map<String,String> getProxyDetails(Context context) {
        Map<String,String> proxy = new HashMap<String,String>();
        try {
            if (!IS_ICS_OR_LATER) {
                String proxyAddress = android.net.Proxy.getHost(context);
                proxy.put("server",proxyAddress);
                if (proxyAddress == null || proxyAddress.equals("")) {
                    return proxy;
                }
                proxy.put("port",""+android.net.Proxy.getPort(context));
            } else {
                proxy.put("server",System.getProperty("http.proxyHost"));
                proxy.put("port",""+System.getProperty("http.proxyPort"));
            }
        } catch (Exception ex) {
            //ignore
        }
        return proxy;
    }
}
