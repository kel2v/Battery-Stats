package com.bytemanager.stats.utils

import android.os.BatteryManager
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class BatteryStateRepositoryTests {
    @ParameterizedTest(name = "level={0}, scale={1} -> {2}%")
    @CsvSource(
        // theoretically enough values
        "0, 1, 0",
        "1, 2, 50",
        "2, 2, 100",

        // Arbitrary values
        "0,1,0",
        "1,1,100",

        "0,2,0",
        "1,2,50",
        "2,2,100",

        "0,3,0",
        "1,3,33",
        "2,3,66",
        "3,3,100",

        "0,5,0",
        "1,5,20",
        "2,5,40",
        "3,5,60",
        "4,5,80",
        "5,5,100",

        "0,10,0",
        "1,10,10",
        "5,10,50",
        "9,10,90",
        "10,10,100",

        "0,100,0",
        "1,100,1",
        "25,100,25",
        "50,100,50",
        "75,100,75",
        "99,100,99",
        "100,100,100",

        "0,1000,0",
        "1,1000,0",
        "25,1000,2",
        "50,1000,5",
        "75,1000,7",
        "100,1000,10",
        "125,1000,12",
        "375,1000,37",
        "500,1000,50",
        "625,1000,62",
        "875,1000,87",
        "999,1000,99",
        "1000,1000,100",
    )
    fun getBatteryLevelPercentage_validInput_returnsCorrect(level: Int, scale: Int, expected: Int) {
        assertEquals(
            BatteryStateRepositoryUtils.getBatteryLevelPercentage(level, scale),
            expected
        )
    }

    @ParameterizedTest(name = "level={0}, scale={1} -> {2}%")
    @CsvSource(
        // theoretically enough values
        "-2, -1, -1",
        "-1, -1, -1",
        "-1, -2, -1",
        "-1, 0, -1",
        "-1, 1, -1",
        "0, -1, -1",
        "0, 0, -1",
        "1, -1, -1",
        "1, 0, -1",
        "2, 1, -1",

        // Arbitrary values
        "-10, -1, -1",
        "-1, -1, -1",
        "0, -1, -1",
        "1, -1, -1",
        "10, -1, -1",

        "-10, 0, -1",
        "-1, 0, -1",
        "0, 0, -1",
        "1, 0, -1",
        "10, 0, -1",

        "-10, 1, -1",
        "-1, 1, -1",
        "2, 1, -1",
        "3, 1, -1",
        "10, 1, -1",

        "-100, 10, -1",
        "-10, 10, -1",
        "-1, 10, -1",
        "11, 10, -1",
        "100, 10, -1"
    )
    fun getBatteryLevelPercentage_invalidInput_returnsError(level: Int, scale: Int, expected: Int) {
        assertEquals(
            BatteryStateRepositoryUtils.getBatteryLevelPercentage(level, scale),
            expected
        )
    }


    @ParameterizedTest(name = "rawValue={0} -> temp={1}°C")
    @CsvSource(
        "-501, -50.1",
        "-500, -50.0",
        "-249, -24.9",
        "-100, -10.0",
        "-76, -7.6",
        "-49, -4.9",
        "-25, -2.5",

        "-1, -0.1",
        "-0, 0.0",
        "1, 0.1",

        "26, 2.6",
        "51, 5.1",
        "75, 7.5",
        "99, 9.9",
        "250, 25.0",
        "501, 50.1"
    )
    fun getTemperatureInCelsius_validInput_correctOutput(rawValue: Int, expected: Float) {
        assertEquals(BatteryStateRepositoryUtils.getTemperatureInCelsius(rawValue), expected)
    }

    @ParameterizedTest(name = "rawValue={0} -> temp={1}°C")
    @CsvSource(
        "-2147483648, -3.4028235E38"
    )
    fun getTemperatureInCelsius_invalidInput_returnError(rawValue: Int, expected: Float) {
        assertEquals(BatteryStateRepositoryUtils.getTemperatureInCelsius(rawValue), expected)
    }


    @ParameterizedTest(name = "rawValue={0} -> volts={1}V")
    @CsvSource(
        "-501, -0.501",
        "-500, -0.500",
        "-249, -0.249",
        "-100, -0.100",
        "-76, -0.076",
        "-49, -0.049",
        "-25, -0.025",

        "-1, -0.001",
        "-0, 0.0",
        "1, 0.001",

        "26, 0.026",
        "51, 0.051",
        "75, 0.075",
        "99, 0.099",
        "250, 0.250",
        "501, 0.501"
    )
    fun getVoltage_validInput_correctOutput(rawValue: Int, expected: Float) {
        assertEquals(BatteryStateRepositoryUtils.getVoltage(rawValue), expected)
    }

    @ParameterizedTest(name = "rawValue={0} -> volts={1}V")
    @CsvSource(
        "-2147483648, -3.4028235E38"
    )
    fun getVoltage_invalidInput_returnError(rawValue: Int, expected: Float) {
        assertEquals(BatteryStateRepositoryUtils.getVoltage(rawValue), expected)
    }



    @ParameterizedTest(name = "rawString={0} -> technology={1}")
    @CsvSource(
        "Li-ion, Li-ion",
        "LiPo, LiPo",
        "Li-SOCl2, Li-SOCl2",
        "Li-MnO2, Li-MnO2",
        "NiMH, NiMH",
        "Button, Button"
    )
    fun getTechnology_validInput_correctOutput(rawString: String?, expected: String) {
        assertEquals(BatteryStateRepositoryUtils.getTechnology(rawString), expected)
    }

    @Test
    fun getTechnology_invalidInput_returnError() {
        assertEquals(BatteryStateRepositoryUtils.getTechnology(null), "Not available")
    }



    @Test
    fun getHealth_batteryHealthCold_returnCold() {
        assertEquals(
            BatteryStateRepositoryUtils.getHealth(BatteryManager.BATTERY_HEALTH_COLD),
            "Cold"
        )
    }

    @Test
    fun getHealth_batteryHealthDead_returnDead() {
        assertEquals(
            BatteryStateRepositoryUtils.getHealth(BatteryManager.BATTERY_HEALTH_DEAD),
            "Dead"
        )
    }

    @Test
    fun getHealth_batteryHealthGood_returnGood() {
        assertEquals(
            BatteryStateRepositoryUtils.getHealth(BatteryManager.BATTERY_HEALTH_GOOD),
            "Good"
        )
    }

    @Test
    fun getHealth_batteryHealthOverheat_returnOverheated() {
        assertEquals(
            BatteryStateRepositoryUtils.getHealth(BatteryManager.BATTERY_HEALTH_OVERHEAT),
            "Overheated"
        )
    }

    @Test
    fun getHealth_batteryHealthOverVoltage_returnOvervoltage() {
        assertEquals(
            BatteryStateRepositoryUtils.getHealth(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE),
            "Overvoltage"
        )
    }

    @Test
    fun getHealth_batteryHealthUnspecifiedFailure_returnFailure() {
        assertEquals(
            BatteryStateRepositoryUtils.getHealth(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE),
            "Failure"
        )
    }

    @Test
    fun getHealth_batteryHealthUnknown_returnUnknown() {
        assertEquals(
            BatteryStateRepositoryUtils.getHealth(BatteryManager.BATTERY_HEALTH_UNKNOWN),
            "Unknown"
        )
    }


    @Test
    fun getChargingStatus_statusFull_returnFull() {
        assertEquals(
            BatteryStateRepositoryUtils.getChargingStatus(BatteryManager.BATTERY_STATUS_FULL),
            "Full"
        )
    }

    @Test
    fun getChargingStatus_statusCharging_returnCharging() {
        assertEquals(
            BatteryStateRepositoryUtils.getChargingStatus(BatteryManager.BATTERY_STATUS_CHARGING),
            "Charging"
        )
    }

    @Test
    fun getChargingStatus_statusDischarging_returnDischarging() {
        assertEquals(
            BatteryStateRepositoryUtils.getChargingStatus(BatteryManager.BATTERY_STATUS_DISCHARGING),
            "Discharging"
        )
    }

    @Test
    fun getChargingStatus_statusNotCharging_returnNotCharging() {
        assertEquals(
            BatteryStateRepositoryUtils.getChargingStatus(BatteryManager.BATTERY_STATUS_NOT_CHARGING),
            "Not charging"
        )
    }

    @Test
    fun getChargingStatus_statusUnknown_returnUnknown() {
        assertEquals(
            BatteryStateRepositoryUtils.getChargingStatus(BatteryManager.BATTERY_STATUS_UNKNOWN),
            "Unknown"
        )
    }
}