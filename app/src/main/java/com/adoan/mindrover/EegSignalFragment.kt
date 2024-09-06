package com.adoan.mindrover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adoan.mindrover.adapter.EEGChannelAdapter
import com.adoan.mindrover.databinding.FragmentEegSignalBinding
import com.adoan.mindrover.model.EEGDataListener
import com.adoan.mindrover.model.GameManager
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.launch

class EegSignalFragment : Fragment() {

    private val eegListener: EEGDataListener by activityViewModels()

    lateinit var graphSeries: Array<LineGraphSeries<DataPoint>>

    lateinit var adapter: EEGChannelAdapter

    private lateinit var binding: FragmentEegSignalBinding

    private val gameManager: GameManager by viewModels()

    private var run: Boolean = false

    var dataRecieved = false

    private fun initGraphSeries(num: Int) {

        for (i in 0 until num) {
            graphSeries = graphSeries.plus(LineGraphSeries())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        graphSeries = arrayOf()
        initGraphSeries(eegListener.numOfChannels)

        lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                eegListener.sharedFlow.collect {
                    dataRecieved = true
                    for (i in 0..it.size-1) {
                        graphSeries[i].resetData(it[i])
                    }
                    if (run) {
                        adapter.updateList(graphSeries)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEegSignalBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.channelRecyclerView
        adapter = EEGChannelAdapter(requireContext(), graphSeries)
        recyclerView.adapter = adapter
        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        binding.testButton.setOnClickListener {
            Toast.makeText((activity as MainActivity?)!!,"Button press", Toast.LENGTH_SHORT).show()
            val data = eegListener.getLatestData(500)
            val fArray = gameManager.transformEEG(data!!)
            (activity as MainActivity?)?.predict(arrayOf(fArray))

        }

        binding.testButton.visibility = View.GONE

    }

}

