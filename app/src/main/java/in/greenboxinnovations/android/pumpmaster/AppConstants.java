package in.greenboxinnovations.android.pumpmaster;

public interface AppConstants {

//    String BASE_URL = "https://pay.greenboxinnovations.in";
    String BASE_URL = "http://192.168.0.100/fuelqr";


    String SCAN_CUST_QR = BASE_URL + "/pump_scan";
    String MOVE_PEND_TO_COMPLETED = BASE_URL + "/pending_trans_completed";

}