package com.example.mapbox

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mapbox.ui.theme.MapboxExampleTheme
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptionsManager
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resourceOptions = ResourceOptionsManager
            .getDefault(this, getMapboxKey())
            .resourceOptions
        val mapInitOptions = MapInitOptions(this, resourceOptions = resourceOptions)
        val mapView = MapView(this, mapInitOptions)
        val annotationApi = mapView.annotations

        // Create a circle
        val circleAnnotationManager = annotationApi.createCircleAnnotationManager()
        val circleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(Point.fromLngLat(0.0, 0.0))
            .withCircleRadius(30.0)
            .withCircleColor("Red")
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor("Black")
            .withDraggable(true)
        circleAnnotationManager.create(circleAnnotationOptions)

        // Create a Polyline
        val polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()
        val polylinePoints = listOf(
            Point.fromLngLat(0.0, 0.0),
            Point.fromLngLat(17.94, 59.25),
            Point.fromLngLat(18.18, 59.37),
        )
        val polylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(polylinePoints)
            .withLineColor("#ee4e8b")
            .withLineWidth(5.0)
        polylineAnnotationManager.create(polylineAnnotationOptions)

        // Create a Polygon
        val polygonAnnotationConfig =
            AnnotationConfig(layerId = "polygonLayer", belowLayerId = "symbolLayer")
        val polygonAnnotationManager =
            annotationApi.createPolygonAnnotationManager(polygonAnnotationConfig)
        val points = listOf(
            listOf(
                Point.fromLngLat(17.94, 39.25),
                Point.fromLngLat(28.18, 39.25),
                Point.fromLngLat(28.18, 59.37),
                Point.fromLngLat(17.94, 59.37)
            )
        )
        val polygonAnnotationOptions = PolygonAnnotationOptions()
            .withPoints(points)
            .withFillColor("#ee4e8b")
            .withFillOpacity(1.0)
            .withFillSortKey(0.0)
            .withDraggable(true)
        polygonAnnotationManager.create(polygonAnnotationOptions)

        // Create a symbol
        val markerIcon = bitmapFromDrawableRes(this, androidx.core.R.drawable.ic_call_answer)
        val symbolAnnotationConfig =
            AnnotationConfig(layerId = "symbolLayer")
        val pointAnnotationManager =
            annotationApi.createPointAnnotationManager(symbolAnnotationConfig)
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(18.06, 59.31))
            .withIconImage(markerIcon!!)
            .withIconSize(5.0)
            .withSymbolSortKey(1.0)
            .withDraggable(true)
        pointAnnotationManager.create(pointAnnotationOptions)

        setContent {
            MapboxExampleTheme {
                val coroutineScope = rememberCoroutineScope()

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { mapView },
                    update = { mapView ->
                        coroutineScope.launch {
                            mapView.getMapboxMap().apply {
                                loadStyleUri(Style.MAPBOX_STREETS)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(context.getDrawable(resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun getMapboxKey(): String {
        val key = resources.getString(R.string.mapboxKey)

        if (key.isEmpty()) {
            throw RuntimeException(
                "Failed to find mapboxKey. Did you define 'MAPBOX_API_KEY' in keystore.properties?"
            )
        }

        return key
    }
}
