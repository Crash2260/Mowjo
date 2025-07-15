package com.example.mowjo

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var userLocation: LatLng? = null
    private val lawnPoints = mutableListOf<LatLng>()
    private var lawnPolygon: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Get lat/lng from intent
        val lat = intent.getDoubleExtra("LAT", Double.NaN)
        val lng = intent.getDoubleExtra("LNG", Double.NaN)

        userLocation = if (!lat.isNaN() && !lng.isNaN()) {
            LatLng(lat, lng)
        } else {
            Toast.makeText(this, "No location received.", Toast.LENGTH_SHORT).show()
            null
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable satellite view and UI controls
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // Center map
        val target = userLocation ?: LatLng(27.3805, 33.6318) // fallback location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 20f))

        // Allow user to place lawn points
        mMap.setOnMapClickListener { latLng ->
            lawnPoints.add(latLng)

            mMap.addCircle(
                CircleOptions()
                    .center(latLng)
                    .radius(0.5) // meters
                    .strokeColor(Color.GREEN)
                    .fillColor(Color.argb(100, 0, 255, 0))
                    .strokeWidth(2f)
            )

            drawLawnPolygon()
        }
    }

    private fun drawLawnPolygon() {
        lawnPolygon?.remove()

        if (lawnPoints.size < 3) return

        val polygonOptions = PolygonOptions()
            .addAll(lawnPoints)
            .strokeColor(Color.GREEN)
            .strokeWidth(4f)
            .fillColor(Color.argb(50, 0, 255, 0))

        lawnPolygon = mMap.addPolygon(polygonOptions)

        // TODO: Add undo button to remove last placed point
        // TODO: Add reset button to clear all lawn points
        // TODO: Add saving/loading of lawn outlines
        // TODO: Add UI for entering address or jumping to saved location
    }
}
