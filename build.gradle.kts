import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(17)
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set("""
            -----BEGIN CERTIFICATE-----
            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
            T2X2G7P+jHiU
            -----END CERTIFICATE-----
        """.trimIndent())
        privateKey.set("""
            -----BEGIN RSA PRIVATE KEY-----
            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
            -----END RSA PRIVATE KEY-----
        """.trimIndent())
        password.set("pass")
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }
}
