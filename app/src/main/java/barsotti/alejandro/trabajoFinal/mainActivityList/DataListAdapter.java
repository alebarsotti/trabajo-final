package barsotti.alejandro.trabajoFinal.mainActivityList;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import barsotti.alejandro.trabajoFinal.R;

public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.ViewHolder> {
    private List<DataItem> mDataSet;

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView address;
        TextView date;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_name);
            address = view.findViewById(R.id.tv_address);
            date = view.findViewById(R.id.tv_date);
        }
    }

    public DataListAdapter(List<DataItem> dataSet) {
        mDataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataItem dataItem = mDataSet.get(position);
        holder.name.setText(dataItem.Name);
        holder.address.setText(dataItem.Address);
        holder.date.setText(dataItem.Date);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
