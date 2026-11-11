package com.valterc.ki2.data.switches

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class WirelessSwitchesInfoTest {

    @Test
    fun `lowest known level is independent of switch order`() {
        val knownLevels = listOf(
            WirelessSwitchBatteryLevel.LOW,
            WirelessSwitchBatteryLevel.MID,
            WirelessSwitchBatteryLevel.HIGH
        )
        for ((firstIndex, first) in knownLevels.withIndex()) {
            for ((secondIndex, second) in knownLevels.withIndex()) {
                assertEquals(
                    knownLevels[minOf(firstIndex, secondIndex)],
                    switches(first, second).lowestBatteryLevel
                )
            }
        }
    }

    @ParameterizedTest
    @EnumSource(WirelessSwitchBatteryLevel::class)
    fun `single switch reports its level`(level: WirelessSwitchBatteryLevel) {
        assertEquals(level, switches(level).lowestBatteryLevel)
    }

    @ParameterizedTest
    @EnumSource(WirelessSwitchBatteryLevel::class)
    fun `unknown levels do not mask known levels`(level: WirelessSwitchBatteryLevel) {
        assertEquals(
            level,
            switches(WirelessSwitchBatteryLevel.UNKNOWN, level, WirelessSwitchBatteryLevel.UNKNOWN).lowestBatteryLevel
        )
    }

    @Test
    fun `all received switches participate in the minimum`() {
        assertEquals(
            WirelessSwitchBatteryLevel.LOW,
            switches(
                WirelessSwitchBatteryLevel.HIGH,
                WirelessSwitchBatteryLevel.MID,
                WirelessSwitchBatteryLevel.UNKNOWN,
                WirelessSwitchBatteryLevel.LOW
            ).lowestBatteryLevel
        )
    }

    @Test
    fun `no switches reports unknown`() {
        assertEquals(WirelessSwitchBatteryLevel.UNKNOWN, switches().lowestBatteryLevel)
    }

    @Test
    fun `unpaired and unknown switch types do not affect the minimum`() {
        val info = WirelessSwitchesInfo(
            listOf(
                WirelessSwitchInfo(1, SwitchType.ROAD_SHIFTER, SwitchSide.LEFT, WirelessSwitchBatteryLevel.HIGH),
                WirelessSwitchInfo(2, SwitchType.NONE, SwitchSide.NONE, WirelessSwitchBatteryLevel.LOW),
                WirelessSwitchInfo(3, SwitchType.UNKNOWN, SwitchSide.NONE, WirelessSwitchBatteryLevel.LOW)
            )
        )
        assertEquals(WirelessSwitchBatteryLevel.HIGH, info.lowestBatteryLevel)
    }

    private fun switches(vararg levels: WirelessSwitchBatteryLevel) = WirelessSwitchesInfo(
        levels.mapIndexed { index, level ->
            WirelessSwitchInfo(index + 1, SwitchType.ROAD_SHIFTER, SwitchSide.NONE, level)
        }
    )
}
