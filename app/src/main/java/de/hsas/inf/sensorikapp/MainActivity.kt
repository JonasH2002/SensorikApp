package de.hsas.inf.sensorikapp

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis



class MainActivity : ComponentActivity(), SensorEventListener {

    // Views:
    private var tvGravity:ArrayList<TextView> = ArrayList()
    private var tvAcceleration:ArrayList<TextView> = ArrayList()
    private var tvPressure:ArrayList<TextView> = ArrayList()

    // Buttons:
    private lateinit var btnGravity:Button
    private lateinit var btnAcceleration:Button
    //private lateinit var btnLight: ToggleButton
    private lateinit var btnPressure: Button

    // View Ids:
    private var idGravity:ArrayList<Int> = arrayListOf(R.id.tv_g_x,R.id.tv_g_y,R.id.tv_g_z)
    private var idAcceleration:ArrayList<Int> = arrayListOf(R.id.tv_a_x,R.id.tv_a_y,R.id.tv_a_z)
    private var idPressure:ArrayList<Int> = arrayListOf(R.id.tv_pressure)

    // Sensoren:
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorGravity: Sensor
    private lateinit var sensorAcceleration: Sensor
    private lateinit var sensorPressure: Sensor
    //private lateinit var cameraManager: CameraManager

    // SensorData
    private var gravityData: SensorData? = null
    private var accelerationData: SensorData? = null
    private var pressureData: SensorDataPressure? = null

    // Camera
    // private var getCameraID = cameraManager!!.cameraIdList[0]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        initViews()
        initSensors()
        //initLight()
        setupChart()

    }


    private fun initViews() {
        // Init TvAcceleration
        for (i in idAcceleration) {
            tvAcceleration.add(findViewById(i))
        }

        // Init TvGravity
        for (i in idGravity) {
            tvGravity.add(findViewById(i))
        }

        // Init TvPressure
        for (i in idPressure) {
            tvPressure.add(findViewById(i))
        }

        // Init lineChart
        lineChart = findViewById(R.id.lineChart)

        // Init Buttons
        btnGravity = findViewById(R.id.btn_gravity)
        btnAcceleration = findViewById(R.id.btn_acceleration)
        btnPressure = findViewById(R.id.btn_pressure)
        //btnLight = findViewById(R.id.btn_light)

        // Click Listener
        btnAcceleration.setOnClickListener {
            registerListenerAcceleration()
        }
        btnGravity.setOnClickListener {
            registerListenerGravity()
        }
        btnPressure.setOnClickListener {
            registerListenerPressure()
        }

    }

    private fun initSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)!!
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)!!
        }
    }

    private fun registerListenerGravity() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun registerListenerAcceleration() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorManager.registerListener(this,sensorAcceleration,SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun registerListenerPressure() {
        if(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            sensorManager.registerListener(this,sensorPressure,SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterListener() {
        sensorManager.unregisterListener(this,sensorGravity)
        sensorManager.unregisterListener(this,sensorAcceleration)
        sensorManager.unregisterListener(this,sensorPressure)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event!!.sensor.type == Sensor.TYPE_GRAVITY) {
            getGravityData(event)
            val gravity: ArrayList<Float> = arrayListOf(event.values[0], event.values[1], event.values[2])
            val timestampGravity = event.timestamp
            Log.d("SENSOR_DATA", "Gravity $gravity $timestampGravity")
        }
        else if(event!!.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
            getAccelerationData(event)
            val gravity: ArrayList<Float> = arrayListOf(event.values[0], event.values[1], event.values[2])
            val timestampAcceleration = event.timestamp
            Log.d("SENSOR_DATA", "Acceleration $gravity $timestampAcceleration")
        }
        else if(event!!.sensor.type == Sensor.TYPE_PRESSURE){
            getPressureData(event)
            val pressure: ArrayList<Float> = arrayListOf(event.values[0])
            val timestampPressure = event.timestamp
            Log.d("SENSOR_DATA", "Pressure $pressure $timestampPressure")
            val pressureChart = event.values[0]
            addData(pressureChart)
            if(event.values[0] >= 1050.0f) {
                Toast.makeText(this@MainActivity, "Luftdruck zu hoch!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun getGravityData(e:SensorEvent?) {
        if(gravityData == null) {
            gravityData = SensorData(e!!.values[0], e!!.values[1], e!!.values[2], e!!.timestamp)
        }
        else {
            gravityData!!.x1 = e!!.values[0]
            gravityData!!.x2 = e!!.values[1]
            gravityData!!.x3 = e!!.values[2]
        }

        tvGravity[0].text = "x: ${"%.2f".format(gravityData!!.x1)} m/s²"
        tvGravity[1].text = "y: ${"%.2f".format(gravityData!!.x2)} m/s²"
        tvGravity[2].text = "z: ${"%.2f".format(gravityData!!.x3)} m/s²"
    }

    private fun getAccelerationData(e:SensorEvent?) {
        if(accelerationData == null) {
            accelerationData = SensorData(e!!.values[0],e!!.values[1],e!!.values[2],e!!.timestamp)
        }
        else {
            accelerationData!!.x1 = e!!.values[0]
            accelerationData!!.x2 = e!!.values[1]
            accelerationData!!.x3 = e!!.values[2]
        }

        tvAcceleration[0].text = "x: ${"%.2f".format(accelerationData!!.x1)} m/s"
        tvAcceleration[1].text = "y: ${"%.2f".format(accelerationData!!.x2)} m/s"
        tvAcceleration[2].text = "z: ${"%.2f".format(accelerationData!!.x3)} m/s"
    }

    private fun getPressureData(e:SensorEvent?) {
        if(pressureData == null) {
            pressureData = SensorDataPressure(e!!.values[0], e!!.timestamp)
        }
        else {
            pressureData!!.x1 = e!!.values[0]
        }

        tvPressure[0].text = "Luftdruck ${"%.2f".format(pressureData!!.x1)} hPa"
    }

    //private fun initLight() {
    //    cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
    //}

    //fun toggleFlashlight() {
    //    if(btnLight!!.isChecked) {
    //        cameraManager!!.setTorchMode(getCameraID!!, true)
    //        Toast.makeText(this@MainActivity, "Taschenlampe ist an", Toast.LENGTH_SHORT).show()
    //    }
    //    else {
    //        cameraManager!!.setTorchMode(getCameraID, false)
    //        Toast.makeText(this@MainActivity, "Taschenlampe ist aus", Toast.LENGTH_SHORT).show()
    //    }
    //}

    // Chart
    private lateinit var lineChart: LineChart
    private val entries = ArrayList<Entry>()
    private var timeCounter = 0f

    private fun setupChart() {
        lineChart.description.text = "Sensor Data"
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisRight.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setVisibleXRangeMaximum(50f)
        lineChart.moveViewToX(timeCounter)
    }


    private fun addData(value: Float) {
        entries.add(Entry(timeCounter, value))
        timeCounter += 1f

        val dataSet = LineDataSet(entries, "Sensor Value")
        dataSet.color = Color.BLUE
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2f
        dataSet.valueTextSize = 10f


        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.setVisibleXRangeMaximum(50f)
        lineChart.moveViewToX(timeCounter)
        lineChart.invalidate() // refresh chart
    }
}
