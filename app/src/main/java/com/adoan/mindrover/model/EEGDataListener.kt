package com.adoan.mindrover.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjoe64.graphview.series.DataPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

enum class ConnectionState {
    NO_CONNECTION, PENDING, CONNECTION
}

class EEGDataListener: ViewModel() {

    private var wholeData = arrayOf<Array<DataPoint>>()

    private val _impedances = MutableLiveData<Array<Int>>()
    val impedances: LiveData<Array<Int>> = _impedances

    val accelerations: Array<Array<Int>> = arrayOf(arrayOf(), arrayOf(), arrayOf())

    private val _connection = MutableLiveData<ConnectionState>()
    val connection: LiveData<ConnectionState> = _connection

    val numOfChannels = 6

    var time: Double = 1.0

    val MAXSTOREDPOINT = 2000

    private var checkerRunning: Boolean = false


    val _sharedFlow = MutableSharedFlow<Array<Array<DataPoint>>>()
    val sharedFlow = _sharedFlow.asSharedFlow()

    private var numOfDataArrived: Double = 0.0

    init {
        _connection.value = ConnectionState.NO_CONNECTION
    }

    fun getLatestData(ms: Int): Array<Array<Double>>? {

        if (wholeData.isEmpty() || wholeData[0].size < ms) {
            Log.e("ERROR", "wholedata is shorter than expected")
            return null
        }
        var data: Array<Array<Double>> = arrayOf()
        for (ch in 0 until numOfChannels) {
            var tmpArray: Array<Double> = arrayOf()
            for (dp :DataPoint in wholeData[ch].takeLast(ms).toTypedArray()) {
                tmpArray = tmpArray.plus(dp.y)
            }

            data = data.plus(tmpArray)
        }
        return data
    }

    fun addData(array: Array<Double>) {

        numOfDataArrived += 1

        if (!checkerRunning) {
            checkerRunning = true

            viewModelScope.launch {

                while (true) {
                    numOfDataArrived = 0.0

                    delay(1000)

//                    Log.d("Num OF DATA ARRIVED", "$numOfDataArrived")
                    if (numOfDataArrived < 100) {
                        _connection.postValue(ConnectionState.NO_CONNECTION)
                        break
                    }
                }
                checkerRunning = false
            }
        }


        var currentData = arrayOf<Array<DataPoint>>()

        val empt = wholeData.size == 0
        for (i in 0 until numOfChannels) {
            currentData = currentData.plus(arrayOf<DataPoint>())
            if (empt) {
                wholeData = wholeData.plus(arrayOf<DataPoint>())
            }
        }

        time++
        val size = minOf(1000, wholeData[0].size)

        for (ch in 0 until numOfChannels) {
            wholeData[ch] =
                wholeData[ch].plus(DataPoint(time, array[ch]))

            val data = wholeData[ch].takeLast(size).toTypedArray()
            for (i in 0 until size) {
                data[i] = DataPoint(i.toDouble(), data[i].y)
            }
            currentData[ch] = data

        }

        if (time.toInt()%100 == 0) {
            viewModelScope.launch {
                _sharedFlow.emit(currentData)
            }
        }

        //Free memory with whole data
        if (wholeData[0].size > MAXSTOREDPOINT * 2) {
            for (ch in 0 until numOfChannels) {
                wholeData[ch] = wholeData[ch].takeLast(MAXSTOREDPOINT).toTypedArray()
            }
        }

    }

    fun addImpedance(array: Array<Int>) {
//        _impedances.value = array
        Log.d("Impedance arrived", "Impedance value 1: ${array[5]}")
    }

    fun addAcceleration(x: Int, y: Int, z: Int) {
        accelerations[0] = accelerations[0].plus(x)
        accelerations[1] = accelerations[1].plus(y)
        accelerations[2] = accelerations[2].plus(z)

        accelerations[0] = accelerations[0].takeLast(MAXSTOREDPOINT).toTypedArray()
        accelerations[1] = accelerations[1].takeLast(MAXSTOREDPOINT).toTypedArray()
        accelerations[2] = accelerations[2].takeLast(MAXSTOREDPOINT).toTypedArray()

//        for (i in 0..2) {
//            if (accelerations[i].size > MAXSTOREDPOINT * 2) {
//                accelerations[i] = accelerations[i].takeLast(MAXSTOREDPOINT).toTypedArray()
//            }
//        }

    }
    fun getLatestAcceleration(ms: Int): Array<Array<Int>>? {

        if (accelerations[0].isEmpty() || accelerations[0].size < ms) {
            Log.e("ERROR", "wholedata is shorter than expected")
            return null
        }
        var data: Array<Array<Int>> = arrayOf()
        for (ch in 0 until 3) {
            data = data.plus(accelerations[ch].takeLast(ms).toTypedArray())
        }
        return data
    }

}

