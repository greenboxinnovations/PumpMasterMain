package in.greenboxinnovations.android.pumpmaster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class MyGlobals {
    private Context mContext;

    // constructor
    MyGlobals(Context context) {
        this.mContext = context;
    }

    boolean isNetworkConnected() {

        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    boolean isWiFiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connManager != null;
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        assert mWifi != null;
        return mWifi.isConnected();
    }
}