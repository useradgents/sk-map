package tech.skot.libraries.map.di

import tech.skot.core.di.InjectorMock
import tech.skot.core.di.module
import tech.skot.libraries.map.*
import tech.skot.libraries.map.view.Permissions

class SKMapViewInjectorMock : SKMapViewInjector {
    override fun sKMap(
        mapInteractionSettingsInitial: SKMapVC.MapInteractionSettings,
        markersInitial: List<SKMapVC.Marker>,
        linesInitial: List<SKMapVC.Polyline>,
        polygonsInitial: List<SKMapVC.Polygon>,
        selectedMarkerInitial: SKMapVC.Marker?,
        selectMarkerOnClickInitial: Boolean,
        unselectMarkerOnMapClickInitial: Boolean,
        onMarkerClickInitial: ((SKMapVC.Marker) -> Unit)?,
        onMarkerSelectedInitial: ((SKMapVC.Marker?) -> Unit)?,
        onMapClickedInitial: ((LatLng) -> Unit)?,
        onMapLongClickedInitial: ((LatLng) -> Unit)?,
        onMapBoundsChangeInitial: ((SKMapVC.LatLngBounds) -> Unit)?,
        showLogInitial: Boolean,
        mapTypeInitial: MapType
    ): SKMapVC {
        return SKMapViewMock(
            mapInteractionSettingsInitial,
            markersInitial,
            linesInitial,
            polygonsInitial,
            selectedMarkerInitial,
            selectMarkerOnClickInitial,
            unselectMarkerOnMapClickInitial,
            onMarkerClickInitial,
            onMapClickedInitial,
            onMapLongClickedInitial,
            onMarkerSelectedInitial,
            onMapBoundsChangeInitial,
            showLogInitial,
            mapTypeInitial
        )
    }

}

val skMapModuleMock = module<InjectorMock> {
    single<SKMapViewInjector> { SKMapViewInjectorMock() }
    single<Permissions> { PermissionsMock() }
    single<DeclaredPermissionHelper> { DeclaredPermissionHelperMock() }

}