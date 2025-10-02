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
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.serialization.json.Json
import androidx.compose.ui.zIndex
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.HorizontalDivider

// Tek bir Json örneği yeniden kullanım için
private val jsonFormat = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
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
@Suppress("UNUSED_PARAMETER")
@Composable
fun MapScreen(navController: NavController) {
    var dukkanlarState by remember { mutableStateOf<List<Dukkan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedDukkanId by remember { mutableStateOf<Int?>(null) }
    var selectedKategori by remember { mutableStateOf("motosiklet tamir") }
    // Başlangıçta en azından varsayılan kategori olsun, böylece dropdown boş olmaz
    val kategoriListesi = remember { mutableStateListOf("motosiklet tamir") }

    // expandedTop artık harita üstündeki overlay için kullanılıyor
    var expandedTop by remember { mutableStateOf(false) }

    // API'den verileri çekmek için LaunchedEffect
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val responseBody = RetrofitInstance.api.getDukkanlar()
            val jsonString = responseBody.string()
            val fetchedDukkanlar = jsonFormat.decodeFromString<List<Dukkan>>(jsonString)
            dukkanlarState = fetchedDukkanlar
            // API geldikten sonra kategorileri güncelle
            val newKategoriler = fetchedDukkanlar.map { it.kategori }.map { it.trim() }.filter { it.isNotBlank() }.distinct()
            if (newKategoriler.isNotEmpty()) {
                kategoriListesi.clear()
                kategoriListesi.addAll(newKategoriler)
                // Eğer seçili kategori API listesinde yoksa ilk kategoriyi seç
                if (!kategoriListesi.any { it.equals(selectedKategori, ignoreCase = true) }) {
                    selectedKategori = kategoriListesi.first()
                } else {
                    val matched = kategoriListesi.firstOrNull { it.equals(selectedKategori, ignoreCase = true) }
                    if (matched != null) selectedKategori = matched
                }
            }
            Log.d("MapScreen", "Dükkanlar başarıyla alındı: ${fetchedDukkanlar.size} adet")
        } catch (e: Exception) {
            errorMessage = "Veriler yüklenirken bir hata oluştu: ${e.localizedMessage ?: e.message ?: e.toString()}"
            Log.e("MapScreen", "API çağrısı hatası: ${e.localizedMessage ?: e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Basit Scaffold, kategori seçici harita üstünde overlay olacak
    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Harita
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        center(Point.fromLngLat(35.0, 39.0))
                        zoom(4.5)
                    }
                }
            ) {
                dukkanlarState.filter { dukkan ->
                    dukkan.kategori.trim().equals(selectedKategori.trim(), ignoreCase = true)
                }.forEach { dukkan ->
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            // Noktanın konumu
                            geometry(Point.fromLngLat(dukkan.longitude, dukkan.latitude))
                        }
                    ) {
                        DukkanAnnotationBubble(
                            dukkan = dukkan,
                            selected = selectedDukkanId == dukkan.id,
                            onClick = { selectedDukkanId = dukkan.id }
                        )
                    }
                }
            }

            // Harita üstünde kayan kategori seçici (üst ortada)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .zIndex(5f)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
                        .clickable {
                            expandedTop = true
                            Log.d("MapScreen", "Kategori dialogu açıldı (harita üstü)")
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedKategori, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("▾", color = Color.Gray)
                    }
                }

                // Dialog tabanlı kategori seçici
                if (expandedTop) {
                    Dialog(onDismissRequest = { expandedTop = false }) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Kategori Seç", fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                                HorizontalDivider()
                                if (kategoriListesi.isEmpty()) {
                                    Text(selectedKategori, modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedTop = false
                                            Log.d("MapScreen", "Kategori seçildi (fallback): $selectedKategori")
                                        }
                                        .padding(12.dp))
                                } else {
                                    kategoriListesi.forEach { kategori ->
                                        Box(modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedKategori = kategori
                                                expandedTop = false
                                                Log.d("MapScreen", "Kategori seçildi: $kategori")
                                            }
                                            .padding(12.dp)
                                        ) {
                                            Text(kategori)
                                        }
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // Marker tıklanınca dükkan bilgisi göster
            val selectedDukkan = dukkanlarState.find { it.id == selectedDukkanId }
            if (selectedDukkan != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(selectedDukkan.isim, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Kategori: ${selectedDukkan.kategori}", fontSize = 14.sp)
                        // Diğer dükkan bilgileri eklenebilir
                    }
                }
            }

            // Yükleniyor veya hata mesajı
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage?.let {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    Text(it, color = Color.Red)
                }
            }
        }
    }
}
