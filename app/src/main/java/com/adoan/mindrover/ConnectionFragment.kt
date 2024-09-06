package com.adoan.mindrover


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.adoan.mindrover.databinding.FragmentConnectionBinding
import com.adoan.mindrover.model.ConnectionState
import com.adoan.mindrover.model.EEGDataListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectionFragment : Fragment() {

    private val sharedViewModel: EEGDataListener by activityViewModels()

    private lateinit var binding: FragmentConnectionBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentConnectionBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
//            sharedViewModel.establishConnection()
            binding.pBar.visibility = View.VISIBLE

            if ((activity as MainActivity?)!!.isNetworkAvailable()) {

                var dataRecieved = false
                (activity as MainActivity?)!!.startConnection()

                val job = lifecycleScope.launch {
                    sharedViewModel.sharedFlow.collect {
                        dataRecieved = true
                    }
                }

                lifecycleScope.launch {
                    delay(500)
                    if (dataRecieved) {
                        job.cancel()
                        findNavController().navigate(R.id.action_connectionFragment_to_eegSignalFragment)
                        Toast.makeText(
                            (activity as MainActivity?)!!,
                            "Connection success",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        job.cancel()
                        binding.pBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            (activity as MainActivity?)!!,
                            "Be sure the device to be connected by WiFi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

            } else {
                Toast.makeText((activity as MainActivity?)!!,"Hello", Toast.LENGTH_SHORT).show()
            }
        }

        val nameObserver = Observer<ConnectionState> { state ->
                when(state) {
                    ConnectionState.PENDING -> {
                        binding.button.visibility = View.GONE
                        binding.pBar.visibility = View.VISIBLE}
                    ConnectionState.CONNECTION -> {findNavController().navigate(R.id.action_connectionFragment_to_eegSignalFragment)}
                    else -> {
                        binding.button.visibility = View.VISIBLE
                        binding.pBar.visibility = View.GONE
                    }
                }
        }

        sharedViewModel.connection.observe(viewLifecycleOwner, nameObserver)

    }
}