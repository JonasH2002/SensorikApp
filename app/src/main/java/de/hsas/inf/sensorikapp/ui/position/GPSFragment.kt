package de.hsas.inf.sensorikapp.ui.position

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import de.hsas.inf.sensorikapp.Const
import de.hsas.inf.sensorikapp.databinding.FragmentGpsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GPSFragment : Fragment() {

    private var _binding: FragmentGpsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val locationData = mutableListOf<Any>()
    private lateinit var aaChartModel: AAChartModel

    private val requestPermissionLauncher = 
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                // Handle the case where the user denies the permission
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {

                    binding.locationText.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                    val date = Date(location.time)
                    val formattedDate = sdf.format(date)
                    binding.locationTimeText.text = "Last Location Timestamp: $formattedDate"

                    if (locationData.size > 50) {
                        locationData.removeAt(0)
                    }
                    locationData.add(arrayOf(location.longitude, location.latitude))

                    val series = arrayOf(
                        AASeriesElement()
                            .name("Location")
                            .data(locationData.toTypedArray())
                    )
                    binding.aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(series)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val GPSViewModel =
            ViewModelProvider(this).get(GPSViewModel::class.java)

        _binding = FragmentGpsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        aaChartModel = AAChartModel()
            .chartType(AAChartType.Scatter)
            .title("GPS Location")
            .subtitle("Latitude and Longitude")
            .dataLabelsEnabled(false)
            .series(arrayOf(
                AASeriesElement()
                    .name("Location")
                    .data(arrayOf())
            ))

        binding.aaChartView.aa_drawChartWithChartModel(aaChartModel)


        binding.toggleButton.check(binding.textView.id)
        binding.toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.textView.id -> {
                        binding.aaChartView.visibility = View.GONE
                        binding.locationText.visibility = View.VISIBLE
                        binding.locationTimeText.visibility = View.VISIBLE
                    }

                    binding.chartView.id -> {
                        binding.aaChartView.visibility = View.VISIBLE
                        binding.locationText.visibility = View.GONE
                        binding.locationTimeText.visibility = View.GONE
                    }
                }
            }
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(10000).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            setMinUpdateIntervalMillis(5000)
        }.build()

        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(Const.TAG, "Location updates stopped")
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}