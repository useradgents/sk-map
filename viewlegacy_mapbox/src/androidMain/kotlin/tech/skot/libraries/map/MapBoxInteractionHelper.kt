package tech.skot.libraries.map


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.delegates.listeners.OnMapIdleListener
import com.mapbox.maps.plugin.delegates.listeners.OnStyleLoadedListener
import com.mapbox.maps.plugin.gestures.addOnFlingListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.getGesturesManager
import com.mapbox.maps.plugin.gestures.getGesturesSettings
import com.mapbox.maps.plugin.overlay.mapboxOverlay
import com.mapbox.maps.toCameraOptions

@SuppressLint("PotentialBehaviorOverride")
open class MapBoxInteractionHelper(
    context: Context,
    mapView: MapView,
    memoryCache: LruCache<String, Bitmap>
) : MapInteractionHelper(context, mapView, memoryCache) {
    private var clickListener: OnPointAnnotationClickListener? = null
    val annotationApi = mapView.annotations
        //annotationSourceOptions?.clusterOptions?.cluster
    val pointAnnotationManager = annotationApi.createPointAnnotationManager(AnnotationConfig())

    private var items: List<Pair<SKMapVC.Marker, PointAnnotation>> = emptyList()
    private var lastSelectedMarker: Pair<SKMapVC.Marker, PointAnnotation>? = null
    override var onMarkerSelected: ((SKMapVC.Marker?) -> Unit)? = null
    override var onMarkerClick: ((SKMapVC.Marker) -> Unit)? = null

    override fun onDestroy() {
        clickListener?.let {
            pointAnnotationManager.removeClickListener(it)

        }

    }


    private fun oldMarkerStillAvailable(
        marker: SKMapVC.Marker,
        markers: List<SKMapVC.Marker>
    ): Boolean {
        return if (marker.id != null) {
            markers.any {
                marker.id == it.id
            }
        } else {
            false
        }
    }

    private fun newMarkerAlreadyExist(marker: SKMapVC.Marker): Boolean {
        return if (marker.id != null) {
            this.items.any {
                marker.id == it.first.id
            }
        } else {
            false
        }
    }


    override fun addMarkers(markers: List<SKMapVC.Marker>) {
        mapView.getMapboxMap { map ->

            //first parts -> items in map still exist in new markers list
            //second parts -> items in maps no longer exist in new markers list
            val (oldItemsToUpdate, oldItemsToRemove) = this.items.partition { currentItem ->
                oldMarkerStillAvailable(currentItem.first, markers)
            }


            //first parts -> update
            //second parts -> add
            val (newValueForItemsToUpdate, newItemsToAdd) = markers.partition { marker ->
                newMarkerAlreadyExist(marker)
            }

            //items to remove from map
            oldItemsToRemove.forEach { pair ->
                if (pair.first.id == lastSelectedMarker?.first?.id) {
                    lastSelectedMarker = null
                }
                pointAnnotationManager.delete(pair.second)
            }

            //items to update on map
            val updatedMarker = oldItemsToUpdate.mapNotNull { currentPair ->
                newValueForItemsToUpdate.find {
                    it.id == currentPair.first.id
                }?.let {
                    Pair(it, currentPair.second.apply {
                        this.geometry = Point.fromLngLat(
                            it.position.second,
                            it.position.first,
                        )
                        getIcon(it, lastSelectedMarker?.first?.id == it.id)?.let {
                            this.iconImageBitmap = it
                        }
                    })
                }
            }

            val optionsToAdd = mutableListOf<PointAnnotationOptions>()
            //items to add to map
            val markers = newItemsToAdd.map { skMarker ->

                val options = PointAnnotationOptions()
                    .withGeometry(
                        Point.fromLngLat(
                            skMarker.position.second,
                            skMarker.position.first
                        )
                    )
                getIcon(skMarker, false)?.let {
                    options.withIconImage(it)
                }
                optionsToAdd.add(options)
                skMarker
            }
            val annotations = pointAnnotationManager.create(optionsToAdd)
            val addedMarkers = markers.zip(annotations)


            this.items = updatedMarker + addedMarkers
        }
    }


    init {
        mapView.getMapboxMap {
            clickListener = OnPointAnnotationClickListener { annotation ->
                val item = items.find { (_, marker) ->
                    marker == annotation
                }
                item?.let {
                    onMarkerClick?.invoke(item.first)
                }
                true
            }.apply {
                pointAnnotationManager.addClickListener(this)
            }


        }
    }

    override fun onOnMapBoundsChange(onMapBoundsChange: ((SKMapVC.LatLngBounds) -> Unit)?) {
        mapView.getMapboxMap { mapBox ->
            mapBox.addOnCameraChangeListener{
                val cameraState = mapBox.cameraState
                val bounds = mapBox.coordinateBoundsForCamera(cameraState.toCameraOptions())

                onMapBoundsChange?.invoke(getMapBounds(bounds))
            }
        }
    }

    protected fun getMapBounds(bounds: CoordinateBounds): SKMapVC.LatLngBounds {
        return SKMapVC.LatLngBounds(
            bounds.north() to bounds.east(),
            bounds.south() to bounds.west()
        )
    }


    override fun onSelectedMarker(selectedMarker: SKMapVC.Marker?) {
        onMarkerSelected?.invoke(selectedMarker)
        mapView.getMapboxMap { mapBoxMap ->
            lastSelectedMarker?.let { current ->
                getIcon(current.first, false)?.let {
                    current.second.iconImage = null
                    current.second.iconImageBitmap = it
                    current.second.iconOpacity = 1.0
                    pointAnnotationManager.update(current.second)
                }
            }
            lastSelectedMarker = items.find {
                it.first.id == selectedMarker?.id
            }?.also { newSelectedMarker ->
                getIcon(newSelectedMarker.first, true)?.let {

                    newSelectedMarker.second.iconImage = null
                    newSelectedMarker.second.iconImageBitmap = it
                    newSelectedMarker.second.iconOpacity = 1.0

                    pointAnnotationManager.update(newSelectedMarker.second)

                }
            }
        }
    }


}
