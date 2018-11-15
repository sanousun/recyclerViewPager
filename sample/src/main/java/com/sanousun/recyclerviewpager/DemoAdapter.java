package com.sanousun.recyclerviewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.DemoHolder> {

    private Context mContext;
    private List<String> mData;

    public DemoAdapter(Context context, List<String> data) {
        mContext = context;
        mData = data;
    }

    @NonNull
    @Override
    public DemoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_demo, parent, false);
        return new DemoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DemoHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class DemoHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        DemoHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv);
        }
    }
}
