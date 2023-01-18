package com.mancal.mancal

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mancal.mancal.model.Invoice
import com.mancal.mancal.model.Pesanan
import com.mancal.mancal.viewmodel.JarakViewModel
import com.mancal.mancal.viewmodel.TimerViewModel
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.camera.view.MapboxRecenterButton
import com.mapbox.navigation.ui.maps.camera.view.MapboxRouteOverviewButton
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.*
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.turf.TurfMeasurement
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode


class NavigasiActivity : AppCompatActivity() {
    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }
    private lateinit var pesanan: Pesanan
    private lateinit var titikAwal: Point
    private lateinit var jarakViewModel: JarakViewModel
    private var lastDistance: Double = 0.0
    private lateinit var firstPoint: Point
    private val mapboxReplayer = MapboxReplayer()

    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    private lateinit var navigationCamera: NavigationCamera

    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private lateinit var pointNow: Point

    private val pixelDensity = Resources.getSystem().displayMetrics.density

    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            140.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    private lateinit var maneuverApi: MapboxManeuverApi

    private lateinit var tripProgressApi: MapboxTripProgressApi

    private lateinit var routeLineApi: MapboxRouteLineApi

    private lateinit var routeLineView: MapboxRouteLineView

    private lateinit var durasiLast: String

    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    private lateinit var routeArrowView: MapboxRouteArrowView

    private val navigationLocationProvider = NavigationLocationProvider()

    private val mapViewNavigasi: MapView by lazy {
        findViewById<MapView>(R.id.mapViewNavigasi)
    }

    private val maneuverViewNavigasi: MapboxManeuverView by lazy {
        findViewById<MapboxManeuverView>(R.id.maneuverViewNavigasi)
    }
    private val txtWaktuPerjalananNavigasi: TextView by lazy {
        findViewById<TextView>(R.id.txtWaktuPerjalananNavigasi)
    }

//    private val tripProgressViewNavigasi: MapboxTripProgressView by lazy {
//        findViewById<MapboxTripProgressView>(R.id.tripProgressViewNavigasi)
//    }
    private val routeOverviewNavigasi: MapboxRouteOverviewButton by lazy {
        findViewById<MapboxRouteOverviewButton>(R.id.routeOverviewNavigasi)
    }
    private val recenterNavigasi: MapboxRecenterButton by lazy {
        findViewById<MapboxRecenterButton>(R.id.recenterNavigasi)
    }

    private val txtAkumulasiHargaNavigasi: TextView by lazy {
        findViewById<TextView>(R.id.txtAkumulasiHargaNavigasi)
    }
    private val btnSelesaiNavigasi: Button by lazy {
        findViewById<Button>(R.id.btnSelesaiNavigasi)
    }

//    private val stopNavigasi: ImageView by lazy {
//        findViewById<ImageView>(R.id.stopNavigasi)
//    }


    private fun hasPermission(permission: String): Boolean {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
    }

    private fun canMakeSmores(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }


    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
// update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()


            if (!firstLocationUpdateReceived) {
                firstPoint = Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude)
                firstLocationUpdateReceived = true
                titikAwal = Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude)
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }

        override fun onNewRawLocation(rawLocation: Location) {
        }

    }

    private val routeProgressObserver = RouteProgressObserver {routeProgress ->
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()
        val style = mapViewNavigasi.getMapboxMap().getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this@NavigasiActivity,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                maneuverViewNavigasi.visibility = View.VISIBLE
                maneuverViewNavigasi.renderManeuvers(maneuvers)
            }
        )

//        tripProgressViewNavigasi.render(
//            tripProgressApi.getTripProgress(routeProgress)
//        )

        Log.d("ROUTEPROGRESS", routeProgress.distanceTraveled.toString() + ", " + routeProgress.route.duration())
    }
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
// generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                mapViewNavigasi.getMapboxMap().getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

// update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()
        } else {
// remove the route line and route arrow from the map
            val style = mapViewNavigasi.getMapboxMap().getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

// remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object: MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
//                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
// start the trip session to being receiving location updates in free drive
// and later when a route is set also receiving route progress updates
                if (ActivityCompat.checkSelfPermission(
                        this@NavigasiActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@NavigasiActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
//                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
            }

        },
        onInitialize = this::initNavigation
    )

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
// comment out the location engine setting block to disable simulation
//                .locationEngine(replayLocationEngine)
                .build()
        )

// initialize location puck
        mapViewNavigasi.location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@NavigasiActivity,
                    R.drawable.mapbox_user_puck_icon
                )
            )
            enabled = true
        }



