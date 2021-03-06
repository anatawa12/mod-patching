plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("kapt") version "1.3.70"
    `maven-publish`
    signing
}

group = "com.anatawa12.mod-patching"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://files.minecraftforge.net/maven") {
        name = "forge"
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.moshi:moshi:1.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")
    implementation("com.anatawa12.forge:ForgeGradle:2.3-1.0.2")
    implementation("io.sigpipe:jbsdiff:1.0")
}

tasks.compileKotlin.get().kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+TrailingCommas")

val pathingMod by gradlePlugin.plugins.creating {
    implementationClass = "com.anatawa12.modPatching.ModPatchingPlugin"
    id = "com.anatawa12.mod-patching"
}

publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
    pom {
        name.set("patching mod gradle plugin")
        description.set("A plugin to make a mod which patches some other mod.")
        url.set("https://github.com/anatawa12/mod-patching#readme")
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("anatawa12")
                name.set("anatawa12")
                email.set("anatawa12@icloud.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/anatawa12/mod-patching.git")
            developerConnection.set("scm:git:ssh://github.com:anatawa12/mod-patching.git")
            url.set("https://github.com/anatawa12/mod-patching")
        }
    }
}

fun isReleaseBuild() = !version.toString().contains("SNAPSHOT")

fun getReleaseRepositoryUrl(): String {
    return project.findProperty("RELEASE_REPOSITORY_URL")?.toString()
        ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
}

fun getSnapshotRepositoryUrl(): String {
    return project.findProperty("SNAPSHOT_REPOSITORY_URL")?.toString()
        ?: "https://oss.sonatype.org/content/repositories/snapshots/"
}

signing {
    publishing.publications.forEach { publication ->
        sign(publication)
    }
}

publishing {
    repositories {
        maven {
            name = "mavenCentral"
            url = uri(if (isReleaseBuild()) getReleaseRepositoryUrl() else getSnapshotRepositoryUrl())

            credentials {
                username = project.findProperty("com.anatawa12.sonatype.username")?.toString() ?: ""
                password = project.findProperty("com.anatawa12.sonatype.passeord")?.toString() ?: ""
            }
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf { publication.name != "pathingModPluginMarkerMaven" }
}
