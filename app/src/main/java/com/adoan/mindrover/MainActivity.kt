package com.adoan.mindrover

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.adoan.mindrover.databinding.ActivityMainBinding
import com.adoan.mindrover.model.ConnectionState
import com.adoan.mindrover.model.EEGDataListener
import com.adoan.mindrover.model.ExperimentNavigator
import com.adoan.mindrover.model.ExperimentState
import com.adoan.mindrover.model.GameManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mylibrary.mindrove.SensorData
import mylibrary.mindrove.ServerManager
import org.tensorflow.lite.Interpreter
import uk.me.berndporr.iirj.Butterworth
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.channels.FileChannel
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var interpreter: Interpreter

    private var interpreterInitialized = false

    private var networkIsTraining = false

    val NUM_EPOCHS = 5
    val BATCH_SIZE = 10
    val IMG_HEIGHT = 6
    val IMG_WIDTH = 500
    //        val IMG_HEIGHT = 28
//        val IMG_WIDTH = 28
    val NUM_OF_CLASSES = 2

    private var averages = arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
//    var deviations = arrayOf(10.0, 10.0, 10.0, 10.0, 10.0, 10.0)
    var numOfSamples: Double = 0.0


    private val experimentNavigator: ExperimentNavigator by viewModels()
    private val gameManager: GameManager by viewModels()
    private val eegListener: EEGDataListener by viewModels()

    var _filters: Array<Butterworth> = arrayOf()
    private var doFrequencyFilter: Boolean = true

    private val serverManager = ServerManager { sensorData: SensorData ->
// Update the sensor data text
//        sensorDataText.postValue(sensorData.accelerationX.toString())
        if (isEEGMode) {
            if (doFrequencyFilter) {
                val filteredData: Array<Double> = arrayOf(_filters[0].filter(sensorData.channel1),
                                                          _filters[1].filter(sensorData.channel2),
                                                          _filters[2].filter(sensorData.channel3),
                                                          _filters[3].filter(sensorData.channel4),
                                                          _filters[4].filter(sensorData.channel5),
                                                          _filters[5].filter(sensorData.channel6))



                for (i in 0..5) {
                    averages[i] = (averages[i] * numOfSamples + abs(filteredData[i])) / (numOfSamples + 1)
                }
                numOfSamples += 1

                eegListener.addData(
                    arrayOf(
                        filteredData[0]/averages[0], filteredData[1]/averages[1],
                        filteredData[2]/averages[2], filteredData[3]/averages[3],
                        filteredData[4]/averages[4], filteredData[5]/averages[5],
                        sensorData.channel7, sensorData.channel8
                    )
                )

            } else {
                eegListener.addData(
                    arrayOf(
                        sensorData.channel1, sensorData.channel2,
                        sensorData.channel3, sensorData.channel4, sensorData.channel5,
                        sensorData.channel6, sensorData.channel7, sensorData.channel8
                    )
                )
            }
            eegListener.addAcceleration(sensorData.accelerationX, sensorData.accelerationY, sensorData.accelerationZ)

        } else {
            eegListener.addImpedance(
                arrayOf(
                    sensorData.impedance1To2,
                    sensorData.impedance2To3, sensorData.impedance3To4, sensorData.impedance5To4,
                    sensorData.impedance5To6, sensorData.impedance1ToDRL, sensorData.impedance3ToDRL,
                    sensorData.impedance6ToRef, sensorData.impedanceRefTo4, sensorData.impedanceRefToDRL
                )
            )
        }
//        Log.d("Connection success", "Data recieved${sensorData.channel6} - ${sensorData.channel7} - ${sensorData.channel8}")
    }

    private var isServerManagerStarted = false
    private var isEEGMode = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupWithNavController(bottomNavigationView, navController)
        verifyStoragePermissions(this)


        for (i in 1..6) {
            val filter = Butterworth()
            filter.highPass(3,500.0, 1.0)
//            filter.bandPass(3,500.0, 126.0, 125.0)
            _filters = _filters.plus(filter)
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            if(destination.id == R.id.trainFragment || destination.id == R.id.connectionFragment
                || destination.id == R.id.trainingNetworkFragment || destination.id == R.id.gameFragment) {
                bottomNavigationView.visibility = View.GONE
            } else {
                bottomNavigationView.visibility = View.VISIBLE
            }
            if (destination.id == R.id.impedanceCheckerFragment) {
//                serverManager.stop()
                lifecycleScope.launch {
//                    isEEGMode = false
//                    serverManager.stop()
//                    serverManager.start()

//                    serverManager.sendInstruction(Instruction.IMP)

//                    while (!serverManager.isMessageReceived) {
//                        delay(300L)
//                        Log.d("Message NOT recieved", "EEGMode continue")
//                    }
//                    Log.d("Message recieved", "EEGMode off")
                }
//                serverManager.start()
            } else if(!isEEGMode) {
                lifecycleScope.launch {
//                    serverManager.start()

//                    serverManager.sendInstruction(Instruction.EEG)

//                    if (serverManager.isMessageReceived) {
//                        isEEGMode = true
//                    }
                }
            }
        }
