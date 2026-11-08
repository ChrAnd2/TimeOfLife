package de.chrisander.timeoflife.ui.healthsettings

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.countryCode
import platform.Foundation.currentLocale

internal actual fun countryNames(): Map<String, String> {
    val locale = NSLocale.currentLocale
    return NSLocale.ISOCountryCodes.filterIsInstance<String>().associateWith { code ->
        locale.displayNameForKey(NSLocaleCountryCode, code) ?: code
    }
}

internal actual fun deviceCountryCode(): String? =
    NSLocale.currentLocale.countryCode?.takeIf { it.length == 2 }
