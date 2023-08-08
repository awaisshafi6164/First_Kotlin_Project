package com.example.first_kotlin_project

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.first_kotlin_project.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var geocoder: Geocoder

    private lateinit var database:DatabaseReference

    private var count:Int = 1
    lateinit var mapFragment:SupportMapFragment
    lateinit var googleMap:GoogleMap

    private lateinit var latET: EditText
    private lateinit var longET: EditText

    private lateinit var areaET: EditText
    private lateinit var showButton: Button

    private lateinit var textTV: TextView
    private var isBold = false
    private var isItalic = false
    private var isUnderlined = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@OnMapReadyCallback
            }
            googleMap.isMyLocationEnabled = true

            val officeLocation = LatLng(33.66263618397245, 73.05473119595356)
            googleMap.addMarker(MarkerOptions().position(officeLocation).title("RWR Office"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(officeLocation,12f))
        })


        geocoder = Geocoder(this, Locale.getDefault())

        latET = findViewById(R.id.latitudeET)
        longET = findViewById(R.id.longitudeET)
        val submitBTN = findViewById<Button>(R.id.submitBTN)
        submitBTN.setOnClickListener { submitFunc() }

        //Code Decoration Code
        textTV = findViewById(R.id.textTV)
        textTV.setTypeface(null, Typeface.NORMAL) // Initialize the typeface to normal

        val boldBTN = findViewById<Button>(R.id.boldBTN)
        val italicBTN = findViewById<Button>(R.id.italicBTN)
        val underlineBTN = findViewById<ImageButton>(R.id.underlineBTN)

        boldBTN.setOnClickListener { boldFunc() }
        italicBTN.setOnClickListener { italicFunc() }
        underlineBTN.setOnClickListener { underlineFunc() }

        database = FirebaseDatabase.getInstance().getReference("Maps")
        var entryCount: Int = 0
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                entryCount = dataSnapshot.childrenCount.toInt()
                count = entryCount+1
                println("Number of entries in Realtime Database: $entryCount")

                for(i in 1 until (entryCount+1)){
                    database.child("Location "+i).get().addOnSuccessListener {
                        if(it.exists()){
                            val latDatabase=it.child("latitude").value.toString().toDouble()
                            val longDatabase=it.child("longitude").value.toString().toDouble()

                            val databaseLocation = LatLng(latDatabase, longDatabase)
                            googleMap.addMarker(MarkerOptions().position(databaseLocation).title("Location "+i))

                            //
                            //
                            //
                            val geocoder = Geocoder(applicationContext, Locale.getDefault())

                            Thread {
                                try {
                                    val addresses: List<Address>? = geocoder.getFromLocation(latDatabase, longDatabase, 1)
                                    // Check if the addresses list is not empty and handle the result
                                    if (addresses != null && addresses.isNotEmpty()) {
                                        val address = addresses[0].getAddressLine(0)
                                        runOnUiThread {
                                            Toast.makeText(applicationContext, address.toString(), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    runOnUiThread {
                                        Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }.start()





                        }else{
                            Toast.makeText(applicationContext, "Map Doesn't Exist", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener{
                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that may occur during the read operation
                println("Error: ${databaseError.message}")
            }
        })

        

        areaET = findViewById(R.id.areaET)
        showButton = findViewById(R.id.showBTN)

        showButton.setOnClickListener {
            val area = areaET.text.toString().trim()
            if (area.isNotEmpty()) {
                getCoordinatesFromCityName(area)
            } else {
                Toast.makeText(applicationContext, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun getCoordinatesFromCityName(area: String) {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())

        Thread {
            try {
                val addresses: List<Address>? = geocoder.getFromLocationName(area, 1)
                // Check if the addresses list is not empty and handle the result
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val latitude = address.latitude
                    val longitude = address.latitude

                    runOnUiThread {
                        val message = "Latitude: $latitude\nLongitude: $longitude"
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()

                        val userEnterLocation = LatLng(address.latitude, address.latitude)
                        googleMap.addMarker(MarkerOptions().position(userEnterLocation).title("User Enter Location"))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userEnterLocation,15f))

                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "No results found for the given city", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }


    private fun submitFunc() {
        val latitude = latET.text.toString()
        val longitude = longET.text.toString()

        if(latitude.isEmpty() || longitude.isEmpty()){
            Toast.makeText(applicationContext, "Kindly fill the values", Toast.LENGTH_SHORT).show()
        }else{
            val latitudeDouble = latET.text.toString().toDouble()
            val longitudeDouble = longET.text.toString().toDouble()

            val userEnterLocation = LatLng(latitudeDouble, longitudeDouble)
            googleMap.addMarker(MarkerOptions().position(userEnterLocation).title("User Enter Location"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userEnterLocation,15f))

            database = FirebaseDatabase.getInstance().getReference("Maps")
            val mapData = MapData(latitude, longitude)
            database.child("Location "+count++).setValue(mapData).addOnSuccessListener {

                val geocoder = Geocoder(applicationContext, Locale.getDefault())

                Thread {
                    try {
                        val addresses: List<Address>? = geocoder.getFromLocation(latitudeDouble, longitudeDouble, 1)
                        // Check if the addresses list is not empty and handle the result
                        if (addresses != null && addresses.isNotEmpty()) {
                            val address_1 = addresses[0].getAddressLine(0)
                            runOnUiThread {
                                Toast.makeText(applicationContext, address_1.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()


                latET.setText("")
                longET.setText("")

                Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()

            }
        }

    }


    private fun boldFunc(){
        isBold = !isBold
        if (isBold) {
            textTV.setTypeface(null, Typeface.BOLD)

            if (isItalic) {
                textTV.setTypeface(null, Typeface.BOLD_ITALIC)
            } else {
                textTV.setTypeface(null, Typeface.BOLD)
            }

        } else {
            textTV.setTypeface(null, Typeface.NORMAL)

            if (isItalic) {
                textTV.setTypeface(null, Typeface.ITALIC)
            } else {
                textTV.setTypeface(null, Typeface.NORMAL)
            }
        }
    }
    private fun italicFunc(){
        isItalic = !isItalic
        if (isItalic) {
            textTV.setTypeface(null, Typeface.ITALIC)

            if (isBold) {
                textTV.setTypeface(null, Typeface.BOLD_ITALIC)
            }else{
                textTV.setTypeface(null, Typeface.ITALIC)
            }

        } else {
            textTV.setTypeface(null, Typeface.NORMAL)

            if (isBold) {
                textTV.setTypeface(null, Typeface.BOLD)
            }else{
                textTV.setTypeface(null, Typeface.NORMAL)
            }
        }
    }
    private fun underlineFunc(){
        val text = textTV.text.toString()
        val spannableString = SpannableString(text)
        if (isUnderlined) {
            spannableString.removeSpan(UnderlineSpan()) // Remove underline if already applied
        } else {
            spannableString.setSpan(UnderlineSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        isUnderlined = !isUnderlined
        textTV.text = spannableString
    }
}