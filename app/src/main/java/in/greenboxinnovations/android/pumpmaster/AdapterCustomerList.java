package in.greenboxinnovations.android.pumpmaster;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterCustomerList extends RecyclerView.Adapter<AdapterCustomerList.CustomerViewHolder> {

    private ArrayList<POJO_id_string> customerList;
    private Context context;
    private gridListener mListener;


    public AdapterCustomerList(ArrayList<POJO_id_string> customerList, Context context, gridListener mListener) {
        this.customerList = customerList;
        this.context = context;
        this.mListener = mListener;
    }


    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView cust_name;
        RelativeLayout tile;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            cust_name = itemView.findViewById(R.id.tv_cust_single_name);
            tile = itemView.findViewById(R.id.rl_cust_single);
        }
    }


    @Override
    public AdapterCustomerList.CustomerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customer_single, parent, false);
        CustomerViewHolder vh = new CustomerViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, final int position) {
        holder.cust_name.setText(customerList.get(position).getDisplay_name());
        holder.tile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logStuff(position);
            }
        });
    }

//    @Override
//    public void onBindViewHolder(AdapterCustomerList.CustomerViewHolder holder, int position) {
////        holder.kartNum.setText(customerList.get(position).getDisplay_name());
//        holder.kartNum.setText("jiggy");
//        Log.e("each",""+customerList.size());
//
////        if (customerList.get(position).isActive()) {
////            holder.tile.setBackgroundColor(Color.WHITE);
////        } else {
////            holder.tile.setBackgroundColor(Color.parseColor("#212121"));
////        }
//    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public void updateReceiptsList(ArrayList<POJO_id_string> newlist) {
//        customerList.clear();
//        customerList.addAll(newlist);
//        this.notifyDataSetChanged();
//        Log.e("ada",""+customerList.size());
    }


    public interface gridListener {
        //        void gridCutEngine(View v, int position);
//
//        void makeKartActive(View v, int position);
//
//        boolean gridCheckActive(View v, int position);
//
//        void changeKart(View v, int position);
//
//        void dialog(int position);
        void logStuff(int position);
    }
}
