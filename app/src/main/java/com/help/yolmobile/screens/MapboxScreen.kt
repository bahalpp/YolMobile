package com.help.yolmobile.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.help.yolmobile.data.Dukkan
import com.help.yolmobile.network.RetrofitInstance // API instance'ı
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.launch

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
            // Dukkan data class'ında isim alanı "isim" olarak güncellenmişti.
            text = dukkan.isim, 
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
    var dukkanlarState by remember { mutableStateOf<List<Dukkan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedDukkanId by remember { mutableStateOf<Int?>(null) }

    // API'den verileri çekmek için LaunchedEffect
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val responseBody = RetrofitInstance.api.getDukkanlar()
            val jsonString = responseBody.string()
            val fetchedDukkanlar = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }.decodeFromString<List<Dukkan>>(jsonString)
            dukkanlarState = fetchedDukkanlar
            Log.d("MapScreen", "Dükkanlar başarıyla alındı: ${fetchedDukkanlar.size} adet")
        } catch (e: Exception) {
            errorMessage = "Veriler yüklenirken bir hata oluştu: ${e.localizedMessage ?: e.message ?: e.toString()}"
            Log.e("MapScreen", "API çağrısı hatası: ${e.localizedMessage ?: e.message}")
            e.printStackTrace() // Hata detayını logcat'te göster
        } finally {
            isLoading = false
        }
    }

    Scaffold {
        paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)){
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        // Türkiye'yi ortalayacak genel bir başlangıç noktası ve zoom
                        center(Point.fromLngLat(35.0, 39.0)) 
                        zoom(4.5)
                    }
                }
            ) {
                if (!isLoading && errorMessage == null) {
                    dukkanlarState.forEach { dukkan ->
                        // Dukkan data class'ındaki latitude ve longitude alanlarını kullanıyoruz
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
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
        }
    }
}
