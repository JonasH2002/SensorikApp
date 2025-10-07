package de.hsas.inf.sensorikapp.ui.position

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import de.hsas.inf.sensorikapp.databinding.FragmentGpsBinding

class GPSFragment : Fragment() {

    private var _binding: FragmentGpsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var chart: AAChartView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var categories: Array<String>
    private lateinit var elements: Array<AASeriesElement>

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
                    // Update UI with location data
                    // ...
                    categories = arrayOf<String>().plus(location.longitude.toString())

                    elements = arrayOf<AASeriesElement>().plus(AASeriesElement().name("Latitude").data(arrayOf(location.latitude)))

                    val aaChartModel: AAChartModel = AAChartModel()
                        .chartType(AAChartType.Line)
                        .title("title")
                        .subtitle("subtitle")
                        .backgroundColor("#a379e8")
                        .categories(categories)
                        .dataLabelsEnabled(true)
                        .series(arrayOf(elements))

                    chart.aa_refreshChartWithChartModel(aaChartModel)
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

        chart = binding.chart

        categories = arrayOf("0")

        elements = arrayOf(AASeriesElement()
            .name("GPS")
            .data(arrayOf(0)))

        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Line)
            .title("title")
            .subtitle("subtitle")
            .backgroundColor("#a379e8")
            .categories(categories)
            .dataLabelsEnabled(true)
            .series(arrayOf(elements))

        chart.aa_drawChartWithChartModel(aaChartModel)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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