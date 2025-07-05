package com.heard.mobile.ui.screens.pathDetail

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream


object GpxLoader {

    suspend fun loadPath(
        context: Context,
        mapView: MapView,
        idPercorso: String,
        imageViewIfNoGpx: ImageView
    ) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val doc = firestore.collection("Percorsi").document(idPercorso).get().await()

            val gpxText = doc.getString("Mappa")

            if (gpxText.isNullOrEmpty()) {
                mostraIconaDefault(context, imageViewIfNoGpx)
                return
            }

            val inputStream = ByteArrayInputStream(gpxText.toByteArray())

            val points = parseGpx(inputStream)

            if (points.isNotEmpty()) {
                val polyline = Polyline().apply {
                    setPoints(points)
                }

                mapView.overlays.clear()
                mapView.overlays.add(polyline)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(points.first())
                mapView.invalidate()

                imageViewIfNoGpx.setImageDrawable(null)
            } else {
                mostraIconaDefault(context, imageViewIfNoGpx)
            }

        } catch (e: Exception) {
            Log.e("GPXLoader", "Errore: ${e.message}")
            mostraIconaDefault(context, imageViewIfNoGpx)
        }
    }

    private fun mostraIconaDefault(context: Context, imageView: ImageView) {
        val icon: Drawable? = AppCompatResources.getDrawable(context, org.osmdroid.library.R.drawable.ic_menu_mapmode)
        imageView.setImageDrawable(icon)
    }

    private fun parseGpx(inputStream: InputStream): List<GeoPoint> {
        val geoPoints = mutableListOf<GeoPoint>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "trkpt") {
                    val lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                    val lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()

                    if (lat != null && lon != null) {
                        geoPoints.add(GeoPoint(lat, lon))
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("GPXManualParser", "Errore parsing: ${e.message}")
        }
        return geoPoints
    }

}
