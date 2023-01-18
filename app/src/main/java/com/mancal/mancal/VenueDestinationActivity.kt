package com.mancal.mancal

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.util.Property
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.set
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mancal.mancal.model.Sepeda
import com.mancal.mancal.model.Venue
import com.mancal.mancal.utils.LocationPermissionHelper
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.compat.permissions.PermissionsManager
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.image.ImageExtensionImpl
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.coordinates
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference


class VenueDestinationActivity : AppCompatActivity() {
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var mapViewSelectDest: MapView
    private lateinit var pointNow: Point
    private var firstTime: Boolean = true
    private var zoomLevel: Double = 18.0
    private var isObah: Boolean = false
    private lateinit var venueFirstData: Venue
    private lateinit var sepedaFirstData: Sepeda


    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        if (isObah == false) {
            mapViewSelectDest.getMapboxMap().setCamera(CameraOptions.Builder().center(it).padding(EdgeInsets(100.0, 100.0, 100.0, 100.0)).build())
        }
//        Toast.makeText(this, "latitude: ${it.latitude()}, longitude: ${it.longitude()}", Toast.LENGTH_SHORT).show()
        Log.d("ONINDICATORDEBUG", "latitude: ${it.latitude()}, longitude: ${it.longitude()}")
        mapViewSelectDest.gestures.focalPoint = mapViewSelectDest.getMapboxMap().pixelForCoordinate(it)
        pointNow = it
    }
    private val btnQrBicycle: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.btnQrBicycle)
    }
    private val txtNamaJudulVenue: TextView by lazy {
        findViewById<TextView>(R.id.txtNamaJudulVenue)
    }
    private val txtNamaJudulDest: TextView by lazy {
        findViewById<TextView>(R.id.txtNamaJudulDest)
    }
    private val btnCenterPointVenueDest: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.btnCenterPointVenueDest)
    }
    private val lytDestination: ConstraintLayout by lazy {
        findViewById<ConstraintLayout>(R.id.lytDestination)
    }
    private val btnProsesVenueDest: Button by lazy {
        findViewById<Button>(R.id.btnProsesVenueDest)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private val getDestinationVenue = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val intent = it.data
            val destVenue: Venue = intent?.getParcelableExtra<Venue>("VENUE") as Venue
            Toast.makeText(this, destVenue.title, Toast.LENGTH_SHORT).show()
            prepareDest(destVenue)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_destination)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        mapViewSelectDest = findViewById(R.id.mapViewSelectDest)
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
//        btnQrBicycle = findViewById(R.id.btnQrBicycle)
        val hasilQR = registerForActivityResult(ScanQRCode()) { result ->
//            val hasilqrintent = Intent(this@VenueDestinationActivity,HasilQRActivity::class.java)
            val hasilqr = when(result) {
                is QRResult.QRSuccess -> result.content.rawValue
                QRResult.QRUserCanceled -> "User canceled"
                QRResult.QRMissingPermission -> "Missing Permission"
                is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
            }
//            Toast.makeText(this, hasilqr, Toast.LENGTH_SHORT).show()
            Log.d("SCANQR", hasilqr)
            val db = Firebase.firestore
            val data = db.collection("sepeda").document(hasilqr).get()

            data.addOnSuccessListener { sepeda ->
                var venue = sepeda.get("venue") as DocumentReference
                venue.get().addOnSuccessListener { resultVenue ->
                    Log.d("FIRESTOREREFERENCE", resultVenue.get("title") as String)
                    venueFirstData = Venue(resultVenue.get("title") as String, resultVenue.get("address") as String, (resultVenue.get("bicycleCount") as Long).toInt(), resultVenue.get("latitude") as Double, resultVenue.get("longitude") as Double)
                    prepareVenue(venueFirstData)
                    sepedaFirstData = Sepeda(sepeda.id, sepeda.get("model") as String, venueFirstData, sepeda.get("kunci") as Boolean)
                }
            }
//            Log.d("HASILVENUE", venueData.toString())

//            hasilqrintent.putExtra("HASIL", hasilqr)
//            startActivity(hasilqrintent)
//            finish()
        }
        btnQrBicycle.setOnClickListener {
            hasilQR.launch(null)
        }
        btnCenterPointVenueDest.setOnClickListener {
            setupGesturesListener()
            isObah = false
            if (this::pointNow.isInitialized) {
                mapViewSelectDest.getMapboxMap().setCamera(CameraOptions.Builder().center(pointNow).build())
            }
//            mapViewSelectDest.location.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            btnCenterPointVenueDest.visibility = View.GONE
        }
        lytDestination.setOnClickListener {
            if (this::venueFirstData.isInitialized) {
                getDestinationVenue.launch(Intent(this, SelectDestinationVenueActivity::class.java))
            } else {
                Toast.makeText(this, "Pilih venue awal dahulu!", Toast.LENGTH_SHORT).show()
            }
        }

    }
