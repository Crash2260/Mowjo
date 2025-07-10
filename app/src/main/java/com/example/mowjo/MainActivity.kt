package com.example.mowjo  // Your actual package name here

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import android.graphics.Color

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val lawnPoints = mutableListOf<LatLng>()
    private var lawnPolygon: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Switch to satellite image
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

        // Example marker (SF) — change to your house coords later
        val home = LatLng(32.1777, -81.3349)
        mMap.addMarker(MarkerOptions().position(home).title("My Lawn"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 20f))

        mMap.setOnMapClickListener { latLng ->
            lawnPoints.add(latLng)

            // Optional: show a small marker at each point
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
        // Remove old polygon if it exists
        lawnPolygon?.remove()

        if (lawnPoints.size < 3) return // Need at least 3 points to make a polygon

        val polygonOptions = PolygonOptions()
            .addAll(lawnPoints)
            .strokeColor(Color.GREEN)
            .strokeWidth(4f)
            .fillColor(Color.argb(50, 0, 255, 0)) // Transparent green fill

        lawnPolygon = mMap.addPolygon(polygonOptions)

    // TODO: Add undo button to remove last placed point
    // TODO: Add reset button to clear all lawn points
    // TODO: Add saving/loading of lawn outlines
    // TODO: Add UI for entering address or jumping to saved location

    }
}
