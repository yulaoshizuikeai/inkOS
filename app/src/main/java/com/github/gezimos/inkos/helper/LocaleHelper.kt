package com.github.gezimos.inkos.helper

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun applyLocale(context: Context, langCode: String): Context {
        if (langCode == "system") {
            return context
        }

        val locale = when (langCode) {
            "zh_cn" -> Locale.SIMPLIFIED_CHINESE
            "zh_tw" -> Locale.TRADITIONAL_CHINESE
            "en" -> Locale.ENGLISH
            else -> return context
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return context.createConfigurationContext(config)
    }
}