//        replayOriginLocation()
    }

    private fun findRoute(destination: Point) {
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

// execute a route request
// it's recommended to use the
// applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
// that make sure the route request is optimized
// to allow for support of all of the Navigation SDK features
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(originPoint, destination))
// provide the bearing for the origin of the request to ensure
// that the returned route faces in the direction of the current user movement
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
// no impl
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
// no impl
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    setRouteAndStartNavigation(routes)
                }
            }
        )
    }
    private fun setRouteAndStartNavigation(routes: List<NavigationRoute>) {
// set routes, where the first route in the list is the primary route that
// will be used for active guidance
        mapboxNavigation.setNavigationRoutes(routes)

// show UI elements
        routeOverviewNavigasi.visibility = View.VISIBLE
//        tripProgressViewNavigasi.visibility = View.VISIBLE

// move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }
    private fun clearRouteAndStopNavigation() {
// clear
        mapboxNavigation.setNavigationRoutes(listOf())

// stop simulation
//        mapboxReplayer.stop()

// hide UI elements
        maneuverViewNavigasi.visibility = View.INVISIBLE
        routeOverviewNavigasi.visibility = View.INVISIBLE
//        tripProgressViewNavigasi.visibility = View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigasi)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        viewportDataSource = MapboxNavigationViewportDataSource(mapViewNavigasi.getMapboxMap())
        navigationCamera = NavigationCamera(
            mapViewNavigasi.getMapboxMap(),
            mapViewNavigasi.camera,
            viewportDataSource
        )
        mapViewNavigasi.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
// shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> recenterNavigasi.visibility = View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> recenterNavigasi.visibility = View.VISIBLE
            }
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(this).build()
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label-navigation")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)
        val hasilQR = registerForActivityResult(ScanQRCode()) { result ->
            val hasilqr = when(result) {
                is QRResult.QRSuccess -> result.content.rawValue
                QRResult.QRUserCanceled -> "User canceled"
                QRResult.QRMissingPermission -> "Missing Permission"
                is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
            }
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.loading_dialog, null)
            dialogBuilder.setView(dialogView)
            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.show()
            if(this::pesanan.isInitialized) {
                if (hasilqr == pesanan.sepedaAwal.id) {
                    var sepeda = Firebase.firestore.collection("sepeda").document(pesanan.sepedaAwal.id).update("kunci", !pesanan.sepedaAwal.kunci)
                    sepeda.addOnSuccessListener {
                        alertDialog.dismiss()
                        var intentBerhasil: Intent = Intent(this, Activity_Lock_Berhasil::class.java)
                        var invoice: Invoice = Invoice(pesanan.sepedaAwal, pesanan.venueTujuan, pesanan.getTotalBayar(), durasiLast)
                        intentBerhasil.putExtra("INVOICE", invoice)
                        startActivity(intentBerhasil)
                    }
                    sepeda.addOnFailureListener {
                        alertDialog.dismiss()
                        var intentGagal: Intent = Intent(this, Activity_Lock_Gagal::class.java)
                        startActivity(intentGagal)
                        Toast.makeText(this@NavigasiActivity,
                            it.localizedMessage?.toString() ?: "Error", Toast.LENGTH_SHORT).show()
                        Log.e("UPDATEKUNCI", it.localizedMessage)
                    }
                }
                finish()
            }
        }
        mapViewNavigasi.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE) {
// add long click listener that search for a route to the clicked destination
            if (intent.hasExtra("PESANAN")) {
                pesanan = intent.getParcelableExtra<Pesanan>("PESANAN")!!
//                if (pesanan.sepedaAwal.kunci == false) finish()
                var sepeda = Firebase.firestore.collection("sepeda").document(pesanan.sepedaAwal.id).update("kunci", !pesanan.sepedaAwal.kunci)
                sepeda.addOnSuccessListener {
                    findRoute(pesanan.venueTujuan.getPoint())
                }
                val timerViewModel = ViewModelProvider(this).get(TimerViewModel::class.java)
                timerViewModel.getElapsedTime().observe(this@NavigasiActivity) {
                    val durasi = 5
                    txtAkumulasiHargaNavigasi.text = pesanan.getAkumulasiHarga(it.toInt())
                    txtWaktuPerjalananNavigasi.text = timerViewModel.formatDuration()
                    durasiLast = timerViewModel.formatDuration()
//                    TurfMeasurement.distance(pesanan.sepedaAwal.venue.getPoint(), pesanan.venueTujuan.getPoint())
                }
//                if (this@NavigasiActivity::jarakViewModel.isInitialized) {
//                    jarakViewModel.getLiveData().observe(this) {
//                        txtJarakNavigasi.text = it.toString() + " km"
//                    }
//                }

                btnSelesaiNavigasi.setOnClickListener {
                    hasilQR.launch(null)
                }
            } else {
                finish()
            }

        }
        recenterNavigasi.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            routeOverviewNavigasi.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        routeOverviewNavigasi.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            recenterNavigasi.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }


    }

    override fun onDestroy() {
        super.onDestroy()
//        mapboxReplayer.finish()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
    }


}