//    private fun loadVenueQR(qr: String): Venue {
//        val db = Firebase.firestore
//        val data = db.collection("sepeda").document(qr).get()
//
//        data.addOnSuccessListener { sepeda ->
//            Log.d("FIRESTORESEPEDA", sepeda.get("model") as String)
//            var venue = sepeda.get("venue") as DocumentReference
//            venue.get().addOnSuccessListener { resultVenue ->
//                Log.d("FIRESTOREREFERENCE", resultVenue.get("title") as String)
//                venueFirstData = Venue(resultVenue.get("title") as String, resultVenue.get("address") as String, (resultVenue.get("bicycleCount") as Long).toInt(), resultVenue.get("latitude") as Long, resultVenue.get("longitude") as Long)
//
//            }
//        }
//        Log.d("HASILVENUE", venueFirstData.toString())
//        data.addOnFailureListener {
//            throw it
//        }
////        Log.d("FIRESTORESEPEDA", data.)
////        Toast.makeText(this, data.result.id, Toast.LENGTH_SHORT).show()
//        return venueFirstData
//
//    }
    private fun prepareVenue(venue: Venue) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading_dialog, null)
        dialogBuilder.setView(dialogView)

        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()
        txtNamaJudulVenue.text = venue.title
        Toast.makeText(this, venue.title, Toast.LENGTH_SHORT).show()
        alertDialog.dismiss()
    }
    private fun prepareDest(venue: Venue) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading_dialog, null)
        dialogBuilder.setView(dialogView)

        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()
        txtNamaJudulDest.text = venue.title
        Toast.makeText(this, venue.title, Toast.LENGTH_SHORT).show()
        alertDialog.dismiss()
        btnProsesVenueDest.visibility = View.VISIBLE
        val coordinateLists = listOf(
            listOf(
                venueFirstData.getPoint(),
                venue.getPoint(),
                pointNow
            )
        )
        val polygon = Polygon.fromLngLats(coordinateLists)
        val paddingDouble: Double = 200.0
        val cameraPosition = mapViewSelectDest.getMapboxMap().cameraForGeometry(polygon, EdgeInsets(paddingDouble, paddingDouble, paddingDouble, paddingDouble))
        mapViewSelectDest.getMapboxMap().setCamera(cameraPosition)
        onCameraTrackingDismissed()

        val routeOptions = RouteOptions.builder()
            .coordinates(venueFirstData.getPoint(), null, venue.getPoint())
            .profile(DirectionsCriteria.PROFILE_CYCLING)
            .alternatives(false)
            .build()

        val mapboxDirections = MapboxDirections.builder()
            .routeOptions(routeOptions)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()

        mapboxDirections.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.body() == null) {
                    Log.e("ERRROUTE","No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Log.e("ERRROUTE","No routes found")
                    return
                }
                val response = response
                val currentRoute = response.body()!!.routes()[0]
//                Log.d("RESROUTE", response.body().toString())
                if (currentRoute != null) {
                    val directionsRouteFeature = Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry()!!, PRECISION_6))
                    mapViewSelectDest.getMapboxMap().loadStyle(
                        style(styleUri = Style.MAPBOX_STREETS) {
                            +geoJsonSource("icon-source") {
                                val featureCollection = FeatureCollection.fromFeatures(listOf<Feature>(
                                    Feature.fromGeometry(venueFirstData.getPoint()),
                                    Feature.fromGeometry(venue.getPoint())
                                ))
                                featureCollection(featureCollection)
                            }
                            +geoJsonSource("line") {
                                feature(directionsRouteFeature)
                            }
                            +lineLayer("linelayer", "line") {
                                lineCap(LineCap.ROUND)
                                lineJoin(LineJoin.ROUND)
                                lineOpacity(0.7)
                                lineWidth(8.0)
                                lineColor("#db5eb0")
                            }
                            +image("red-marker") {
                                var gambar: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)
                                gambar = Bitmap.createScaledBitmap(gambar, gambar.width / 4, gambar.height / 4, false)
                                bitmap(gambar)
                            }
                            +symbolLayer("icon-layer", "icon-source") {
                                iconImage("red-marker")
                                iconIgnorePlacement(true)
                                iconAllowOverlap(true)
                                iconOffset(listOf<Double>(0.0, -9.0))
                            }
                        }
                    )
                }

            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Toast.makeText(this@VenueDestinationActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
        btnProsesVenueDest.setOnClickListener {
            val intent: Intent = Intent(this, Activity_ReviewPesanan::class.java)
            if (this::sepedaFirstData.isInitialized) {
                intent.putExtra("SEPEDAAWAL", sepedaFirstData)
            } else {
                intent.putExtra("VENUEAWAL", venueFirstData)
            }
            intent.putExtra("VENUETUJUAN", venue)
            startActivity(intent)
            finish()
        }
    }

    private fun onMapReady() {
        mapViewSelectDest.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(zoomLevel)
                .build()
        )
        mapViewSelectDest.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }
    private fun setupGesturesListener() {
        mapViewSelectDest.gestures.addOnMoveListener(onMoveListener)
    }
    private fun initLocationComponent() {
        val locationComponentPlugin = mapViewSelectDest.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            val bearing = AppCompatResources.getDrawable(
                this@VenueDestinationActivity,
                R.drawable.mapbox_user_puck_icon,
            )?.current
            val shadow = AppCompatResources.getDrawable(
                this@VenueDestinationActivity,
                R.drawable.mapbox_user_icon_shadow
            )
            this.locationPuck = LocationPuck2D(
                bearingImage = bearing,
                shadowImage = shadow,
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(10.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }
    private fun onCameraTrackingDismissed() {
        btnCenterPointVenueDest.visibility = View.VISIBLE
//        mapViewSelectDest.location
//            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        mapViewSelectDest.location
//            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        isObah = true
        mapViewSelectDest.gestures.removeOnMoveListener(onMoveListener)
    }

    private fun updateCamera(point: Point) {
        val mapAnimationOptions = MapAnimationOptions.Builder().build()
        if (isObah == false) {
            mapViewSelectDest.camera.easeTo(
                CameraOptions.Builder()
                    .center(point)
                    .padding(EdgeInsets(500.0, 0.0, 0.0, 0.0))
                    .build(), mapAnimationOptions
            )
        }
    }
//    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
//        mapViewSelectDest.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
//        mapViewSelectDest.getMapboxMap().pixelForCoordinate(it)
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode,
            permissions, grantResults)
    }

    override fun onStart() {
        super.onStart()
        mapViewSelectDest.location.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onStop() {
        super.onStop()
        mapViewSelectDest.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
//        mapViewSelectDest.location
//            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapViewSelectDest.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapViewSelectDest.gestures.removeOnMoveListener(onMoveListener)
    }
}