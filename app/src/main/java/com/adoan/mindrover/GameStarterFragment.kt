package com.adoan.mindrover

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.adoan.mindrover.databinding.FragmentGameStarterBinding
import com.adoan.mindrover.model.GameManager


class GameStarterFragment : Fragment() {

    private lateinit var binding: FragmentGameStarterBinding
    private val gameManager: GameManager by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameStarterBinding.inflate(inflater)


        if (gameManager.points.value != -1) {
            binding.resultText.text = buildString {
                append("Your previous score was ${gameManager.points.value}.")
            }

            var color = Color.WHITE
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isDarkModeOn = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

            if (!isDarkModeOn)
                color = Color.BLUE


            binding.resultText.setTextColor(color)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button5.setOnClickListener {
            findNavController().navigate(R.id.action_gameStarterFragment_to_gameFragment)
        }
    }
}