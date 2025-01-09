package com.valterc.ki2.karoo.audio

enum class AudioIntensity(val frequencyMultiplier: Double, val durationMultiplier: Double) {

    Quiet(0.01, 0.7),
    Low(0.1, 0.9),
    Reduced(0.4, 0.9),
    Normal(1.0, 1.0);

    companion object {
        fun fromName(name: String): AudioIntensity {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: Normal
        }
    }

}