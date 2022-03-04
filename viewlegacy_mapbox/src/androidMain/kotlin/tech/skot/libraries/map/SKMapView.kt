package tech.skot.libraries.map

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.collection.LruCache
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.location
import tech.skot.core.SKLog
import tech.skot.core.components.SKActivity
import tech.skot.core.components.SKComponentView


class SKMapView(
    override val proxy: SKMapViewProxy,
    activity: SKActivity,
    fragment: Fragment?,
    val mapView: MapView
) : SKComponentView<MapView>(proxy, activity, fragment, mapView), SKMapRAI {

    private var lastKnownLocation: Point? = null
    private var mapInteractionHelper: MapInteractionHelper? = null
    private val memoryCache: LruCache<String, Bitmap>
    private var onMapClick: OnMapClickListener? = null

    /**
     * use it to create BitmapDescriptor in case of  [CustomMarker][SKMapVC.CustomMarker] use
     */
    @Suppress("unused")
    var onCreateCustomMarkerIcon: ((SKMapVC.CustomMarker, selected: Boolean) -> Bitmap)? = null
        set(value) {
            field = value
            mapInteractionHelper?.onCreateCustomMarkerIcon = value
        }

    init {

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                mapInteractionHelper
                onMapClick?.let {
                    mapView.getMapboxMap {  mapBox ->
                        mapBox.removeOnMapClickListener(it)
                    }
                }
                super.onDestroy(owner)
            }
        })

        val maxMemory = (Runtime.getRuntime().maxMemory()).toInt()
        val cacheSize = maxMemory / 8
        memoryCache =
            object : LruCache<String, Bitmap>(cacheSize) {

                override fun sizeOf(
                    key: String,
                    bitmap: Bitmap
                ): Int {
                    return bitmap.byteCount
                }
            }
    }


    override fun onSelectedMarker(selectedMarker: SKMapVC.Marker?) {
        mapInteractionHelper?.onSelectedMarker(selectedMarker)
    }

    override fun onItems(items: List<SKMapVC.Marker>) {
        mapInteractionHelper?.addMarkers(markers = items)
    }


    override fun onOnMapClicked(onMapClicked: ((LatLng) -> Unit)?) {
        mapView.getMapboxMap { mapBoxView ->
            onMapClick?.let {
                mapBoxView.removeOnMapClickListener(it)
            }
            onMapClick = OnMapClickListener { point ->
                onMapClicked?.invoke(point.latitude() to point.longitude())
                true
            }

            onMapClick?.let {
                mapBoxView.addOnMapClickListener(it)
            }
        }
    }


    override fun onOnMarkerClick(onMarkerClick: ((SKMapVC.Marker) -> Unit)?) {
        mapInteractionHelper?.onMarkerClick = onMarkerClick
    }



    override fun onMapInteractionSettings(mapInteractionSettings: SKMapVC.MapInteractionSettings) {
        mapView.getMapboxMap { googleMap ->
            mapInteractionHelper = when (mapInteractionSettings) {
                is SKMapVC.MapClusteringInteractionSettings -> {
                    throw NotImplementedError()
//                    GMapClusteringInteractionHelper(
//                        context = activity,
//                        mapView = mapView,
//                        memoryCache = memoryCache,
//                        onClusterClick = mapInteractionSettings.onClusterClick
//                    )
                }
                is SKMapVC.MapNormalInteractionSettings -> {
                    MapBoxInteractionHelper(activity, mapView, memoryCache)
                }
                is SKMapVC.MapCustomInteractionSettings -> {
                    mapRefCustomInteractionHelper[mapInteractionSettings.customRef]?.invoke(
                        activity,
                        mapView,
                        memoryCache,
                        mapInteractionSettings.data
                    )
                        ?: throw NotImplementedError("With MapCustomInteractionSettings you must provide a CustomInteractionHelper with ref ${mapInteractionSettings.customRef} in mapRefCustomInteractionHelper ")
                }
            }.apply {
                this.onCreateCustomMarkerIcon = this@SKMapView.onCreateCustomMarkerIcon

                this.onOnMapBoundsChange(proxy.onMapBoundsChange)
                this.onMarkerClick = proxy.onMarkerClicked
                this.onMarkerSelected = proxy.onMarkerSelected
                this.onSelectedMarker(proxy.selectedMarker)
                this.addMarkers(proxy.markers)
            }



        }

    }


    override fun setCameraPosition(
        position: LatLng,
        zoomLevel: Float,
        animate: Boolean
    ) {
        mapView.getMapboxMap {
            val moveOptions = cameraOptions {
                center(Point.fromLngLat(position.first, position.second))
                zoom(zoomLevel.toDouble())
            }
            if (animate) {
                it.flyTo(
                    moveOptions
                )
            } else {
                it.setCamera(
                    moveOptions
                )
            }
        }
    }


    private fun zoomToMultipleGeoCoordinates(boundingList: List<LatLng>): CoordinateBounds {
        var north = Double.MIN_VALUE
        var south = Double.MAX_VALUE
        var east = Double.MIN_VALUE
        var west = Double.MAX_VALUE

        boundingList.forEach { loc ->
            north = loc.first.coerceAtLeast(north)
            south = loc.first.coerceAtMost(south)
            west = loc.second.coerceAtMost(west)
            east = loc.second.coerceAtLeast(east)
        }

//        if (isPaddingRequired) {
//            val padding = 0.01
//            north += padding
//            south -= padding
//            west -= padding
//            east += padding
        // google mapView (16 * Resources.getSystem().displayMetrics.density).toInt()
//        }


        return CoordinateBounds(Point.fromLngLat(west, south), Point.fromLngLat(east, north))
    }


    override fun centerOnPositions(positions: List<LatLng>) {
        if (positions.isNotEmpty()) {
            val box = zoomToMultipleGeoCoordinates(positions)
            mapView.getMapboxMap {
                val bounds = it.cameraForCoordinateBounds(box)
                it.flyTo(
                    bounds
                )
            }
        }
    }


    override fun getMapBounds(onResult: (SKMapVC.LatLngBounds) -> Unit) {
        mapView.getMapboxMap {
            it.getBounds().bounds.let {
                onResult(
                    SKMapVC.LatLngBounds(
                        it.northeast.latitude() to it.northeast.longitude(),
                        it.southwest.latitude() to it.southwest.longitude()
                    )
                )
            }
        }
    }

    override fun onOnMapBoundsChange(onMapBoundsChange: ((SKMapVC.LatLngBounds) -> Unit)?) {
        mapInteractionHelper?.onOnMapBoundsChange(onMapBoundsChange)
    }


    override fun setCameraZoom(zoomLevel: Float, animate: Boolean) {
        mapView.getMapboxMap {
            if (animate) {
                it.flyTo(
                    cameraOptions {
                        zoom(zoomLevel.toDouble())
                    }
                )
            } else {
                it.setCamera(
                    cameraOptions {
                        zoom(zoomLevel.toDouble())
                    }
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun showMyLocationButton(
        show: Boolean,
        onPermissionError: (() -> Unit)?
    ) {
        mapView.getMapboxMap { mapboxMap ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                SKLog.d("enabled button :$show")
                val button = ImageButton(mapView.context)
                mapView.addView(button)
                val params = button.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.LEFT or Gravity.TOP
                mapView.location.enabled = true
                mapView.location.getLocationProvider()
                    ?.registerLocationConsumer(object : LocationConsumer {
                        override fun onBearingUpdated(
                            vararg bearing: Double,
                            options: (ValueAnimator.() -> Unit)?
                        ) {

                        }

                        override fun onLocationUpdated(
                            vararg location: Point,
                            options: (ValueAnimator.() -> Unit)?
                        ) {
                            lastKnownLocation = location.last()
                        }

                        override fun onPuckBearingAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {

                        }

                        override fun onPuckLocationAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {

                        }

                    })
                button.setOnClickListener { view ->


                    if (lastKnownLocation != null) {
                        mapboxMap.flyTo(
                            cameraOptions {
                                center(lastKnownLocation)
                            }
                        )
                    }

                }
            } else {
                SKLog.d("permission error :$show")
                onPermissionError?.invoke()
            }
        }
    }


    companion object {
        val mapRefCustomInteractionHelper: MutableMap<Int, (context: Context, mapView: MapView, memoryCache: LruCache<String, Bitmap>, data: Any?) -> MapInteractionHelper> =
            mutableMapOf()
    }


}


fun MapView.getMapboxMap(onReady: (MapboxMap) -> Unit) {

    this.getMapboxMap().let { mapboxMap ->
        try {
            if (mapboxMap.isFullyLoaded()) {
                onReady.invoke(mapboxMap)
            } else {
                var listener: OnMapLoadedListener? = null
                listener = OnMapLoadedListener {
                    onReady.invoke(mapboxMap)
                    listener?.let {
                        mapboxMap.removeOnMapLoadedListener(it)
                    }

                }
                mapboxMap.addOnMapLoadedListener(listener)
            }
        } catch (e: Exception) {
            var listener: OnMapLoadedListener? = null
            listener = OnMapLoadedListener {
                onReady.invoke(mapboxMap)
                listener?.let {
                    mapboxMap.removeOnMapLoadedListener(it)
                }
            }
            mapboxMap.addOnMapLoadedListener(listener)
        }
    }
}