//        if (savedInstanceState != null) {
//            startConnection()
//        }

//        loadNeuralNetwork()


        val nameObserver = Observer<ConnectionState> { state ->
            if (state == ConnectionState.NO_CONNECTION) {
                navController.navigate(R.id.connectionFragment)
                gameManager.cancelGame()
                experimentNavigator.finishExperiment()
            }
        }

        eegListener.connection.observe(this, nameObserver)


     }
    override fun onDestroy() {
        super.onDestroy()
        // Stop the server when the activity is destroyed
        serverManager.stop()
    }


    override fun onBackPressed() {

        if (navController.currentDestination?.id == R.id.trainingNetworkFragment) {
            navController.navigate(R.id.trainHomeFragment)
            experimentNavigator.finishExperiment()
        } else if (navController.currentDestination?.id == R.id.gameFragment) {
            navController.navigate(R.id.action_gameFragment_to_gameStarterFragment)
            gameManager.cancelGame()
        } else if (navController.currentDestination?.id == R.id.gameStarterFragment) {
            navController.navigate(R.id.connectionFragment)
        } else if (navController.currentDestination?.id == R.id.connectionFragment) {
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!interpreterInitialized) {
            loadNeuralNetwork()
        }
    }

    // Function to check network connectivity
    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    fun startConnection() {
        if (!isServerManagerStarted) {
            serverManager.start()
            isServerManagerStarted = true
            Log.d("ServerManager", "ServerManager started")
        }
    }

    var isWifiSettingsOpen = false
    private val wifiSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
