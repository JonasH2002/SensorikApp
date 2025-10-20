package de.hsas.inf.sensorikapp.ui.photometer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import de.hsas.inf.sensorikapp.Const
import de.hsas.inf.sensorikapp.R
import de.hsas.inf.sensorikapp.databinding.FragmentPhotometerBinding

class PhotometerFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentPhotometerBinding? = null
    private val binding get() = _binding!!

    private lateinit var lightValueText: TextView

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private val lightData = mutableListOf<Any>()
    private lateinit var aaChartModel: AAChartModel

    private var lightThreshold = 120.0f
    private val NOTIFICATION_CHANNEL_ID = "light_sensor_channel"
    private lateinit var sharedPreferences: SharedPreferences

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

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        lightThreshold = sharedPreferences.getFloat("light_threshold", 120.0f)
        binding.thresholdEditText.setText(lightThreshold.toString())


        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        createNotificationChannel()

        aaChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title("Light Sensor Data")
            .subtitle("lux")
            .dataLabelsEnabled(false)
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Light")
                        .data(arrayOf())
                )
            )

        binding.aaChartView.aa_drawChartWithChartModel(aaChartModel)

        binding.toggleButton.check(binding.textView.id)
        binding.toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.textView.id -> {
                        binding.aaChartView.visibility = View.GONE
                        binding.lightValueText.visibility = View.VISIBLE
                        binding.thresholdInputLayout.visibility = View.VISIBLE
                        binding.saveButton.visibility = View.VISIBLE
                    }

                    binding.chartView.id -> {
                        binding.aaChartView.visibility = View.VISIBLE
                        binding.lightValueText.visibility = View.GONE
                        binding.thresholdInputLayout.visibility = View.GONE
                        binding.saveButton.visibility = View.GONE
                    }
                }
            }
        }

        binding.saveButton.setOnClickListener {
            val newThreshold = binding.thresholdEditText.text.toString().toFloatOrNull()
            if (newThreshold != null) {
                lightThreshold = newThreshold
                sharedPreferences.edit {
                    putFloat("light_threshold", lightThreshold)
                }

                Toast.makeText(requireContext(), "Threshold saved", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(requireContext(), "Invalid threshold", Toast.LENGTH_SHORT).show()
            }
        }

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

                    if (lightData.size > 50) {
                        lightData.removeAt(0)
                    }
                    lightData.add(lightValue)

                    val series = arrayOf(
                        AASeriesElement()
                            .name("Light")
                            .data(lightData.toTypedArray())
                    )
                    binding.aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(series)


                    if (lightValue > lightThreshold) {
                        sendNotification(lightValue)
                    }
                } else {
                    Log.d(Const.TAG, "Light sensor event.values is empty")
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Light Sensor"
            val descriptionText = "Notifications for light sensor threshold"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(lightValue: Float) {
        val builder = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your own icon
            .setContentTitle("Light Sensor Threshold Exceeded")
            .setContentText("Current light value: $lightValue lux")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
                return
            }
            notify(1, builder.build())
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d(Const.TAG, "Light sensor listener registered")
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        Log.d(Const.TAG, "Light sensor listener unregistered")
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}