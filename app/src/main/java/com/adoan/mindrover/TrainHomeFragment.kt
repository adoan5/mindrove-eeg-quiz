package com.adoan.mindrover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.adoan.mindrover.databinding.FragmentTrainHomeBinding

class TrainHomeFragment : Fragment() {

    private lateinit var binding: FragmentTrainHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTrainHomeBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            binding.button3.setOnClickListener {

            findNavController().navigate(R.id.action_trainHomeFragment_to_trainFragment)
        }
    }

}