package com.globalcommand.game

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

data class HistoricalScenarioMeta(
    val id: String,
    val title: String,
    val subtitle: String,
    val startDate: GameDate,
    val description: String
)

object HistoricalScenarioRepository {
    val scenarios = listOf(
        HistoricalScenarioMeta("1936_road_to_war", "1936: Road to War", "The Verge of Rearmament", GameDate(1936, 1, 1, 12), "The League of Nations faces crises. Nations mobilize."),
        HistoricalScenarioMeta("1939_europe_at_war", "1939: Europe at War", "Blitzkrieg in the West", GameDate(1939, 9, 1, 6), "Germany invades Poland, triggering declarations of war."),
        HistoricalScenarioMeta("1941_global_conflict", "1941: Global Conflict", "Barbarossa and Pacific", GameDate(1941, 6, 22, 4), "Operation Barbarossa begins. Pacific tension rises."),
        HistoricalScenarioMeta("1943_turning_point", "1943: Turning Point", "The Tide Shifts", GameDate(1943, 7, 5, 5), "Allied armies contest the initiative on every front."),
        HistoricalScenarioMeta("1945_final_campaign", "1945: Final Campaign", "Twilight of Dictators", GameDate(1945, 1, 1, 0), "Allied armies cross the Rhine and Oder towards Berlin.")
    )

    private fun createPoly(cx: Float, cy: Float, r: Float = 40f): ProvincePolygon {
        val list = mutableListOf<MapCoordinates>()
        for (i in 0 until 6) {
            val a = (2.0 * Math.PI * i / 6)
            list.add(MapCoordinates((cx + r * Math.cos(a)).toFloat(), (cy + r * Math.sin(a)).toFloat()))
        }
        return ProvincePolygon(list)
    }

    fun createInitialGameState(scenarioId: String, playerCountryId: String): GameState {
        val sc = scenarios.firstOrNull { it.id == scenarioId } ?: scenarios.first()
        val provinces = mutableListOf(
            ProvinceData(1, "Berlin", "GER", "GER", "Brandenburg", TerrainType.URBAN, 4300000, 50, 9, 6, 5, 0, 0, 5, MapCoordinates(1350f, 650f), createPoly(1350f, 650f, 44f), ProvinceResources(steel = 45)),
            ProvinceData(2, "Hamburg", "GER", "GER", "Hannover", TerrainType.PLAINS, 1800000, 25, 8, 0, 0, 4, 8, 0, MapCoordinates(1270f, 630f), createPoly(1270f, 630f, 38f), ProvinceResources(steel = 20)),
            ProvinceData(10, "London", "ENG", "ENG", "Greater London", TerrainType.URBAN, 8200000, 50, 9, 8, 4, 5, 9, 6, MapCoordinates(1110f, 660f), createPoly(1110f, 660f, 44f), ProvinceResources(steel = 35)),
            ProvinceData(20, "Moscow", "SOV", "SOV", "Moscow Oblast", TerrainType.URBAN, 4100000, 50, 8, 8, 7, 0, 0, 5, MapCoordinates(1700f, 560f), createPoly(1700f, 560f, 48f), ProvinceResources(oil = 65, steel = 40)),
            ProvinceData(30, "Washington D.C.", "USA", "USA", "D.C.", TerrainType.URBAN, 660000, 50, 9, 7, 0, 0, 0, 5, MapCoordinates(520f, 700f), createPoly(520f, 700f, 42f), ProvinceResources(oil = 120, steel = 90)),
            ProvinceData(40, "Paris", "FRA", "FRA", "Île-de-France", TerrainType.URBAN, 2800000, 50, 8, 7, 4, 0, 0, 0, MapCoordinates(1180f, 740f), createPoly(1180f, 740f, 44f), ProvinceResources(steel = 30)),
            ProvinceData(50, "Tokyo", "JAP", "JAP", "Kanto", TerrainType.URBAN, 6300000, 50, 8, 7, 6, 6, 9, 0, MapCoordinates(2600f, 750f), createPoly(2600f, 750f, 45f), ProvinceResources(steel = 15))
        ).associateBy { it.id }

        val countries = listOf(
            CountryData("GER", "Deutsches Reich", "Germany", 1, GovernmentInfo(IdeologyGroup.FASCIST, "NSDAP", "Adolf Hitler"), 0xFF383838L, 67000000, 1850000, civilianFactories = 35, militaryFactories = 28, isMajorNation = true),
            CountryData("ENG", "United Kingdom", "United Kingdom", 10, GovernmentInfo(IdeologyGroup.DEMOCRATIC, "Conservative", "Winston Churchill"), 0xFF9E2A2BL, 47000000, 900000, civilianFactories = 38, militaryFactories = 18, isMajorNation = true),
            CountryData("SOV", "Soviet Union", "Soviet Union", 20, GovernmentInfo(IdeologyGroup.COMMUNIST, "CPSU", "Iosif Stalin"), 0xFF8A1C1CL, 168000000, 4200000, civilianFactories = 42, militaryFactories = 32, isMajorNation = true),
            CountryData("USA", "United States", "United States", 30, GovernmentInfo(IdeologyGroup.DEMOCRATIC, "Democratic", "Franklin D. Roosevelt"), 0xFF1D3557L, 128000000, 2100000, civilianFactories = 90, militaryFactories = 12, isMajorNation = true),
            CountryData("FRA", "France", "France", 40, GovernmentInfo(IdeologyGroup.DEMOCRATIC, "Front Populaire", "Albert Lebrun"), 0xFF457B9DL, 41500000, 850000, civilianFactories = 28, militaryFactories = 14, isMajorNation = true),
            CountryData("JAP", "Japan", "Japan", 50, GovernmentInfo(IdeologyGroup.FASCIST, "Taisei Yokusankai", "Hirohito"), 0xFFDDA15EL, 71000000, 1400000, civilianFactories = 26, militaryFactories = 20, isMajorNation = true)
        ).associateBy { it.id }

        return GameState(
            scenarioId = sc.id,
            currentDate = sc.startDate,
            playerCountryId = playerCountryId,
            countries = countries,
            provinces = provinces,
            selectedCountryId = playerCountryId
        )
    }
}

