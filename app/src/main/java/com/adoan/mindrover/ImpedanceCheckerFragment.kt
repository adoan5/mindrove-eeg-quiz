package com.adoan.mindrover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.adoan.mindrover.adapter.ImpedanceAdapter
import com.adoan.mindrover.databinding.FragmentImpedanceCheckerBinding
import com.adoan.mindrover.model.EEGDataListener

class ImpedanceCheckerFragment : Fragment() {

    private val eegListener: EEGDataListener by activityViewModels()
    lateinit var impedances: Array<Int>
    private lateinit var binding: FragmentImpedanceCheckerBinding
    lateinit var adapter: ImpedanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImpedanceCheckerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.impedanceRecycler

        impedances = arrayOf(5, 6, 7, 8, 9, 10)

        adapter = ImpedanceAdapter(requireContext(), impedances)
        recyclerView.adapter = adapter
        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
//        recyclerView.setHasFixedSize(true)


        val impedanceObserver = Observer<Array<Int>> { state ->
            impedances = state
            adapter.updateList(impedances)
        }

        eegListener.impedances.observe(viewLifecycleOwner, impedanceObserver)

    }

}