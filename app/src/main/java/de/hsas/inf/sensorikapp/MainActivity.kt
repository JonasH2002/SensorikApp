package de.hsas.inf.sensorikapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    private lateinit var lightValueText: TextView
    private lateinit var gravityValueText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        lightValueText = findViewById(R.id.lightValueText)
        gravityValueText = findViewById(R.id.gravityValueText)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    // Inside MainActivity.kt
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            // Check the sensor type if you're listening to multiple sensors
            if (event.sensor.type == Sensor.TYPE_GRAVITY) {
                if (event.values.size >= 3) { // Accelerometer should have at least 3 values
                    val xValue = event.values[0]
                    val yValue = event.values[1] // This is now safe
                    val zValue = event.values[2]
                    // Use xValue, yValue, zValue

                    gravityValueText.text = "X: $xValue, Y: $yValue, Z: $zValue"
                } else {
                    Log.w(
                        "SensorApp",
                        "Accelerometer event.values has unexpected size: ${event.values.size}"
                    )
                }
            } else if (event.sensor.type == Sensor.TYPE_LIGHT) {
                if (event.values.isNotEmpty()) { // Light sensor should have at least 1 value
                    val lightValue = event.values[0] // This is safe
                    // Use lightValue
                    lightValueText.text = "$lightValue lux"
                } else {
                    Log.w("SensorApp", "Light sensor event.values is empty")
                }
            }
            // Add similar checks for other sensor types you are using
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}