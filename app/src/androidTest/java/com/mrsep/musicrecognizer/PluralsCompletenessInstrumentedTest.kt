package com.mrsep.musicrecognizer

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.icu.text.PluralRules
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import com.mrsep.musicrecognizer.core.strings.R as StringsR

/**
 * Ensures all plural resources have the required plural forms for every supported locale.
 *
 * If a translation for a plural key exists but is missing one of the required
 * plural forms (e.g. quantity="other"), the build succeeds but the app crashes at runtime
 * with Resources$NotFoundException when accessing a resource.
 * Android Lint does not catch this for some reason.
 * This test verify all plural forms at runtime for every supported locale.
 */
@RunWith(AndroidJUnit4::class)
class PluralsCompletenessInstrumentedTest {

    @Test
    fun allPluralsMustResolveForRequiredQuantities() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val locales = getSupportedLocales(context)

        val errors = mutableListOf<String>()

        for (locale in locales) {
            val localizedContext = createLocalizedContext(context, locale)
            val resources = localizedContext.resources
            val testQuantities = getQuantitiesForLocale(locale)

            val pluralsClass = StringsR.plurals::class.java
            for (field in pluralsClass.declaredFields) {
                if (field.type == Int::class.javaPrimitiveType) {
                    val id = field.getInt(null)
                    val pluralName = field.name

                    for (quantity in testQuantities) {
                        try {
                            resources.getQuantityString(id, quantity)
                        } catch (e: Resources.NotFoundException) {
                            errors.add(
                                "Locale '$locale', plural '$pluralName', " +
                                "quantity=$quantity: ${e.message}"
                            )
                        }
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail(
                "Found ${errors.size} plural errors:\n\n" +
                errors.joinToString("\n")
            )
        }
    }

    private fun getSupportedLocales(context: Context): List<Locale> {
        return context.resources.assets.locales
            .filter { it.isNotEmpty() }
            .map { Locale.forLanguageTag(it) }
            .filter { it.language.isNotEmpty() }
    }

    private fun createLocalizedContext(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun getQuantitiesForLocale(locale: Locale): Set<Int> {
        val pluralRules = PluralRules.forLocale(locale)
        return pluralRules.keywords
            .mapNotNull { category ->
                pluralRules.getSamples(category)?.firstOrNull()?.toInt()
            }
            .toSet()
    }
}
