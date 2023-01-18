package com.mancal.mancal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mancal.mancal.model.Venue
import com.mancal.mancal.recyclerview.ItemDestinationAdapter
import com.mancal.mancal.recyclerview.ItemHistoryAdapter
import com.mancal.mancal.viewmodel.VenueViewModel

class SelectDestinationVenueActivity : AppCompatActivity() {
    private val rcycSelectDestination: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.rcycSelectDestination)
    }
    private lateinit var viewmodel: VenueViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_destination_venue)
        val db = Firebase.firestore
        viewmodel = ViewModelProvider(this).get(VenueViewModel::class.java)
        val listVenue = viewmodel.listVenue
        val liveData = viewmodel.liveDataVenue
        db.collection("venue").get()
            .addOnSuccessListener {
                viewmodel.emptyList()
                for (venue in it) {
                    viewmodel.addVenue(
                        Venue(
                            venue.data["title"] as String,
                            venue.data["address"] as String,
                            if (venue.data["bicycleCount"] != null) (venue.data["bicycleCount"] as Long).toInt() else 0,
                            venue.data["latitude"] as Double,
                            venue.data["longitude"] as Double
                        )
                    )
                }
            }

        val destAdapter: ItemDestinationAdapter = ItemDestinationAdapter(viewmodel.listVenue, this)
        rcycSelectDestination.layoutManager = LinearLayoutManager(this)
        rcycSelectDestination.adapter = destAdapter

    }
}