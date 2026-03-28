import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.UrlTileProvider
import java.net.URL

/**
 * The maximum bound for Web Mercator (EPSG:3857) projection when the world is cropped to square,
 * which is the case for GoogleMap
 */
public const val GMAPS_WMS_BOUND: Double = 20037508.34

/**
 * A [UrlTileProvider] for WMS layers using the EPSG:3857 projection and square bounding boxes.
 *
 * @param datasetXMinBound : the minimum X bound of the dataset in meters. Defaults to -[GMAPS_WMS_BOUND].
 * @param datasetXMaxBound : the maximum X bound of the dataset in meters. Defaults to [GMAPS_WMS_BOUND].
 * @param datasetYMinBound : the minimum Y bound of the dataset in meters. Defaults to -[GMAPS_WMS_BOUND].
 * @param datasetYMaxBound : the maximum Y bound of the dataset in meters. Defaults to [GMAPS_WMS_BOUND].
 * @param tileSize : the size of the tiles in pixels. Defaults to 256.
 * @param urlFormatter : a lambda that returns the WMS URL for a given bounding box.
 */
@Immutable
public class WmsUrlTileProviderEpsg3857(
    private val datasetXMinBound: Double = -GMAPS_WMS_BOUND,
    private val datasetXMaxBound: Double = GMAPS_WMS_BOUND,
    private val datasetYMinBound: Double = -GMAPS_WMS_BOUND,
    private val datasetYMaxBound: Double = GMAPS_WMS_BOUND,
    tileSize: Int = 256,
    private val urlFormatter: (
        xMinBound: Double,
        yMinBound: Double,
        xMaxBound: Double,
        yMaxBound: Double
    ) -> String
): UrlTileProvider(tileSize, tileSize) {
    private val alteredBounds: Boolean = datasetXMinBound != -GMAPS_WMS_BOUND || datasetXMaxBound != GMAPS_WMS_BOUND || datasetYMinBound != -GMAPS_WMS_BOUND || datasetYMaxBound != GMAPS_WMS_BOUND

    override fun getTileUrl(x: Int, y: Int, zoomLevel: Int): URL? {
        val dimensionsBBOX: Double = getZoomLevelBoundingBoxDimensions(zoomLevel)
        val resolutionXY: Int = getZoomLevelXYResolution(zoomLevel)
        val xMin: Double =
            getBoundingBoxXMin(dimensionsBBOX = dimensionsBBOX, resolutionXY = resolutionXY, x = x)
        val yMin: Double =
            getBoundingBoxYMin(dimensionsBBOX = dimensionsBBOX, resolutionXY = resolutionXY, y = y)
        
        // If no pixels are in the bounding box, return null
        if (alteredBounds && (xMin > datasetXMaxBound || xMin + dimensionsBBOX < datasetXMinBound ||
                yMin > datasetYMaxBound || yMin + dimensionsBBOX < datasetYMinBound)) {
            return null
        }
        
        return try {
            URL(urlFormatter(xMin, yMin, xMin + dimensionsBBOX, yMin + dimensionsBBOX))
        } catch (_: Exception) {
            null
        }
    }
}

private const val SHIFT_BIT: Int = 1
private const val Y_INVERT_ALIGNER: Int = 1

// Divides distance from center to edge by how many tiles there are from center to edge at that zoom level. Thus finding the meter-dimensions of a tile. 
private fun getZoomLevelBoundingBoxDimensions(zoom: Int): Double {
    return if (zoom == 0) {
        2 * GMAPS_WMS_BOUND
    } else {
        GMAPS_WMS_BOUND / (SHIFT_BIT shl (zoom - 1))
    }
}

// Returns how many tiles across there are for a given zoom level.
private fun getZoomLevelXYResolution(zoom: Int): Int {
    return (SHIFT_BIT shl zoom)
}

// For the bounding box the strategy chosen is finding the x-coordinate of the west edge and the y-coordinate of the south edge. 

// West edge
private fun getBoundingBoxXMin(dimensionsBBOX: Double, resolutionXY: Int, x: Int): Double {
    return if (resolutionXY == 1) {
        -GMAPS_WMS_BOUND
    } else {
        (x - (resolutionXY / 2)) * dimensionsBBOX
    }
}

// South edge
private fun getBoundingBoxYMin(dimensionsBBOX: Double, resolutionXY: Int, y: Int): Double {
    return if (resolutionXY == 1) {
        -GMAPS_WMS_BOUND
    } else {
        ((resolutionXY / 2) - (y + Y_INVERT_ALIGNER)) * dimensionsBBOX
    }
}