data class SaveFileInfo(val fileName: String, val saveName: String, val scenarioId: String, val playerCountryId: String, val gameDateFormatted: String, val timestampMillis: Long)

class SaveGameRepository(private val context: Context) {
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }
    private val dir: File get() = File(context.filesDir, "saves").apply { if (!exists()) mkdirs() }

    suspend fun saveGame(slot: String, state: GameState): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(dir, "${slot.replace(Regex("[^a-zA-Z0-9_]"), "_")}.gcsave")
            val tmp = File(dir, "${file.name}.tmp")
            FileOutputStream(tmp).use { it.write(json.encodeToString(state).toByteArray()) }
            if (file.exists()) file.delete()
            tmp.renameTo(file)
            Result.success(file.absolutePath)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun loadGame(fileName: String): Result<GameState> = withContext(Dispatchers.IO) {
        try { Result.success(json.decodeFromString<GameState>(File(dir, fileName).readText())) }
        catch (e: Exception) { Result.failure(e) }
    }

    suspend fun listSaves(): List<SaveFileInfo> = withContext(Dispatchers.IO) {
        dir.listFiles { f -> f.extension == "gcsave" }?.mapNotNull { f ->
            try {
                val st = json.decodeFromString<GameState>(f.readText())
                SaveFileInfo(f.name, st.saveSlotName, st.scenarioId, st.playerCountryId, st.currentDate.formatted(), st.timestampMillis)
            } catch (_: Exception) { null }
        }?.sortedByDescending { it.timestampMillis } ?: emptyList()
    }

    suspend fun deleteSave(fileName: String): Boolean = withContext(Dispatchers.IO) { File(dir, fileName).delete() }
}

class SettingsRepository(private val context: Context) {
    private val file = File(context.filesDir, "settings.json")
    private val json = Json { prettyPrint = true }
    private val _settings = MutableStateFlow(SettingsData())
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    fun updateSettings(s: SettingsData) {
        _settings.value = s
        try { file.writeText(json.encodeToString(s)) } catch (_: Exception) {}
    }
}
