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
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import de.hsas.inf.sensorikapp.Const
import de.hsas.inf.sensorikapp.databinding.FragmentGyroskopBinding

class GyroskopFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentGyroskopBinding? = null
    private val binding get() = _binding!!

    private lateinit var gyroskopValueTextView: TextView

    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null

    private val xData = mutableListOf<Any>()
    private val yData = mutableListOf<Any>()
    private val zData = mutableListOf<Any>()
    private lateinit var aaChartModel: AAChartModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val gyroskopViewModel =
            ViewModelProvider(this).get(GyroskopViewModel::class.java)

        _binding = FragmentGyroskopBinding.inflate(inflater, container, false)
        val root: View = binding.root

        gyroskopValueTextView = binding.gyroskopValueTextView

        sensorManager = requireActivity().getSystemService(SensorManager::class.java)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        aaChartModel = AAChartModel()
            .chartType(AAChartType.Line)
            .title("Gyroscope Data")
            .subtitle("X, Y, and Z axes")
            .dataLabelsEnabled(false)
            .series(arrayOf(
                AASeriesElement().name("X").data(arrayOf()),
                AASeriesElement().name("Y").data(arrayOf()),
                AASeriesElement().name("Z").data(arrayOf())
            ))

        binding.aaChartView.aa_drawChartWithChartModel(aaChartModel)

        binding.toggleButton.check(binding.textView.id)
        binding.toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.textView.id -> {
                        // Handle selection of first button
                        binding.aaChartView.visibility = View.GONE
                        binding.gyroskopValueTextView.visibility = View.VISIBLE
                    }

                    binding.chartView.id -> {
                        // Handle selection of second button
                        binding.aaChartView.visibility = View.VISIBLE
                        binding.gyroskopValueTextView.visibility = View.GONE
                    }
                }
            }
        }

        return root
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                if (event.values.size >= 3) {
                    val xValue = event.values[0]
                    val yValue = event.values[1]
                    val zValue = event.values[2]

                    gyroskopValueTextView.text = "X: $xValue, Y: $yValue, Z: $zValue"

                    if (xData.size > 50) {
                        xData.removeAt(0)
                        yData.removeAt(0)
                        zData.removeAt(0)
                    }
                    xData.add(xValue)
                    yData.add(yValue)
                    zData.add(zValue)

                    val series = arrayOf(
                        AASeriesElement().name("X").data(xData.toTypedArray()),
                        AASeriesElement().name("Y").data(yData.toTypedArray()),
                        AASeriesElement().name("Z").data(zData.toTypedArray())
                    )
                    binding.aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(series)
                } else {
                    Log.d(
                        Const.TAG,
                        "Gyroskop event.values has unexpected size: ${event.values.size}"
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(Const.TAG, "Gyroskop listener registered")
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        Log.d(Const.TAG, "Gyroskop listener unregistered")
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}