package nl.synappz.fhir;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class FhirApp extends Application {

    public static String TAG = "FHIR ANDROID";
    public static Context mContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        MultiDex.install(this);
        mContext = this;
        super.onCreate();
    }
}
