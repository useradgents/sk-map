package tech.skot.libraries.map

import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tech.skot.core.toColor

class GMapClusteringInteractionHelper(
    context: Context,
    mapView: MapView,
    memoryCache: LruCache<String, SKMapView.BitmapDescriptorContainer>,
    val settings: SKMapVC.MapClusteringInteractionSettings
) : MapInteractionHelper(context, mapView, memoryCache) {
    private var _clusterManager: ClusterManager<SKClusterMarker>? = null
    private var onMapBoundsChange: ((MapBounds) -> Unit)? = null
    private var lastSelectedMarker: SKClusterMarker? = null
    override var onMarkerClick: ((SKMapVC.Marker) -> Unit)? = null
    private var mutext = Mutex()
    var getClusterIcon: ((List<SKMapVC.Marker>) -> Bitmap?)? = null
    val onClusterClick: ((List<SKMapVC.Marker>) -> Unit)? = settings.onClusterClick


    private var items: List<SKClusterMarker> = emptyList()


    private fun getClusterManagerAsync(onReady: (ClusterManager<SKClusterMarker>) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            mutext.lock()
            _clusterManager?.let {
                onReady.invoke(it)
                mutext.unlock()
            } ?: kotlin.run {
                mapView.getMapAsync { googleMap ->
                    val clusterManager = ClusterManager<SKClusterMarker>(context, googleMap)
                    clusterManager.renderer = object : DefaultClusterRenderer<SKClusterMarker>(
                        context,
                        googleMap,
                        clusterManager
                    ) {

                        override fun getColor(clusterSize: Int): Int {
                            return settings.getClusterColor?.invoke(clusterSize)?.toColor(context)
                                ?: super.getColor(clusterSize)
                        }

                        override fun getBucket(cluster: Cluster<SKClusterMarker>): Int {
                            return settings.getBucket?.invoke(cluster.size) ?: super.getBucket(
                                cluster
                            )
                        }

                        override fun getClusterText(bucket: Int): String {
                            return settings.getClusterText?.invoke(bucket) ?: super.getClusterText(
                                bucket
                            )
                        }


                        override fun getMinClusterSize(): Int {
                            return settings.getMinClusterSize?.invoke() ?: super.getMinClusterSize()
                        }


                        override fun onBeforeClusterItemRendered(
                            item: SKClusterMarker,
                            markerOptions: MarkerOptions
                        ) {
                            CoroutineScope(context = Dispatchers.Main).launch {
                                getIcon(item.marker, item.selected)?.let {
                                    markerOptions.icon(it)
                                }
                            }
                            super.onBeforeClusterItemRendered(item, markerOptions)
                        }

                        override fun onClusterItemUpdated(item: SKClusterMarker, marker: Marker) {
                            super.onClusterItemUpdated(item, marker)
                            CoroutineScope(context = Dispatchers.Main).launch {
                                getIcon(item.marker, item.selected)?.let {
                                    marker.setIcon(it)
                                }

                            }
                        }

                        override fun onBeforeClusterRendered(
                            cluster: Cluster<SKClusterMarker>,
                            markerOptions: MarkerOptions
                        ) {
                            super.onBeforeClusterRendered(cluster, markerOptions)
                            getClusterIcon?.invoke(cluster.items.map { it.marker })?.let {
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(it))
                            }
                        }

                    }

                    googleMap.setOnCameraIdleListener {
                        MapLoggerView.d("OnCameraIdleListener")
                        clusterManager.onCameraIdle()
                        onMapBoundsChange?.invoke(getMapBounds(googleMap.projection))
                    }
                    clusterManager.setOnClusterClickListener {
                        it.items.map {
                            it.marker
                        }.let {
                            MapLoggerView.d("on cluster click")
                            onClusterClick?.invoke(it)
                        }
                        true
                    }
                    clusterManager.setOnClusterItemClickListener {
                        it.marker.let {


                            MapLoggerView.d("on marker click ${it.id}")
                            onMarkerClick?.invoke(it)
                        }
                        true
                    }
                    _clusterManager = clusterManager
                    onReady.invoke(clusterManager)
                    mutext.unlock()

                }
            }

        }

    }


    override fun onSelectedMarker(selectedMarker: SKMapVC.Marker?) {
        getClusterManagerAsync { clusterManager ->
            lastSelectedMarker?.let {
                it.selected = false
                clusterManager.updateItem(lastSelectedMarker)
                clusterManager.cluster()
            }

            lastSelectedMarker = items.find {
                it.marker == selectedMarker
            }?.apply {
                this.selected = true
                clusterManager.updateItem(this)
                clusterManager.cluster()
            }
        }
    }

    override fun onOnMapBoundsChange(onMapBoundsChange: ((SKMapVC.LatLngBounds) -> Unit)?) {
        this.onMapBoundsChange = onMapBoundsChange
    }

    private fun oldMarkerStillAvailable(
        marker: SKClusterMarker,
        markers: List<SKMapVC.Marker>
    ): Boolean {
        return if (marker.marker.id != null) {
            markers.any {
                marker.marker.id == it.id
            }
        } else {
            false
        }
    }

    private fun newMarkerAlreadyExist(marker: SKMapVC.Marker): Boolean {
        return if (marker.id != null) {
            this.items.any {
                marker.id == it.marker.id
            }
        } else {
            false
        }
    }


    override fun addMarkers(markers: List<SKMapVC.Marker>) {
        getClusterManagerAsync { clusterManager ->

            //first parts -> items in map still exist in new markers list
            //second parts -> items in maps no longer exist in new markers list
            val (oldItemsToUpdate, oldItemsToRemove) = this.items.partition { currentItem ->
                oldMarkerStillAvailable(currentItem, markers)
            }

            //first parts -> update
            //second parts -> add
            val (newValueForItemsToUpdate, newItemsToAdd) = markers.partition { marker ->
                newMarkerAlreadyExist(marker)
            }

            val (hiddenMarkerUpdate, visibleMarkerUpdate) = newValueForItemsToUpdate.partition { marker ->
                marker.hidden
            }

            val (hiddenNewMarkerUpdate, visibleNewMarker) = newItemsToAdd.partition { marker ->
                marker.hidden
            }


            //remove items not in new list
            if (oldItemsToRemove.any { it.marker.id == lastSelectedMarker?.marker?.id }) {
                lastSelectedMarker = null
            }
            clusterManager.removeItems(oldItemsToRemove)

            //hide items
            val updateHidden = hiddenMarkerUpdate.mapNotNull { marker ->
                oldItemsToUpdate.find {
                    it.marker.id == marker.id
                }?.let {
                    clusterManager.removeItem(it)
                    if (marker.id == lastSelectedMarker?.marker?.id) {
                        lastSelectedMarker = null
                    }
                    it.marker = marker
                    it
                }
            }

            val updateVisble = visibleMarkerUpdate.mapNotNull { marker ->
                oldItemsToUpdate.find {
                    it.marker.id == marker.id
                }?.let {
                    if (!it.marker.hidden) {
                        it.marker = marker
                        clusterManager.updateItem(it)
                    } else {
                        it.marker = marker
                        clusterManager.addItem(it)
                    }
                    it
                }
            }


            val addHidden = hiddenNewMarkerUpdate.map { marker ->
                SKClusterMarker(marker, false)
            }

            val addVisible = visibleNewMarker.map { marker ->
                SKClusterMarker(marker, false)
            }
            loadingImageJob?.cancel()
            loadingImageJob = CoroutineScope(context = Dispatchers.Main).launch {
                addVisible.forEach {
                    getIcon(it.marker, false)
                }
                clusterManager.addItems(addVisible)

                items = addVisible + addHidden + updateVisble + updateHidden

                clusterManager.cluster()
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


}