// This block is executed when the Wi-Fi settings activity is finished
            isWifiSettingsOpen = false
        }

    // Function to open Wi-Fi settings
    private fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        wifiSettingsLauncher.launch(intent)
    }

    private fun loadNeuralNetwork() {

        interpreterInitialized = true

        lifecycleScope.launch(Dispatchers.IO) {
            interpreter = Interpreter(loadModelFile())
        }

    }

    private fun createFloatBuffer(inputArray: Array<FloatArray>?, size: Int): FloatBuffer {

//        val row = inputArray?.size
//        val col = inputArray[0].size
//        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(((4*row*col)))
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(((4*size)))

        byteBuffer.order(ByteOrder.nativeOrder())
        val floatBuffer: FloatBuffer = byteBuffer.asFloatBuffer();

        inputArray?.let {
            for (i in inputArray.indices) {
                for (j in inputArray[i].indices) {
                    floatBuffer.put(inputArray[i][j])
                }
            }
        }

        return floatBuffer

    }

    fun generateTrainData(x: Int, y: Int, label: Int): Pair<Array<FloatArray>, Array<FloatArray>> {

        var array: Array<FloatArray> = arrayOf()
        var labelarray: Array<FloatArray> = arrayOf()

        for (n in 1..100) {
            var labelrow = arrayOf<Float>()
            var arrayrow = arrayOf<Float>()
            for (i in 0..1) {
                labelrow = if (i == label) {
                    labelrow.plus(1.0f)
                } else {
                    labelrow.plus(0.0f)
                }
            }
            for (i in 1..x) {
                for (j in 1..y) {
                    arrayrow.plus((1+label.toFloat()) * Random.nextFloat())
                }
            }

            array = array.plus(arrayrow.toFloatArray())
            labelarray.plus(labelrow.toFloatArray())
        }

        return Pair(array, labelarray)
    }


    fun trainModel(trainData: Array<FloatArray>, trainLabel: Array<FloatArray>) {

        networkIsTraining = true

        val NUM_TRAININGS = 1
//        val NUM_BATCHES = NUM_TRAININGS / BATCH_SIZE
        val NUM_BATCHES = trainData.size * NUM_TRAININGS / BATCH_SIZE

        val trainImageBatches: MutableList<FloatBuffer> = ArrayList(NUM_BATCHES)
        val trainLabelBatches: MutableList<FloatBuffer> = ArrayList(NUM_BATCHES)

        // Prepare training batches.

        // Prepare training batches.
        for (i in 0 until NUM_BATCHES) {

            // Fill the data values...

            val startIndex = (i * BATCH_SIZE) % trainData.size
            val endIndex = ((i+1) * BATCH_SIZE) % trainData.size

            val trainLabels = createFloatBuffer(trainLabel.sliceArray(startIndex..< endIndex), BATCH_SIZE*NUM_OF_CLASSES)
            val trainImages = createFloatBuffer(trainData.sliceArray(startIndex..< endIndex), BATCH_SIZE * IMG_HEIGHT * IMG_WIDTH)

            trainImageBatches.add(trainImages.rewind().mark() as FloatBuffer)
            trainLabelBatches.add(trainLabels.rewind().mark() as FloatBuffer)
        }

        // Run training for a few steps.

        // Run training for a few steps.
        val losses = FloatArray(NUM_EPOCHS)
        for (epoch in 0 until NUM_EPOCHS) {
            for (batchIdx in 0 until NUM_BATCHES) {
                val inputs: MutableMap<String, Any> = HashMap()
                inputs["x"] = trainImageBatches[batchIdx]
                inputs["y"] = trainLabelBatches[batchIdx]
                val outputs: MutableMap<String, Any> = HashMap()
                val loss = FloatBuffer.allocate(1)
                outputs["loss"] = loss
                interpreter.runSignature(inputs, outputs, "train")

                // Record the last loss.
                if (batchIdx == NUM_BATCHES - 1) losses[epoch] = loss[0]
                // Print the loss output for every 10 epochs.

            }

//            if ((epoch + 1) % 2 == 0) {
            println(
                "Finished " + (epoch + 1) + " epochs, current loss: " + losses[epoch]
            )
//            }

            if (experimentNavigator.doesExperimentRun.value == ExperimentState.FINISHED) {
                break
            }

        }
        networkIsTraining = false

        experimentNavigator.finishExperiment()

    }

    fun predict(data: Array<FloatArray>): Array<Float> {

        var NUM_TESTS = 1//data.size
        val testImages: FloatBuffer = createFloatBuffer(data, NUM_TESTS*IMG_HEIGHT*IMG_WIDTH)
//            FloatBuffer.allocateDirect(NUM_TESTS * 28 * 28).order(ByteOrder.nativeOrder())
        val output: FloatBuffer = createFloatBuffer(null, NUM_TESTS*NUM_OF_CLASSES)
//            FloatBuffer.allocateDirect(NUM_TESTS * 10).order(ByteOrder.nativeOrder())

        // Run the inference.
        // Run the inference.
        val inputs: MutableMap<String, Any> = HashMap()
        inputs["x"] = testImages.rewind()
        val outputs: MutableMap<String, Any> = HashMap()
        outputs["output"] = output
        interpreter.runSignature(inputs, outputs, "infer")
        output.rewind()

        // Process the result to get the final category values.
//        for (o in output.array()) {
//            print(o)
//        }
        var outputarray = arrayOf<Float>()

        for (j in 0 .. 1) {
            outputarray = outputarray.plus(output.get(j))
        }

//        print("Classification result ${testLabels[0]}")
//        Log.d("Classsification succed", "Classification result ${testLabels[0]}")
        for (output in outputarray) {
            Log.d("Classsification succed", "Classification result ${output}")
        }

        return outputarray
    }

    fun saveTrainedFile() {
        // Conduct the training jobs.
        // Export the trained weights as a checkpoint file.

        val outputFile = File(filesDir, "checkpoint.ckpt")
        val inputs: MutableMap<String, Any> = HashMap()
        inputs["checkpoint_path"] = outputFile.absolutePath
        val outputs: Map<String, Any> = HashMap()
        interpreter.runSignature(inputs, outputs, "save")

    }

    fun loadTrainedFile() {

        // Load the trained weights from the checkpoint file.
        val outputFile = File(filesDir, "checkpoint.ckpt")
        val inputs: MutableMap<String, Any> = HashMap()
        inputs["checkpoint_path"] = outputFile.absolutePath
        val outputs: Map<String, Any> = HashMap()
        interpreter.runSignature(inputs, outputs, "restore")

    }


    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = assets.openFd("shallow_convnet_model_normalized.tflite")
        val fileInputStream = assetFileDescriptor.createInputStream()
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength).apply {
            order(ByteOrder.nativeOrder())
        }
    }
}



// Storage Permissions
private const val REQUEST_EXTERNAL_STORAGE = 1
private val PERMISSIONS_STORAGE = arrayOf<String>(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

/**
 * Checks if the app has permission to write to device storage
 *
 * If the app does not has permission then the user will be prompted to grant permissions
 *
 * @param activity
 */
fun verifyStoragePermissions(activity: Activity?) {
    // Check if we have write permission
    val permission =
        ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    Log.d("Mainactivity access", "Hello ${permission}")
    if (permission != PackageManager.PERMISSION_GRANTED) {
        // We don't have permission so prompt the user
        ActivityCompat.requestPermissions(
            activity,
            PERMISSIONS_STORAGE,
            REQUEST_EXTERNAL_STORAGE
        )
    }
}