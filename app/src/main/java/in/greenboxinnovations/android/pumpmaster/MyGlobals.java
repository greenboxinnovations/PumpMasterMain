package in.greenboxinnovations.android.pumpmaster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MyGlobals{
    Context mContext;

    // constructor
    public MyGlobals(Context context){
        this.mContext = context;
    }

    public String getUserName(){
        return "test";
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;

    }

    public boolean isWiFiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
}