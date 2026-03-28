import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.TileOverlay

/**
 * The maximum bound for Web Mercator (EPSG:3857) projection when the world is cropped to square,
 * which is the case for GoogleMap
 */
public const val GMAPS_WMS_BOUND: Double = 20037508.34

/**
 * A [TileOverlay] for a tile overlay of WMS layers using the EPSG:3857 projection and square bounding boxes.
 *
 * @param wmsDataSetXMinBound : the minimum X bound of the dataset in meters. Defaults to -[GMAPS_WMS_BOUND].
 * @param wmsDataSetXMaxBound the maximum X bound of the dataset in meters. Defaults to [GMAPS_WMS_BOUND].
 * @param wmsDataSetYMinBound the minimum Y bound of the dataset in meters. Defaults to -[GMAPS_WMS_BOUND].
 * @param wmsDataSetYMaxBound the maximum Y bound of the dataset in meters. Defaults to [GMAPS_WMS_BOUND].
 * @param tileSize the size of the tiles in pixels. Defaults to 256.
 * @param urlFormatter a lambda that returns the WMS URL for a given bounding box.
 * @param state the [TileOverlayState] to be used to control or observe the tile overlay state.
 * @param fadeIn boolean indicating whether the tiles should fade in.
 * @param transparency the transparency of the tile overlay.
 * @param visible the visibility of the tile overlay.
 * @param zIndex the z-index of the tile overlay.
 * @param onClick lambda invoked when the tile overlay is clicked.
 */
@Composable
@GoogleMapComposable
public fun WmsTileOverlayEpsg3857(
    wmsDataSetXMinBound: Double = -GMAPS_WMS_BOUND,
    wmsDataSetXMaxBound: Double = GMAPS_WMS_BOUND,
    wmsDataSetYMinBound: Double = -GMAPS_WMS_BOUND,
    wmsDataSetYMaxBound: Double = GMAPS_WMS_BOUND,
    tileSize: Int = 256,
    urlFormatter: (
        xMinBound: Double,
        yMinBound: Double,
        xMaxBound: Double,
        yMaxBound: Double
    ) -> String,
    state: TileOverlayState = rememberTileOverlayState(),
    fadeIn: Boolean = true,
    transparency: Float = 0f,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onClick: (TileOverlay) -> Unit = {},
) {
    val wmsUrlTileProvider = remember(
        wmsDataSetXMinBound,
        wmsDataSetXMaxBound,
        wmsDataSetYMinBound,
        wmsDataSetYMaxBound,
        tileSize,
        urlFormatter
    ) {
        WmsUrlTileProviderEpsg3857(
            datasetXMinBound = wmsDataSetXMinBound,
            datasetXMaxBound = wmsDataSetXMaxBound,
            datasetYMinBound = wmsDataSetYMinBound,
            datasetYMaxBound = wmsDataSetYMaxBound,
            tileSize = tileSize,
            urlFormatter = urlFormatter
        )
    }
    TileOverlay(
        tileProvider = wmsUrlTileProvider,
        state = state,
        fadeIn = fadeIn,
        transparency = transparency,
        visible = visible,
        zIndex = zIndex,
        onClick = onClick,
    )
}

// An abstraction that simplifies making the urlFormatter lambda.
// Just specify whether your API uses x-first (most common) or y-first ordering.
public fun urlFormatterFactory(url: String, firstCoor: Char): ((Double, Double, Double, Double) -> String) =
    when(firstCoor) {
        'x' -> { xMinBound, yMinBound, xMaxBound, yMaxBound ->
            "$url&BBOX=$xMinBound,$yMinBound,$xMaxBound,$yMaxBound"
        }

        'y' -> { xMinBound, yMinBound, xMaxBound, yMaxBound ->
            "$url&BBOX=$yMinBound,$xMinBound,$yMaxBound,$xMaxBound"
        }
        else -> { xMinBound, yMinBound, xMaxBound, yMaxBound ->
                "Coordinate ordering was incorrectly specified for urlFormatterFactory. Lower-case x or y Char needed."
        }
    }
   