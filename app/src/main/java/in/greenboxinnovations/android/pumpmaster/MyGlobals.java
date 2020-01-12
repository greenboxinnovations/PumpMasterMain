package in.greenboxinnovations.android.pumpmaster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class MyGlobals{
    private Context mContext;

    // constructor
    MyGlobals(Context context){
        this.mContext = context;
    }

//    public String getUserName(){
//        return "test";
//    }

    boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo ni = cm.getActiveNetworkInfo();

        assert ni != null;
        if (ni.getTypeName().equalsIgnoreCase("MOBILE")){
            return ni.isConnected();
        }
        return false;
    }

    boolean isWiFiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        assert mWifi != null;
        return mWifi.isConnected();
    }
}