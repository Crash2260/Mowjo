package com.example.mowjo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.location.Geocoder
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val addressContainer = findViewById<LinearLayout>(R.id.addressInputContainer)
        val editStreet = findViewById<EditText>(R.id.editStreet)
        val editCity = findViewById<EditText>(R.id.editCity)
        val editState = findViewById<EditText>(R.id.editState)
        val editZip = findViewById<EditText>(R.id.editZip)
        val btnGo = findViewById<Button>(R.id.btnGo)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnLoadLawn = findViewById<Button>(R.id.btnLoadLawn)
        val btnLoadCut = findViewById<Button>(R.id.btnLoadCut)

        btnStart.setOnClickListener {
            // Hide start/load buttons
            btnStart.visibility = View.GONE
            btnLoadLawn.visibility = View.GONE
            btnLoadCut.visibility = View.GONE

            // Show address entry form
            addressContainer.visibility = View.VISIBLE
        }

        btnBack.setOnClickListener {
            addressContainer.visibility = View.GONE
            btnStart.visibility = View.VISIBLE
            btnLoadLawn.visibility = View.VISIBLE
            btnLoadCut.visibility = View.VISIBLE
        }

        btnGo.setOnClickListener {
            val fullAddress = "${editStreet.text}, ${editCity.text}, ${editState.text} ${editZip.text}"
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(fullAddress, 1)

            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val lat = location.latitude
                val lng = location.longitude

                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("LAT", lat)
                intent.putExtra("LNG", lng)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Could not find location. Please check address.", Toast.LENGTH_SHORT).show()
            }
        }

        btnLoadLawn.setOnClickListener {
            // TODO: Load lawn data from storage
        }

        btnLoadCut.setOnClickListener {
            // TODO: Load cut path from storage
        }
    }
}
