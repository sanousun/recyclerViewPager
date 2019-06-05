package com.mhc.samplex

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_demo.view.*

class DemoAdapter(
        private val mContext: Context, private val mData: List<String>
) : RecyclerView.Adapter<DemoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoHolder {
        val view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_demo, parent, false)
        return DemoHolder(view)
    }

    override fun onBindViewHolder(holder: DemoHolder, position: Int) {
        holder.itemView.tv.text = mData[position]
    }

    override fun getItemCount(): Int {
        return mData.size
    }
}

class DemoHolder(itemView: View) : RecyclerView.ViewHolder(itemView)