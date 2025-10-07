package de.hsas.inf.sensorikapp.ui.photometer

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
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import de.hsas.inf.sensorikapp.databinding.FragmentPhotometerBinding

class PhotometerFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentPhotometerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var lightValueText: TextView

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val photometerViewModel =
            ViewModelProvider(this).get(PhotometerViewModel::class.java)

        _binding = FragmentPhotometerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        lightValueText = binding.lightValueText

        sensorManager = requireActivity().getSystemService(SensorManager::class.java)

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        return root
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                if (event.values.isNotEmpty()) {
                    val lightValue = event.values[0]
                    lightValueText.text = "$lightValue lux"
                } else {
                    Log.w("SensorApp", "Light sensor event.values is empty")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
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