package com.example.mowjo

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var userLocation: LatLng? = null
    // For drawing lawn shapes
    private val lawnPoints = mutableListOf<LatLng>()
    private var lawnPolygon: Polygon? = null
    private val savedLawnPolygons = mutableListOf<Polygon>()
    private val lawnCircles = mutableListOf<Circle>()
    // For drawing obstacle shapes
    private val obstaclePoints = mutableListOf<LatLng>()
    private var obstaclePolygon: Polygon? = null
    private val savedObstaclePolygons = mutableListOf<Polygon>()


    // Drawing modes
    enum class DrawingMode {
        LAWN,
        OBSTACLE
    }

    // Track current mode
    private var currentMode = DrawingMode.LAWN


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

        // Undo Button
        val btnUndo = findViewById<Button>(R.id.btnUndo)
        btnUndo.setOnClickListener{
            undoLastAction()
        }

        // Toggle button
        val btnModeToggle = findViewById<Button>(R.id.btnModeToggle)
        btnModeToggle.setOnClickListener {
            toggleDrawingMode(btnModeToggle)
        }
            // Set initial mode appearance
        btnModeToggle.text = "Mode: Lawn"
        btnModeToggle.setBackgroundColor(Color.parseColor("#4CAF50")) // Green for lawn

        // Save Button
        val btnSaveShape = findViewById<Button>(R.id.btnSaveShape)
        btnSaveShape.setOnClickListener {
            saveCurrentShape()
        }
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
            if (currentMode == DrawingMode.LAWN) {
                lawnPoints.add(latLng)

                val circle = mMap.addCircle(
                    CircleOptions()
                        .center(latLng)
                        .radius(0.5)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(100, 0, 255, 0))
                        .strokeWidth(2f)
                )
                lawnCircles.add(circle)

                drawLawnPolygon()
            } else {
                obstaclePoints.add(latLng)

                val circle = mMap.addCircle(
                    CircleOptions()
                        .center(latLng)
                        .radius(0.5)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(100, 255, 0, 0))
                        .strokeWidth(2f)
                )
                lawnCircles.add(circle) // reuse circle list for now

                drawObstaclePolygon()
            }
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
    }

    private fun drawObstaclePolygon() {
        obstaclePolygon?.remove()

        if (obstaclePoints.size < 3) return

        val polygonOptions = PolygonOptions()
            .addAll(obstaclePoints)
            .strokeColor(Color.RED)
            .strokeWidth(4f)
            .fillColor(Color.argb(50, 255, 0, 0))

        obstaclePolygon = mMap.addPolygon(polygonOptions)
    }


    //Function for Undo button
    private fun undoLastAction() {
        if (lawnPoints.isNotEmpty()) {
            lawnPoints.removeAt(lawnPoints.size-1)

            // Remove last visual circle if applicable
            if (lawnCircles.isNotEmpty()) {
                lawnCircles.removeAt(lawnCircles.size-1).remove()
            }

            drawLawnPolygon()
        } else {
            Toast.makeText(this, "No points to undo.", Toast.LENGTH_SHORT).show()
        }
    }

    //Function for mode toggle
    private fun toggleDrawingMode(button: Button) {

        // Check for unsaved shapes
        if (lawnPoints.isNotEmpty() || obstaclePoints.isNotEmpty()) {
            Toast.makeText(this, "Please finish and save your current shape before switching modes.", Toast.LENGTH_SHORT).show()
            return
        }

        // Safe to switch
        currentMode = when (currentMode) {
            DrawingMode.LAWN -> {
                button.text = "Mode: Obstacle"
                button.setBackgroundColor(Color.parseColor("#FFA500")) // Orange for obstacle
                DrawingMode.OBSTACLE
            }
            DrawingMode.OBSTACLE -> {
                button.text = "Mode: Lawn"
                button.setBackgroundColor(Color.parseColor("#4CAF50")) // Green for lawn
                DrawingMode.LAWN
            }
        }
    }

    //Function for Save button
    private fun saveCurrentShape() {
        if (currentMode == DrawingMode.LAWN) {
            if (lawnPoints.size >= 3) {
                val savedPolygon = mMap.addPolygon(
                    PolygonOptions()
                        .addAll(lawnPoints)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(50, 0, 255, 0))
                        .strokeWidth(4f)
                )
                savedLawnPolygons.add(savedPolygon)

                // Remove all point markers
                for (circle in lawnCircles) {
                    circle.remove()
                }
                lawnCircles.clear()

                // Reset current shape
                lawnPoints.clear()
                lawnPolygon?.remove()
                lawnPolygon = null

            } else {
                Toast.makeText(this, "Need at least 3 points to save a lawn.", Toast.LENGTH_SHORT).show()
            }
        } else if (currentMode == DrawingMode.OBSTACLE) {
            if (obstaclePoints.size >= 3) {
                val savedObstacle = mMap.addPolygon(
                    PolygonOptions()
                        .addAll(obstaclePoints)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(50, 255, 0, 0))
                        .strokeWidth(4f)
                )
                savedObstaclePolygons.add(savedObstacle)

                // Remove point markers
                for (circle in lawnCircles) {
                    circle.remove()
                }
                lawnCircles.clear()

                // Reset current shape
                obstaclePoints.clear()
                obstaclePolygon?.remove()
                obstaclePolygon = null

            } else {
                Toast.makeText(this, "Need at least 3 points to save an obstacle.", Toast.LENGTH_SHORT).show()
            }
        }

        // TODO: On save, check if the new shape overlaps with existing ones of the same type (lawn or obstacle)
        // If so, merge them into a single polygon using a geometry union operation (e.g., JTS via Maps Utils library)

    }

}
