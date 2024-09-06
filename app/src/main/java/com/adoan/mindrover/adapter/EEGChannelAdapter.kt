package com.adoan.mindrover.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adoan.mindrover.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class EEGChannelAdapter(
    private val context: Context,
    private var dataset: Array<LineGraphSeries<DataPoint>>
): RecyclerView.Adapter<EEGChannelAdapter.ItemViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        var graphView: GraphView = view.findViewById(R.id.graph)
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.channel_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.graphView.removeAllSeries()
        holder.graphView.addSeries(item)
        holder.graphView.viewport.setMinX(0.0)
        holder.graphView.viewport.setMaxX(50.0)
        holder.graphView.viewport.setMinY(-20.0)
        holder.graphView.viewport.setMaxY(20.0)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(itemList: Array<LineGraphSeries<DataPoint>>) {
        dataset = itemList
        notifyDataSetChanged()
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = dataset.size
}