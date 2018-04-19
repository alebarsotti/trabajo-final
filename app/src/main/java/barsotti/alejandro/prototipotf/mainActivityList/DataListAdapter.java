package barsotti.alejandro.prototipotf.mainActivityList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import barsotti.alejandro.prototipotf.R;

public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.ViewHolder> {
    private List<DataItem> mDataSet;

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        TextView mAddress;
        TextView mDate;

        ViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.tv_name);
            mAddress = view.findViewById(R.id.tv_address);
            mDate = view.findViewById(R.id.tv_date);
        }
    }

    public DataListAdapter(List<DataItem> dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataItem dataItem = mDataSet.get(position);
        holder.mName.setText(dataItem.Name);
        holder.mAddress.setText(dataItem.Address);
        holder.mDate.setText(dataItem.Date);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}
