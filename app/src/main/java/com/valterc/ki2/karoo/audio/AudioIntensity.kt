package com.valterc.ki2.karoo.audio

enum class AudioIntensity(
    val frequencyMultiplierK2: Double, val durationMultiplierK2: Double,
    val frequencyMultiplierK24: Double, val durationMultiplierK24: Double,
    ) {

    Quiet(
        0.02, 0.8,
        0.05, 0.4
    ),
    Low(
        0.1, 0.9,
        0.1, 0.5
    ),
    Reduced(
        0.4, 0.9,
        0.4, 0.6
    ),
    Normal(
        1.0, 1.0,
        1.0, 0.7
    );

    companion object {
        fun fromName(name: String): AudioIntensity {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: Normal
        }
    }

}