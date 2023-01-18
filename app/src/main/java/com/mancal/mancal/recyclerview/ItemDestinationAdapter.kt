package com.mancal.mancal.recyclerview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.mancal.mancal.R
import com.mancal.mancal.SelectDestinationVenueActivity
import com.mancal.mancal.model.Venue

class ItemDestinationAdapter(private val listOfVenue: List<Venue>, private val context: Context): RecyclerView.Adapter<ItemDestinationAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtNamaTempatVenue: TextView = itemView.findViewById(R.id.txtNamaTempatVenue)
        val txtAddressVenue: TextView = itemView.findViewById(R.id.txtAddressVenue)
        val txtBicycleCountVenue: TextView = itemView.findViewById(R.id.txtBicycleCountVenue)
        val imgPanahVenue: ImageView = itemView.findViewById(R.id.imgPanahVenue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewModel = listOfVenue[position]
        val activity = context as SelectDestinationVenueActivity
        holder.txtAddressVenue.text = itemViewModel.address
        holder.txtNamaTempatVenue.text = itemViewModel.title
        holder.txtBicycleCountVenue.text = itemViewModel.bicycleCount.toString()
        if (position != listOfVenue.size - 1) {
            val layoutparams = holder.itemView.layoutParams as MarginLayoutParams
            layoutparams.bottomMargin = 24
        }
        holder.itemView.setOnClickListener {
            val intent = Intent()
            intent.putExtra("VENUE", itemViewModel)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish()
        }
    }

    override fun getItemCount(): Int {
        return listOfVenue.size
    }
}