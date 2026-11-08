package de.chrisander.timeoflife.ui.healthsettings

import java.util.Locale

internal actual fun countryNames(): Map<String, String> =
    Locale.getISOCountries().associateWith { Locale.Builder().setRegion(it).build().displayCountry }

internal actual fun deviceCountryCode(): String? =
    Locale.getDefault().country.takeIf { it.length == 2 }
