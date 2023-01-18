package com.mancal.mancal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mancal.mancal.model.Venue
import com.mancal.mancal.recyclerview.ItemHistoryAdapter
import com.mancal.mancal.viewmodel.VenueViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var btnCariVenueHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCariVenueHome = view.findViewById(R.id.btnCariVenueHome) as Button
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_item)
        val viewmodel: VenueViewModel by lazy {
            ViewModelProvider(this).get(VenueViewModel::class.java)
        }
        val listVenue = viewmodel.listVenue
//        viewmodel.emptyList()

        val db = Firebase.firestore
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

//
        val venueAdapter: ItemHistoryAdapter = ItemHistoryAdapter(listVenue)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = venueAdapter
        btnCariVenueHome.setOnClickListener {
            var intent = Intent(it.context, VenueDestinationActivity::class.java)
            view.context.startActivity(intent)
        }
        viewmodel.liveDataVenue.observe(viewLifecycleOwner) {
            venueAdapter.notifyDataSetChanged()
            val venueAdapter: ItemHistoryAdapter = ItemHistoryAdapter(it)
            recyclerView.adapter = venueAdapter
        }
    }
}
       