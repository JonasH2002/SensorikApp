package de.hsas.inf.sensorikapp.ui.gyroskop

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.hsas.inf.sensorikapp.databinding.FragmentGyroskopBinding

class GyroskopFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentGyroskopBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var gravityValueText: TextView

    private lateinit var sensorManager: SensorManager
    private var gravitySensor: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val gyroskopViewModel =
            ViewModelProvider(this).get(GyroskopViewModel::class.java)

        _binding = FragmentGyroskopBinding.inflate(inflater, container, false)
        val root: View = binding.root

        gravityValueText = binding.gravityValueText

        sensorManager = requireActivity().getSystemService(SensorManager::class.java)
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        return root
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_GRAVITY) {
                if (event.values.size >= 3) {
                    val xValue = event.values[0]
                    val yValue = event.values[1]
                    val zValue = event.values[2]

                    gravityValueText.text = "X: $xValue, Y: $yValue, Z: $zValue"
                } else {
                    Log.w(
                        "SensorApp",
                        "Accelerometer event.values has unexpected size: ${event.values.size}"
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}