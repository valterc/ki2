package com.valterc.ki2.karoo.audio

enum class AudioIntensity(
    val frequencyMultiplierK2: Double, val durationMultiplierK2: Double,
    val frequencyMultiplierK24: Double, val durationMultiplierK24: Double,
    ) {

    Quiet(
        0.01, 0.7,
        0.05, 0.3
    ),
    Low(
        0.1, 0.9,
        0.1, 0.4
    ),
    Reduced(
        0.4, 0.9,
        0.4, 0.4
    ),
    Normal(
        1.0, 1.0,
        1.0, 0.5
    );

    companion object {
        fun fromName(name: String): AudioIntensity {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: Normal
        }
    }

}