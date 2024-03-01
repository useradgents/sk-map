package tech.skot.libraries.map


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import androidx.collection.LruCache
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.skot.libraries.skmap.viewlegacy.R


@SuppressLint("PotentialBehaviorOverride")
open class GMapInteractionHelper(
    context: Context,
    mapView: MapView,
    memoryCache: LruCache<String, SKMapView.BitmapDescriptorContainer>
) : MapInteractionHelper(context, mapView, memoryCache) {


    private var items: List<Pair<SKMapVC.Marker, Marker>> = emptyList()
    private var lastSelectedMarker: Pair<SKMapVC.Marker, Marker>? = null
    override var onMarkerClick: ((SKMapVC.Marker) -> Unit)? = null
    private val infoWindowView =
        LayoutInflater.from(context).inflate(R.layout.fake_popup_window, null)
    private val adapter = MapWindowAdapter()

    init {
        mapView.getMapAsync {
            it.setInfoWindowAdapter(adapter)
            it.setOnMarkerClickListener { clickedMarker ->
                val item = items.find { (_, marker) ->
                    marker == clickedMarker
                }
                item?.let {
                    onMarkerClick?.invoke(item.first)
                    item.second.showInfoWindow()
                }
                true
            }
        }
    }


    inner class MapWindowAdapter() :
        InfoWindowAdapter {

        // Hack to prevent info window from displaying: use a 0dp/0dp frame
        override fun getInfoWindow(marker: Marker): View {
            return infoWindowView
        }

        override fun getInfoContents(marker: Marker): View? {
            return null
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
        mapView.getMapAsync { map ->
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
                pair.second.remove()
            }


            //items to update on map
            val updatedMarker = oldItemsToUpdate.mapNotNull { currentPair ->
                newValueForItemsToUpdate.find {
                    it.id == currentPair.first.id
                }?.let {
                    Pair(it, currentPair.second.apply {
                        this.position = LatLng(
                            it.position.first,
                            it.position.second
                        )
                        isVisible = !it.hidden

                        val isSelected = lastSelectedMarker?.first?.id == it.id

                        getMarkerAnchor?.invoke(it, isSelected)?.let {
                            setAnchor(it.first, it.second)
                        }

                        getIcon(it, isSelected)?.let {
                            this.setIcon(it)
                        }
                    })
                }
            }


            //items to add to map
            val addedMarker = newItemsToAdd.mapNotNull { skMarker ->

                val anchor = getMarkerAnchor?.invoke(skMarker, false)


                val marker =
                    map.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    skMarker.position.first,
                                    skMarker.position.second
                                )
                            )
                            .anchor(anchor?.first ?: 0.5f, anchor?.second ?: 1f)
                            .visible(!skMarker.hidden)
                            .icon(getIcon(skMarker, false))
                    )

                marker?.let {
                    Pair(skMarker, marker)
                }
            }
            this@GMapInteractionHelper.items = updatedMarker + addedMarker
        }

    }

    override fun onOnMapBoundsChange(onMapBoundsChange: ((SKMapVC.LatLngBounds) -> Unit)?) {
        mapView.getMapAsync {
            if (onMapBoundsChange == null) {
                it.setOnCameraIdleListener(null)
            } else {
                it.setOnCameraIdleListener {
                    onMapBoundsChange(getMapBounds(it.projection))
                }
            }
        }
    }

    protected fun getMapBounds(projection: Projection): SKMapVC.LatLngBounds {
        return projection.visibleRegion.latLngBounds.let {
            SKMapVC.LatLngBounds(
                it.northeast.latitude to it.northeast.longitude,
                it.southwest.latitude to it.southwest.longitude
            )

        }
    }

    override fun onSelectedMarker(selectedMarker: SKMapVC.Marker?) {

        mapView.getMapAsync {
            CoroutineScope(Dispatchers.Main).launch {
                lastSelectedMarker?.let { current ->
                    getIcon(current.first, false)?.let {
                        current.second.setIcon(it)
                    }
                }
                lastSelectedMarker = items.find {
                    it.first == selectedMarker
                }?.also { newSelectedMarker ->
                    getIcon(newSelectedMarker.first, true)?.let {
                        newSelectedMarker.second.setIcon(it)
                    }
                }
            }
        }
    }

    private fun getBitmapDescriptorFromBitmap(bitmap: Bitmap): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


}
