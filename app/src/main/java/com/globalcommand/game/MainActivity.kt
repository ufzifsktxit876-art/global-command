package com.globalcommand.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var engine: GameEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val saveRepo = SaveGameRepository(applicationContext)
        val initialState = HistoricalScenarioRepository.createInitialGameState("1936_road_to_war", "GER")
        engine = GameEngine(initialState, saveRepo)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Slate900) {
                    val currentEngine = engine ?: return@Surface
                    val state by currentEngine.gameState.collectAsState()
                    var mapMode by remember { mutableStateOf(MapMode.POLITICAL) }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // نوار فرمان بالا
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${state.playerCountryId} | PP: ${state.countries[state.playerCountryId]?.politicalPower?.toInt() ?: 100}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(state.currentDate.formatted(), color = BrassGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { currentEngine.togglePause() }, modifier = Modifier.size(28.dp)) {
                                    Icon(if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = if (state.isPaused) Color.Red else Color.Green)
                                }
                                for (spd in 1..5) {
                                    Box(
                                        modifier = Modifier
                                            .background(if (state.simulationSpeed == spd) BrassGold else Color.DarkGray, RoundedCornerShape(2.dp))
                                            .clickable { currentEngine.setSpeed(spd) }
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text("$spd", fontSize = 10.sp, color = if (state.simulationSpeed == spd) Color.Black else Color.White)
                                    }
                                }
                            }
                        }

                        // نقشه و انتخاب مود
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            InteractiveMap(state = state, mode = mapMode, onSelect = { currentEngine.selectProvince(it) })

                            Row(modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                MapMode.values().forEach { m ->
                                    Button(
                                        onClick = { mapMode = m },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (mapMode == m) BrassGold else Color(0xAA1E293B)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(m.name, fontSize = 9.sp, color = if (mapMode == m) Color.Black else Color.White)
                                    }
                                }
                            }

                            // شیت استان انتخاب شده
                            val sel = state.selectedProvinceId?.let { state.provinces[it] }
                            if (sel != null) {
                                Card(
                                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Slate800)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(sel.name, color = BrassGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("Owner: ${sel.ownerCountryId} | Terrain: ${sel.terrain}", color = Color.LightGray, fontSize = 11.sp)
                                        }
                                        Text("VP: ${sel.victoryPoints} | Infra: ${sel.infrastructureLevel}/10", color = TextPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        engine?.shutdown()
    }
}
