package com.sanousun.recyclerviewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * @author dashu
 * @date 2018/4/25
 */
public class TestScrollAdapter extends RecyclerView.Adapter<TestScrollAdapter.TestScrollHolder> {

    private Context mContext;
    private List<String> mData;

    public TestScrollAdapter(Context context, List<String> data) {
        mContext = context;
        mData = data;
    }

    @NonNull
    @Override
    public TestScrollHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_test_scroll, parent, false);
        return new TestScrollHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestScrollHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class TestScrollHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        TestScrollHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv);
        }
    }
}
