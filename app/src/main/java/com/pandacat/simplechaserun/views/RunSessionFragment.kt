package com.pandacat.simplechaserun.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pandacat.simplechaserun.databinding.FragmentRunSessionBinding

class RunSessionFragment : Fragment() {
    private val TAG = "RunSessionFragment"
    private lateinit var binding: FragmentRunSessionBinding
    private var map: GoogleMap? = null;
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentRunSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync{
            map = it
            val options = MarkerOptions()
                .position(LatLng(39.161607200033515, -77.27608037908082))
                .title("Test Marker")
            it.addMarker(options)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(options.position, 16.0f))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun startRun()
    {

    }

    private fun stopRun()
    {

    }
}