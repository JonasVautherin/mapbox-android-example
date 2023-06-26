package com.example.mapbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mapbox.ui.theme.MapboxExampleTheme
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptionsManager
import com.mapbox.maps.Style
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MapboxExampleTheme {
                val resourceOptions = ResourceOptionsManager.getDefault(
                    LocalContext.current,
                    getMapboxKey()
                ).resourceOptions

                val mapInitOptions = MapInitOptions(
                    LocalContext.current,
                    resourceOptions = resourceOptions
                )

                val mapView = MapView(LocalContext.current, mapInitOptions)
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
