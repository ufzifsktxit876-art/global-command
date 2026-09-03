package com.globalcommand.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val BrassGold = Color(0xFFD4AF37)
val TextPrimary = Color(0xFFF1F5F9)

enum class MapMode { POLITICAL, TERRAIN, INFRASTRUCTURE, VP }

@Composable
fun InteractiveMap(state: GameState, mode: MapMode, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1E28))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.4f, 5f)
                    offset += pan
                }
            }
            .pointerInput(state) {
                detectTapGestures { tap ->
                    val wx = (tap.x - offset.x) / scale
                    val wy = (tap.y - offset.y) / scale
                    state.provinces.values.firstOrNull { prov ->
                        val poly = prov.polygon.vertices
                        var inside = false
                        var j = poly.size - 1
                        for (i in poly.indices) {
                            if ((poly[i].y > wy) != (poly[j].y > wy) &&
                                (wx < (poly[j].x - poly[i].x) * (wy - poly[i].y) / (poly[j].y - poly[i].y + 0.0001f) + poly[i].x)
                            ) inside = !inside
                            j = i
                        }
                        inside
                    }?.let { onSelect(it.id) }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (p in state.provinces.values) {
                val poly = p.polygon.vertices
                if (poly.isEmpty()) continue
                val path = Path().apply {
                    moveTo(poly[0].x * scale + offset.x, poly[0].y * scale + offset.y)
                    for (i in 1 until poly.size) lineTo(poly[i].x * scale + offset.x, poly[i].y * scale + offset.y)
                    close()
                }
                val color = when (mode) {
                    MapMode.POLITICAL -> state.countries[p.controllerCountryId]?.let { Color(it.colorHex) } ?: Color.Gray
                    MapMode.TERRAIN -> if (p.terrain == TerrainType.URBAN) Color(0xFF707070) else Color(0xFF557A46)
                    MapMode.INFRASTRUCTURE -> Color(1f - p.infrastructureLevel / 10f, p.infrastructureLevel / 10f, 0.2f)
                    MapMode.VP -> Color(p.victoryPoints / 50f, 0.3f, 1f - p.victoryPoints / 50f)
                }
                drawPath(path, color)
                val isSel = p.id == state.selectedProvinceId
                drawPath(path, if (isSel) Color(0xFFFFD700) else Color(0xFF1E293B), style = Stroke(if (isSel) 4f * scale else 1.2f * scale))
            }
        }
    }
}
