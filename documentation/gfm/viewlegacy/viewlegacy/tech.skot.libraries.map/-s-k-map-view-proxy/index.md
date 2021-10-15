//[viewlegacy](../../../index.md)/[tech.skot.libraries.map](../index.md)/[SKMapViewProxy](index.md)

# SKMapViewProxy

[android]\
class [SKMapViewProxy](index.md)(onMarkerClick: ([SKMapVC.Marker](../../../../viewcontract/viewcontract/tech.skot.libraries.map/-s-k-map-v-c/-marker/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html), itemsInitial: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[SKMapVC.Marker](../../../../viewcontract/viewcontract/tech.skot.libraries.map/-s-k-map-v-c/-marker/index.md)&gt;, onMapClickedInitial: ([Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)&gt;) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)?, selectedMarkerInitial: [SKMapVC.Marker](../../../../viewcontract/viewcontract/tech.skot.libraries.map/-s-k-map-v-c/-marker/index.md)?) : SKComponentViewProxy&lt;MapView&gt; , [SKMapVC](../../../../viewcontract/viewcontract/tech.skot.libraries.map/-s-k-map-v-c/index.md)

## Types

| Name | Summary |
|---|---|
| [SetCameraPositionData](-set-camera-position-data/index.md) | [android]<br>data class [SetCameraPositionData](-set-camera-position-data/index.md)(pos: [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)&gt;, zoomLevel: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), animate: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |

## Functions

| Name | Summary |
|---|---|
| [_bindTo](index.md#-2023930412%2FFunctions%2F-2118544462) | [android]<br>fun [_bindTo](index.md#-2023930412%2FFunctions%2F-2118544462)(activity: SKActivity, fragment: [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html)?, binding: MapView, collectingObservers: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): SKComponentView&lt;MapView&gt; |
| [bindingOf](index.md#-218721014%2FFunctions%2F-2118544462) | [android]<br>open fun [bindingOf](index.md#-218721014%2FFunctions%2F-2118544462)(view: [View](https://developer.android.com/reference/kotlin/android/view/View.html)): MapView |
| [bindTo](bind-to.md) | [android]<br>open override fun [bindTo](bind-to.md)(activity: SKActivity, fragment: [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html)?, binding: MapView, collectingObservers: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [SKMapView](../-s-k-map-view/index.md) |
| [bindToItemView](index.md#1853397514%2FFunctions%2F-2118544462) | [android]<br>fun [bindToItemView](index.md#1853397514%2FFunctions%2F-2118544462)(activity: SKActivity, fragment: [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html)?, view: [View](https://developer.android.com/reference/kotlin/android/view/View.html)): SKComponentView&lt;MapView&gt; |
| [bindToView](index.md#-1886702137%2FFunctions%2F-2118544462) | [android]<br>fun [bindToView](index.md#-1886702137%2FFunctions%2F-2118544462)(activity: SKActivity, fragment: [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html)?, view: [View](https://developer.android.com/reference/kotlin/android/view/View.html), collectingObservers: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): SKComponentView&lt;MapView&gt; |
| [closeKeyboard](index.md#-1620891610%2FFunctions%2F-2118544462) | [android]<br>open override fun [closeKeyboard](index.md#-1620891610%2FFunctions%2F-2118544462)() |
| [displayErrorMessage](index.md#491242464%2FFunctions%2F-2118544462) | [android]<br>open override fun [displayErrorMessage](index.md#491242464%2FFunctions%2F-2118544462)(message: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)) |
| [inflate](index.md#-1312652885%2FFunctions%2F-2118544462) | [android]<br>open fun [inflate](index.md#-1312652885%2FFunctions%2F-2118544462)(layoutInflater: [LayoutInflater](https://developer.android.com/reference/kotlin/android/view/LayoutInflater.html), parent: [ViewGroup](https://developer.android.com/reference/kotlin/android/view/ViewGroup.html)?, attachToParent: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): MapView |
| [inflateAndBind](index.md#550086457%2FFunctions%2F-2118544462) | [android]<br>fun [inflateAndBind](index.md#550086457%2FFunctions%2F-2118544462)(activity: SKActivity, fragment: [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html)?): MapView |
| [inflateInParentAndBind](index.md#1955542503%2FFunctions%2F-2118544462) | [android]<br>fun [inflateInParentAndBind](index.md#1955542503%2FFunctions%2F-2118544462)(activity: SKActivity, fragment: [Fragment](https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html)?, parent: [ViewGroup](https://developer.android.com/reference/kotlin/android/view/ViewGroup.html)) |
| [onRemove](index.md#-700855470%2FFunctions%2F-2118544462) | [android]<br>open override fun [onRemove](index.md#-700855470%2FFunctions%2F-2118544462)() |
| [saveState](save-state.md) | [android]<br>open override fun [saveState](save-state.md)() |
| [setCameraPosition](set-camera-position.md) | [android]<br>open override fun [setCameraPosition](set-camera-position.md)(pos: [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)&gt;, zoomLevel: [Float](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-float/index.html), animate: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)) |

## Properties

| Name | Summary |
|---|---|
| [layoutId](index.md#-1846570256%2FProperties%2F-2118544462) | [android]<br>open val [layoutId](index.md#-1846570256%2FProperties%2F-2118544462): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)? |
| [markers](markers.md) | [android]<br>open override var [markers](markers.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[SKMapVC.Marker](../../../../viewcontract/viewcontract/tech.skot.libraries.map/-s-k-map-v-c/-marker/index.md)&gt; |
| [onMapClicked](on-map-clicked.md) | [android]<br>open override var [onMapClicked](on-map-clicked.md): ([Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)&lt;[Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html), [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)&gt;) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)? |
| [onMarkerClick](on-marker-click.md) | [android]<br>open override val [onMarkerClick](on-marker-click.md): ([SKMapVC.Marker](../../../../viewcontract/viewcontract/tech.skot.libraries.map/-s-k-map-v-c/-marker/index.md)) -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [selectedMarker](selected-marker.md) | [android]<br>open override var [selectedMarker](selected-marker.md): [SKMapVC.Marker](../../../../viewcontract/viewcontract/tech.skot.libraries.map/-s-k-map-v-c/-marker/index.md)? |
| [style](index.md#787741876%2FProperties%2F-2118544462) | [android]<br>open override var [style](index.md#787741876%2FProperties%2F-2118544462): Style? |