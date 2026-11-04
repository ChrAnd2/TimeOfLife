import dev.detekt.gradle.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover)
}

subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    configure<DetektExtension> {
        source.setFrom("src")
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        parallel = true
    }
    dependencies {
        "detektPlugins"(rootProject.libs.compose.rules.detekt)
    }
    plugins.withId(rootProject.libs.plugins.kover.get().pluginId) {
        configure<KoverProjectExtension> {
            reports.total.log.format = "${project.name}: <value> %"
        }
    }
}

dependencies {
    kover(project(":sharedLogic"))
    kover(project(":androidApp"))
}

kover {
    reports {
        filters {
            excludes {
                // Compose UI isn't covered by unit tests, so it would only dilute the number.
                annotatedBy("androidx.compose.runtime.Composable", "androidx.compose.ui.tooling.preview.Preview")
                classes("*.ComposableSingletons*", "*.BuildConfig", "*.R", "*.R$*")
            }
        }
        total.log.format = "Total: <value> %"
    }
}
