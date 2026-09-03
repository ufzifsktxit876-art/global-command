package com.globalcommand.game

import kotlinx.serialization.Serializable

@Serializable
data class GameDate(
    val year: Int = 1936,
    val month: Int = 1,
    val day: Int = 1,
    val hour: Int = 12
) {
    fun advanceOneHour(): GameDate {
        var newHour = hour + 1
        var newDay = day
        var newMonth = month
        var newYear = year
        if (newHour >= 24) {
            newHour = 0
            newDay += 1
            val daysInMonth = when (newMonth) {
                2 -> if ((newYear % 4 == 0 && newYear % 100 != 0) || (newYear % 400 == 0)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            if (newDay > daysInMonth) {
                newDay = 1
                newMonth += 1
                if (newMonth > 12) {
                    newMonth = 1
                    newYear += 1
                }
            }
        }
        return GameDate(newYear, newMonth, newDay, newHour)
    }

    fun formatted(): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val m = if (month in 1..12) months[month - 1] else "???"
        val h = if (hour < 10) "0$hour:00" else "$hour:00"
        return "$day $m $year - $h"
    }
}

@Serializable
enum class IdeologyGroup { DEMOCRATIC, COMMUNIST, FASCIST, NON_ALIGNED }

@Serializable
data class GovernmentInfo(
    val ideologyGroup: IdeologyGroup,
    val rulingPartyName: String,
    val rulingLeaderName: String
)

@Serializable
data class CountryData(
    val id: String,
    val historicalName: String,
    val commonName: String,
    val capitalProvinceId: Int,
    val government: GovernmentInfo,
    val colorHex: Long,
    val populationTotal: Long,
    val manpowerAvailable: Long,
    val politicalPower: Double = 100.0,
    val stabilityPercent: Double = 70.0,
    val warSupportPercent: Double = 50.0,
    val civilianFactories: Int = 10,
    val militaryFactories: Int = 10,
    val navalDockyards: Int = 0,
    val ownedProvinceIds: List<Int> = emptyList(),
    val controlledProvinceIds: List<Int> = emptyList(),
    val isMajorNation: Boolean = false
)

@Serializable
enum class TerrainType { PLAINS, FOREST, HILLS, MOUNTAINS, URBAN, MARSH, DESERT, JUNGLE, TUNDRA, COAST }

@Serializable
data class MapCoordinates(val x: Float, val y: Float)

@Serializable
data class ProvincePolygon(val vertices: List<MapCoordinates>)

@Serializable
data class ProvinceResources(
    val steel: Int = 0,
    val oil: Int = 0,
    val aluminium: Int = 0,
    val rubber: Int = 0,
    val tungsten: Int = 0,
    val chromium: Int = 0
)

@Serializable
data class ProvinceData(
    val id: Int,
    val name: String,
    val ownerCountryId: String,
    val controllerCountryId: String,
    val regionStateName: String,
    val terrain: TerrainType,
    val population: Long,
    val victoryPoints: Int,
    val infrastructureLevel: Int = 3,
    val civilianIndustry: Int = 0,
    val militaryIndustry: Int = 0,
    val dockyards: Int = 0,
    val portLevel: Int = 0,
    val airfieldLevel: Int = 0,
    val centerPoint: MapCoordinates,
    val polygon: ProvincePolygon,
    val resources: ProvinceResources = ProvinceResources(),
    val adjacentProvinceIds: List<Int> = emptyList()
)

@Serializable
enum class AutosaveInterval { DAILY, WEEKLY, MONTHLY, NEVER }

@Serializable
data class SettingsData(
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 1.0f,
    val autosaveInterval: AutosaveInterval = AutosaveInterval.MONTHLY
)

@Serializable
data class GameState(
    val saveVersion: Int = 1,
    val saveSlotName: String = "Autosave",
    val timestampMillis: Long = System.currentTimeMillis(),
    val scenarioId: String,
    val currentDate: GameDate,
    val simulationSpeed: Int = 1,
    val isPaused: Boolean = true,
    val playerCountryId: String,
    val countries: Map<String, CountryData>,
    val provinces: Map<Int, ProvinceData>,
    val selectedProvinceId: Int? = null,
    val selectedCountryId: String? = null,
    val tickCounter: Long = 0L
)
