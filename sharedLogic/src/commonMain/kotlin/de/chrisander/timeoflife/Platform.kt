package de.chrisander.timeoflife

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
