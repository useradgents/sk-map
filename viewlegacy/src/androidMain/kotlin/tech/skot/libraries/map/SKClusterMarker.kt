package tech.skot.libraries.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class SKClusterMarker(var marker: SKMapVC.Marker, var selected : Boolean) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(marker.position.first, marker.position.second)
    }

    override fun getTitle(): String? {
        return null
    }

    override fun getSnippet(): String? {
        return null
    }


}