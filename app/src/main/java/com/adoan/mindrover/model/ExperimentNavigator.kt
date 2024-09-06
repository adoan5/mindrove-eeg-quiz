package com.adoan.mindrover.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adoan.mindrover.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ExperimentCommand(val id: String, val imgId:Int, val textId:Int)

data class LabeledData(val label: String, var data: Array<Array<Double>>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LabeledData

        if (label != other.label) return false
        if (!data.contentDeepEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + data.contentDeepHashCode()
        return result
    }

    fun getFloatArrayData(): FloatArray {

        var floatArray: Array<Float> = arrayOf()

        for (ch in 0..<data.size) {
            for (time in 0..<data[0].size) {
                floatArray = floatArray.plus(data[ch][time].toFloat())
            }
        }

        return floatArray.toFloatArray()
    }

    fun getOneHatEncodedLabel(): FloatArray {
        var labelMap = mutableMapOf<String, FloatArray>("rest" to floatArrayOf(0f,1f), "clench" to floatArrayOf(1f,0f))
        return labelMap[label]!!
    }
}

fun getShiftedDataSet(labData:LabeledData, length: Int, shift: Int): MutableList<LabeledData> {

    val shiftedDataSet = mutableListOf<LabeledData>()
    for (i :Int in 0..(labData.data[0].size - length) / shift) {
        var startInd = i*shift
        var endInd = i*shift + length
        var slice = mutableListOf<Array<Double>>()
        for (j in 0 until labData.data.size) {
            val s = (labData.data[j].slice(startInd..< endInd)).toDoubleArray().toTypedArray()
            slice.add(s)
        }
        val array = slice.toTypedArray()
        shiftedDataSet.add(LabeledData(labData.label, array))

    }

    return  shiftedDataSet

}

enum class ExperimentState {
    PENDING, STARTED, FINISHED, TRAINING
}

class ExperimentNavigator(): ViewModel() {

    private val _actualCommand = MutableLiveData<ExperimentCommand>()
    val actualCommand: LiveData<ExperimentCommand> = _actualCommand

    private val _doesExperimentRun = MutableLiveData<ExperimentState>()
    val doesExperimentRun: LiveData<ExperimentState> = _doesExperimentRun

    private val _experimentRun = MutableLiveData<Int>()
    val experimentRun: LiveData<Int> = _experimentRun

    private var _labeledDataArray: Array<LabeledData> = arrayOf()
    private var _eegDataListener: EEGDataListener? = null

    var leftThreshold: Int = -2000
    var rightThreshold: Int = 5000

    init {
        _doesExperimentRun.postValue(ExperimentState.PENDING)
    }

    fun setEEGListener(eegDataListener: EEGDataListener) {
        _eegDataListener = eegDataListener
    }

    fun finishExperiment() {
        _doesExperimentRun.postValue(ExperimentState.FINISHED)
    }
    fun getLabeledDataArrays(shiftGeneration: Boolean = false): Pair<Array<FloatArray>, Array<FloatArray>>{

        var data: Array<FloatArray> = arrayOf()
        var label: Array<FloatArray> = arrayOf()

        if (shiftGeneration) {
            for (labeledData: LabeledData in _labeledDataArray) {
                for (d in getShiftedDataSet(labeledData, 500, 10)) {
                    data = data.plus(d.getFloatArrayData())
                    label = label.plus(d.getOneHatEncodedLabel())
                }
            }
        } else {
            for (labeledData: LabeledData in _labeledDataArray) {
                data = data.plus(labeledData.getFloatArrayData())
                label = label.plus(labeledData.getOneHatEncodedLabel())
            }
        }
//        (activity as MainActivity?)?.
        return Pair(data, label)
    }




    fun startExperiment(num: Int) {


        val left = ExperimentCommand("left", R.drawable.ic_arrow_circle_left, R.string.left_command)
        val right = ExperimentCommand("right", R.drawable.ic_arrow_circle_right, R.string.right_command)
        val clench = ExperimentCommand("clench", R.drawable.ic_clench, R.string.clench_command)
        val rest = ExperimentCommand("rest", R.drawable.ic_rest, R.string.rest_command)
        val action = ExperimentCommand("action", R.drawable.ic_cancel, R.string.no_command)

        val commands= mutableListOf<ExperimentCommand>()

        repeat(1) {
            commands.add(left)
            commands.add(right)
        }

        val commandList= mutableListOf<ExperimentCommand>()

        repeat(num) {
            commandList.add(clench)
            commandList.add(rest)
        }

        commandList.shuffle()

        commands.addAll(commandList)

        _doesExperimentRun.value = ExperimentState.STARTED

        viewModelScope.launch {

            for (comm in commands) {
                Log.d("Experiment", comm.id)
                _actualCommand.value = comm
                _experimentRun.value = comm.imgId
                delay(3000)
                _actualCommand.value = action
                delay(3000)
                val lastArray = _eegDataListener?.getLatestData(1000)

                if (lastArray != null && (comm.id == "rest" || comm.id == "clench")) {
                    _labeledDataArray = _labeledDataArray.plus(LabeledData(comm.id, lastArray))
                } else if (comm.id == "left") {
                    val accelerations = _eegDataListener?.getLatestAcceleration(2000)
                    if (accelerations != null) {
                        leftThreshold = ((accelerations[2]).average() * 0.75).toInt()
                        Log.d("Threshold - LEFT", "$leftThreshold")
                    }
                } else if (comm.id == "right") {
                    val accelerations = _eegDataListener?.getLatestAcceleration(2000)
                    if (accelerations != null) {
                        rightThreshold = ((accelerations[2]).average() * 0.75).toInt()
                        Log.d("Threshold - RIGHT", "$rightThreshold")
                    }
                }
            }
            _doesExperimentRun.value = ExperimentState.TRAINING
            Log.d("Experiment state", "${_doesExperimentRun.value}")
        }
    }
}