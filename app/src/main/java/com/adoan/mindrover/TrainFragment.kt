package com.adoan.mindrover

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.adoan.mindrover.databinding.FragmentTrainBinding
import com.adoan.mindrover.model.EEGDataListener
import com.adoan.mindrover.model.ExperimentCommand
import com.adoan.mindrover.model.ExperimentNavigator
import com.adoan.mindrover.model.ExperimentState
import com.adoan.mindrover.model.GameManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrainFragment : Fragment() {

    private val experimentNavigator: ExperimentNavigator by activityViewModels()
    private val eegDataListener: EEGDataListener by activityViewModels()
    private val gameManager: GameManager by activityViewModels()

    private lateinit var binding: FragmentTrainBinding

    private var experimentRuns = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrainBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        experimentRuns = true

        val commandObserver = Observer<ExperimentCommand> { state ->
            binding.imageView3.setImageResource(state.imgId)
            if (experimentRuns) {
                Log.d("Observer", "Observed:${state.imgId}")
                binding.startTrainTexview.setText(state.textId)
            }
        }

        experimentNavigator.actualCommand.observe(viewLifecycleOwner, commandObserver)

        val runningObserver = Observer<ExperimentState> { state ->
            if (state == ExperimentState.TRAINING) {
                findNavController().navigate(R.id.action_trainFragment_to_trainingNetworkFragment)

                lifecycleScope.launch(Dispatchers.IO) {
                    delay(10)
                    gameManager.setRightThreshold(experimentNavigator.rightThreshold)
                    gameManager.setLeftThreshold(experimentNavigator.leftThreshold)
                    val experimentResult = experimentNavigator.getLabeledDataArrays(shiftGeneration = true)
                    (activity as MainActivity).trainModel(
                        experimentResult.first,
                        experimentResult.second
                    )
                    experimentNavigator.finishExperiment()
                }
            }
        }

        experimentNavigator.doesExperimentRun.observe(viewLifecycleOwner, runningObserver)


        val intObserver = Observer<Int> { state ->
            binding.imageView3.setImageResource(state)
        }

        experimentNavigator.experimentRun.observe(viewLifecycleOwner, intObserver)



        val impedanceObserver = Observer<Array<Int>> { state ->
            if (state.size > 0)
                binding.startTrainTexview.setText("${state[0]}")
        }

        eegDataListener.impedances.observe(viewLifecycleOwner, impedanceObserver)

        experimentNavigator.setEEGListener(eegDataListener)
        experimentNavigator.startExperiment(5)

    }

}