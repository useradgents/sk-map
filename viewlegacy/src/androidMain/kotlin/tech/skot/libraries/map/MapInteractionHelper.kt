package tech.skot.libraries.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.MapView import com.google.android.gms.maps.model.*
import tech.skot.core.toColor
import tech.skot.core.toPixelSize
import tech.skot.core.view.Color
import com.google.android.gms.maps.model.LatLng as LatLngGMap
import tech.skot.libraries.map.LatLng

abstract class MapInteractionHelper(
    val context: Context,
    val mapView: MapView,
    val memoryCache: LruCache<String, SKMapView.BitmapDescriptorContainer>
) {

    private var polylineItems: List<Pair<SKMapVC.Polyline, Polyline>> = emptyList()
    private var polygonItems: List<Pair<SKMapVC.Polygon, Polygon>> = emptyList()
    abstract var onMarkerClick: ((SKMapVC.Marker) -> Unit)?
    var onCreateCustomMarkerIcon:  (suspend (SKMapVC.CustomMarker, selected: Boolean) -> Bitmap?)? = null
    var getMarkerAnchor: ((SKMapVC.Marker, selected: Boolean) -> Pair<Float, Float>?)? = null
    abstract fun onSelectedMarker(selectedMarker: SKMapVC.Marker?)
    abstract fun addMarkers(markers: List<SKMapVC.Marker>)

    abstract fun onOnMapBoundsChange(onMapBoundsChange: ((SKMapVC.LatLngBounds) -> Unit)?)

    private fun oldLineStillAvailable(
        polyline: SKMapVC.Polyline,
        polylines: List<SKMapVC.Polyline>
    ): Boolean {
        return if (polyline.id != null) {
            polylines.any {
                polyline.id == it.id
            }
        } else {
            false
        }
    }

    private fun oldPolygonStillAvailable(
        polygon: SKMapVC.Polygon,
        polygons: List<SKMapVC.Polygon>
    ): Boolean {
        return if (polygon.id != null) {
            polygons.any {
                polygon.id == it.id
            }
        } else {
            false
        }
    }


    private fun newPolygonAlreadyExist(polygon: SKMapVC.Polygon): Boolean {
        return if (polygon.id != null) {
            this.polygonItems.any {
                polygon.id == it.first.id
            }
        } else {
            false
        }
    }


    private fun newLineAlreadyExist(polyline: SKMapVC.Polyline): Boolean {
        return if (polyline.id != null) {
            this.polylineItems.any {
                polyline.id == it.first.id
            }
        } else {
            false
        }
    }

    fun addPolygons(polygons : List<SKMapVC.Polygon>){
        mapView.getMapAsync { googleMap ->
            //first parts -> items in map still exist in new markers list
            //second parts -> items in maps no longer exist in new markers list
            val (oldItemsToUpdate, oldItemsToRemove) = this.polygonItems.partition { currentItem ->
                oldPolygonStillAvailable(currentItem.first, polygons)
            }

            //first parts -> update
            //second parts -> add
            val (newValueForItemsToUpdate, newItemsToAdd) = polygons.partition { marker ->
                newPolygonAlreadyExist(marker)
            }

            //items to remove from map
            oldItemsToRemove.forEach { pair ->
                pair.second.remove()
            }
            //items to update on map
            val updatedMarker = oldItemsToUpdate.mapNotNull { currentPair : Pair<SKMapVC.Polygon, Polygon> ->
                newValueForItemsToUpdate.find {
                    it.id == currentPair.first.id
                }?.let {
                    Pair(it, currentPair.second.apply {
                        this.points = it.points.map {
                            LatLngGMap(it.first, it.second)
                        }
                        this.isVisible = !it.hidden
                        fillColor = it.fillColor.toColor(context)
                        strokeColor = it.strokeColor.toColor(context)
                        strokeWidth = it.lineWidth.toPixelSize(context).toFloat()
                    })
                }
            }

            //items to add to map
            val addedLines = newItemsToAdd.map { polygon ->
                googleMap.addPolygon(
                    PolygonOptions()
                        .addAll(polygon.points.map {
                            LatLngGMap(it.first, it.second)
                        })
                        .visible(!polygon.hidden)
                        .fillColor(polygon.fillColor.toColor(context))
                        .strokeColor(polygon.strokeColor.toColor(context))
                        .strokeWidth(polygon.lineWidth.toPixelSize(context).toFloat())
                ).let {
                    Pair(polygon, it)
                }

            }

            this.polygonItems = updatedMarker + addedLines

        }
    }


    fun addLines(polylines: List<SKMapVC.Polyline>) {
        mapView.getMapAsync { googleMap ->

            //first parts -> items in map still exist in new markers list
            //second parts -> items in maps no longer exist in new markers list
            val (oldItemsToUpdate, oldItemsToRemove) = this.polylineItems.partition { currentItem ->
                oldLineStillAvailable(currentItem.first, polylines)
            }

            //first parts -> update
            //second parts -> add
            val (newValueForItemsToUpdate, newItemsToAdd) = polylines.partition { marker ->
                newLineAlreadyExist(marker)
            }

            //items to remove from map
            oldItemsToRemove.forEach { pair ->
                pair.second.remove()
            }
            //items to update on map
            val updatedMarker = oldItemsToUpdate.mapNotNull { currentPair ->
                newValueForItemsToUpdate.find {
                    it.id == currentPair.first.id
                }?.let {
                    Pair(it, currentPair.second.apply {
                        this.points = it.points.map {
                            LatLngGMap(it.first, it.second)
                        }
                        isVisible = !it.hidden
                        color = it.color.toColor(context)
                        width = it.lineWidth.toPixelSize(context).toFloat()
                    })
                }
            }

            //items to add to map
            val addedLines = newItemsToAdd.map { line ->
                googleMap.addPolyline(
                    PolylineOptions()
                        .addAll(line.points.map {
                            LatLngGMap(it.first, it.second)
                        })
                        .visible(!line.hidden)
                        .color(line.color.toColor(context))
                        .width(line.lineWidth.toPixelSize(context).toFloat())
                ).let {
                    Pair(line, it)
                }

            }

            this.polylineItems = updatedMarker + addedLines

        }

    }

    /**
     * Helper method to obtain BitmapDescriptor from resource.
     * Add compatibility with vector resources
     */
    fun getBitmap(context: Context, resId: Int, color: Color?): Bitmap? {
        val drawable: Drawable? = ContextCompat.getDrawable(context, resId)
            ?.let { if (color != null) it.mutate() else it }
        return drawable?.let {
            drawable.setBounds(
                0,
                0,
                drawable.intrinsicWidth,
                drawable.intrinsicHeight
            )

            color?.let {
                drawable.colorFilter = PorterDuffColorFilter(
                    color.toColor(context),
                    PorterDuff.Mode.MULTIPLY
                )
            }
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            drawable.draw(canvas)
            bitmap
        }
    }

    suspend fun getIcon(marker: SKMapVC.Marker, selected: Boolean): BitmapDescriptor? {
        val hash = marker.iconHash(selected)

        return when (marker) {
            is SKMapVC.IconMarker -> {
                memoryCache.get(hash)?.bitmapDescriptor?.apply {
                    MapLoggerView.d("icon : get icon bitmap ${marker.id} from cache with hash $hash")
                } ?: kotlin.run {
                    if (selected) {
                        getBitmap(context, marker.selectedIcon.res, null)?.let {
                            SKMapView.BitmapDescriptorContainer(it).let {
                                MapLoggerView.d("icon : put icon bitmap ${marker.id} in cache  with hash $hash")
                                memoryCache.put(hash, it)
                                it.bitmapDescriptor
                            }
                        }
                    } else {
                        getBitmap(context, marker.normalIcon.res, null)?.let {
                            SKMapView.BitmapDescriptorContainer(it).let {
                                MapLoggerView.d("icon : put icon bitmap ${marker.id} in cache  with hash $hash")
                                memoryCache.put(hash, it)
                                it.bitmapDescriptor
                            }
                        }
                    }
                }

            }
            is SKMapVC.ColorizedIconMarker -> {
                memoryCache.get(hash)?.bitmapDescriptor?.apply {
                    MapLoggerView.d("icon : get colorized bitmap ${marker.id} from cache with hash $hash")
                } ?: kotlin.run {
                    if (selected) {
                        getBitmap(context, marker.icon.res, marker.selectedColor)?.let {
                            SKMapView.BitmapDescriptorContainer(it).let {
                                MapLoggerView.d("icon : put colorized bitmap ${marker.id} in cache  with hash $hash")
                                memoryCache.put(hash, it)
                                it.bitmapDescriptor
                            }
                        }
                    } else {
                        getBitmap(context, marker.icon.res, marker.normalColor)?.let {
                            SKMapView.BitmapDescriptorContainer(it).let {
                                MapLoggerView.d("icon : put colorized bitmap ${marker.id} in cache  with hash $hash")
                                memoryCache.put(hash, it)
                                it.bitmapDescriptor
                            }
                        }
                    }
                }
            }
            is SKMapVC.CustomMarker -> {
                memoryCache.get(hash)?.bitmapDescriptor?.apply {
                    MapLoggerView.d("icon : get custom bitmap ${marker.id} from cache with hash $hash")
                } ?: kotlin.run {
                    onCreateCustomMarkerIcon?.invoke(marker, selected)?.let {
                        SKMapView.BitmapDescriptorContainer(it).let {
                            MapLoggerView.d("icon : put custom bitmap ${marker.id} in cache  with hash $hash")
                            memoryCache.put(hash, it)
                            it.bitmapDescriptor
                        }
                    }
                        ?: throw NoSuchFieldException("onCreateCustomMarkerIcon must not be null with CustomMarker")
                }
            }
        }
    }
}