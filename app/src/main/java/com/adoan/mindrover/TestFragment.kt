package com.adoan.mindrover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adoan.mindrover.databinding.FragmentTestBinding
import com.adoan.mindrover.model.EEGDataListener
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TestFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val eegListener: EEGDataListener by viewModels()

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    lateinit var graphView1: GraphView
    lateinit var graphView2: GraphView

    lateinit var graphSeries: Array<LineGraphSeries<DataPoint>>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        graphSeries = arrayOf()
        for (i in 0..8) {
            graphSeries = graphSeries.plus(LineGraphSeries())
        }

//        lifecycleScope.launch {
//
//
//            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                eegListener.latestData.collect {
////                    if (graphView1 != null)
////                        Log.d("Retrevition", "Package size ${it.size}")
//                        for (i in 0..it.size-1) {
//                            graphSeries[i].resetData(it[i])
//                        }
//                }
//            }
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_test, container, false)

        graphView1 = root.findViewById(R.id.graph1)
        graphView2 = root.findViewById(R.id.graph2)

        graphView1.addSeries(graphSeries[0])
        graphView1.viewport.setMinX(0.0)
        graphView1.viewport.setMaxX(50.0)

        graphView2.addSeries(graphSeries[1])
        graphView2.viewport.setMinX(0.0)
        graphView2.viewport.setMaxX(50.0)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


        companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TestFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

