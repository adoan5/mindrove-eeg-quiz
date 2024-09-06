package com.adoan.mindrover.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adoan.mindrover.R

class ImpedanceAdapter(
    private val context: Context,
    private var dataset: Array<Int>
): RecyclerView.Adapter<ImpedanceAdapter.ImpedanceViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ImpedanceViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        var impedanceText: TextView = view.findViewById(R.id.impedanceTextbox)
        var channelText: TextView = view.findViewById(R.id.impedanceChannelTextview)
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImpedanceViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.impedance_item, parent, false)
        return ImpedanceViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ImpedanceViewHolder, position: Int) {
//        val impedanceValue:Double = (dataset[position]*100).roundToInt().toDouble() / 100
        val impedanceValue:Double = (dataset[position]*100).toDouble() / 100
        holder.channelText.setText("Channel ${position}:")
        holder.impedanceText.setText("${impedanceValue} Ohm")
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(itemList: Array<Int>) {
        dataset = itemList
        notifyDataSetChanged()
        Log.v("Adapter", "ind: " + dataset[0])
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = dataset.size

}




