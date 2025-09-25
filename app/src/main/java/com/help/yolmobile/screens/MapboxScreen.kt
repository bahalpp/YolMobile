package com.help.yolmobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.help.yolmobile.data.Dukkan
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlin.math.*

// Çok nokta (100+) senaryosu için: Her nokta için ViewAnnotation performansı düşürebilir.
// Şimdilik isteğin doğrultusunda tüm dükkanları ViewAnnotation ile gösteriyoruz.
private val fakeDukkanListesi: List<Dukkan> = buildList {
    add(Dukkan(1, "Ankara Kalesi Manzara Cafe", 39.9415, 32.8640))
    add(Dukkan(2, "İzmir Saat Kulesi Büfe", 38.4189, 27.1287))
    add(Dukkan(3, "Galata Köprüsü Balıkçısı, İstanbul", 41.0185, 28.9741))
    add(Dukkan(4, "Kapadokya Balon Noktası", 38.6431, 34.8285))

}

@Composable
private fun DukkanAnnotationBubble(dukkan: Dukkan, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFF0D47A1) else Color.White
    val textColor = if (selected) Color.White else Color.Black
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, Color(0xFF444444), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = dukkan.ad,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController) {
    val dukkanlar = remember { fakeDukkanListesi }
    var selectedDukkanId by remember { mutableStateOf<Int?>(null) }

    Scaffold { paddingValues ->
        MapboxMap(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
            mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(28.979530, 41.015137))
                    zoom(5.8)
                }
            }
        ) {
            dukkanlar.forEach { dukkan ->
                ViewAnnotation(
                    options = viewAnnotationOptions {
                        geometry(Point.fromLngLat(dukkan.longitude, dukkan.latitude))
                        annotationAnchor {
                            anchor(ViewAnnotationAnchor.BOTTOM)
                            offsetY(-8.0)
                        }
                        allowOverlap(true)
                    }
                ) {
                    DukkanAnnotationBubble(
                        dukkan = dukkan,
                        selected = selectedDukkanId == dukkan.id,
                        onClick = {
                            selectedDukkanId = if (selectedDukkanId == dukkan.id) null else dukkan.id
                        }
                    )
                }
            }
        }
    }
}