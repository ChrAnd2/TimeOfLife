package de.chrisander.timeoflife.data.datasource

import de.chrisander.timeoflife.data.datasource.dto.UserProfileDto
import de.chrisander.timeoflife.data.datasource.mapper.toDomain
import de.chrisander.timeoflife.data.datasource.mapper.toDto
import de.chrisander.timeoflife.domain.model.profile.UserProfile
import kotlinx.serialization.json.Json

internal object UserProfileJson {

    private val json = Json {
        ignoreUnknownKeys = true
        // ActivityDto.Default has a property named "type", the default discriminator.
        classDiscriminator = "kind"
    }

    fun encode(profile: UserProfile): String = json.encodeToString(profile.toDto())

    fun decode(stored: String): UserProfile? =
        runCatching { json.decodeFromString<UserProfileDto>(stored).toDomain() }.getOrNull()
}
