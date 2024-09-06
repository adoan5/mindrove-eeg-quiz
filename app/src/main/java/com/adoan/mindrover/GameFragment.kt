package com.adoan.mindrover

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.adoan.mindrover.databinding.FragmentGameBinding
import com.adoan.mindrover.model.EEGDataListener
import com.adoan.mindrover.model.GameCommand
import com.adoan.mindrover.model.GameManager
import com.adoan.mindrover.model.Question


class GameFragment : Fragment() {

    private val gameManager: GameManager by activityViewModels()
    private val eegDataListener: EEGDataListener by activityViewModels()

    private lateinit var binding: FragmentGameBinding

    private var experimentRuns = false
    private var radioGroupArray = arrayOf<Int>()
    private var choiceNum = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameBinding.inflate(inflater)

        binding.radioGroup.check(binding.radioButton5.id)
        radioGroupArray = arrayOf(binding.radioButton3.id, binding.radioButton4.id,
            binding.radioButton5.id, binding.radioButton6.id)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameManager.setMainActivity(mainActivity = (activity as MainActivity))
        experimentRuns = true

        val commandObserver = Observer<GameCommand> { state ->
            if (experimentRuns) {
                if (state == GameCommand.GO_UP) {
                    choiceNum += 1
                    if (choiceNum > 3) {
                        choiceNum = 0
                    }
                } else if (state == GameCommand.GO_DOWN) {
                    choiceNum -= 1
                    if (choiceNum < 0) {
                        choiceNum = 3
                    }
                } else if (state == GameCommand.CHOOSE) {
                    gameManager.answerQuestion(choiceNum)
                } else if (state == GameCommand.PAUSE) {
                    var color = Color.RED
                    if (gameManager.correctAnswer)
                        color = Color.GREEN

                    when (binding.radioGroup.checkedRadioButtonId) {
                        binding.radioButton3.id -> binding.radioButton3.setTextColor(color)
                        binding.radioButton4.id -> binding.radioButton4.setTextColor(color)
                        binding.radioButton5.id -> binding.radioButton5.setTextColor(color)
                        binding.radioButton6.id -> binding.radioButton6.setTextColor(color)
                    }

                } else if (state == GameCommand.REST) {

                    var color = Color.WHITE
                    val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    val isDarkModeOn = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

                    if (!isDarkModeOn)
                        color = Color.BLACK

                    binding.radioButton3.setTextColor(color)
                    binding.radioButton4.setTextColor(color)
                    binding.radioButton5.setTextColor(color)
                    binding.radioButton6.setTextColor(color)

                }

                binding.radioGroup.check(radioGroupArray[choiceNum])

                Log.d("Observer", "Observed:${state.name}")
            }
        }
        gameManager.actualCommand.observe(viewLifecycleOwner, commandObserver)

        val questionObserver = Observer<Question> { state ->
            binding.textView2.setText(state.question)
            binding.radioButton3.setText(state.answers[0])
            binding.radioButton4.setText(state.answers[1])
            binding.radioButton5.setText(state.answers[2])
            binding.radioButton6.setText(state.answers[3])
        }

        gameManager.actualQuestion.observe(viewLifecycleOwner, questionObserver)

        val pointObserver = Observer<Int> { state ->
            binding.points.setText("Points: ${state}")
        }

        gameManager.points.observe(viewLifecycleOwner, pointObserver)

        gameManager.setEEGDataListener(eegDataListener)
        gameManager.startGame(10)

        val runningObserver = Observer<Boolean> { state ->
            if (!state) {
                Toast.makeText((activity as MainActivity?)!!,"Your score is ${gameManager.points.value} points!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_gameFragment_to_gameStarterFragment)
            }
        }

        gameManager.running.observe(viewLifecycleOwner, runningObserver)

    }

}