package com.adoan.mindrover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.adoan.mindrover.model.ExperimentNavigator
import com.adoan.mindrover.model.ExperimentState


class TrainingNetworkFragment : Fragment() {

    private val experimentNavigator: ExperimentNavigator by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_training_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val runningObserver = Observer<ExperimentState> { state ->
            if (state == ExperimentState.FINISHED) {
                findNavController().navigate(R.id.action_trainingNetworkFragment_to_trainHomeFragment)
            }
        }
        experimentNavigator.doesExperimentRun.observe(viewLifecycleOwner, runningObserver)
    }

}