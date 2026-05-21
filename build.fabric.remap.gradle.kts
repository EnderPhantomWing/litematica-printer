@file:Suppress("UnstableApiUsage")

import groovy.json.JsonSlurper

plugins {
    id("mod-plugin")
    id("maven-publish")
    id("net.fabricmc.fabric-loom-remap")
    id("com.replaymod.preprocess")
}

version = modArchivesBaseName + fullProjectVersion
group = modMavenGroup

repositories {
    mavenLocal()
    maven { url = uri("$rootDir/project_maven") }
    fun strictMaven(url: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) }
        filter {
            groups.forEach {
                includeGroupAndSubgroups(it)
                includeGroupAndSubgroups("$it.*")
            }
        }
    }
    fun githubPKGMaven(repo: String, vararg groups: String) = exclusiveContent {
        forRepository {
            maven {
                url = uri("https://maven.pkg.github.com/$repo")
                credentials {
                    username = System.getenv("GH_USERNAME") ?: "github-actions[bot]"
                    password = System.getenv("GH_TOKEN") ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
        filter {
            groups.forEach {
                includeGroupAndSubgroups(it)
                includeGroupAndSubgroups("$it.*")
            }
        }
    }

    strictMaven("https://mvnrepository.com/artifact/com.belerweb/pinyin4j")

    strictMaven("https://www.cursemaven.com", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth")

    strictMaven("https://maven.fabricmc.net")
    strictMaven("https://maven.nucleoid.xyz", "eu.pb4")
    strictMaven("https://maven.terraformersmc.com/releases", "com.terraformersmc")
    strictMaven("https://maven.fallenbreath.me/releases")
    strictMaven("https://masa.dy.fi/maven/sakura-ryoko")
    strictMaven("https://maven.jackf.red/releases")
    strictMaven("https://maven.blamejared.com")
    strictMaven("https://staging.alexiil.uk/maven")
    strictMaven("https://maven.isxander.dev/releases")
    strictMaven("https://maven.shedaniel.me")

    strictMaven("https://jitpack.io")

    githubPKGMaven("ponuing/JackFredLib")
    githubPKGMaven("ponuing/ChestTracker")
    githubPKGMaven("ponuing/WhereIsIt")
}

// https://github.com/FabricMC/fabric-loader/issues/783
configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:$fabricLoaderVersion")
        force("com.terraformersmc:modmenu:${prop("modmenu")}")
        force("maven.modrinth:malilib:${prop("malilib_dependency")}")
        force("maven.modrinth:litematica:${prop("litematica_dependency")}")
        force("maven.modrinth:tweakeroo:${prop("tweakeroo_dependency")}")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("com.belerweb:pinyin4j:${prop("pinyin_version")}")?.let { include(it) }

    modImplementation("com.terraformersmc:modmenu:${prop("modmenu")}")

    // use https://masa.dy.fi/maven/sakura-ryoko
    modImplementation("fi.dy.masa.malilib:${prop("malilib")}:${prop("malilib_dependency")}")
    modImplementation("fi.dy.masa.litematica:${prop("litematica")}:${prop("litematica_dependency")}")
    modImplementation("fi.dy.masa.tweakeroo:${prop("tweakeroo")}:${prop("tweakeroo_dependency")}")


    // 箱子追踪
    modImplementation("red.jackf.jackfredlib:jackfredlib:${prop("jackfredlib")}")
    modImplementation("red.jackf:chesttracker:${prop("chesttracker")}")
    modImplementation("red.jackf:whereisit:${prop("whereisit")}")

    modImplementation("red.jackf.jackfredlib:jackfredlib:${prop("jackfredlib")}")
    modImplementation("dev.isxander:yet-another-config-lib:${prop("yacl")}")
    modImplementation("com.blamejared.searchables:${prop("searchables")}")

    // 快捷潜影盒
    val quickshulkerUrl = prop("quickshulker").toString()
    if (quickshulkerUrl.isNotEmpty()) {
        val quickshulkerFile = downloadDependencyMod(quickshulkerUrl)
        if (quickshulkerFile != null && quickshulkerFile.exists()) {
            modImplementation(files(quickshulkerFile))
        }
    }
}

loom {
    val commonVmArgs = listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true", "-Dmixin.env.remapRefMap=true")
    var programArgs = listOf("--width", "1280", "--height", "720")
    val profileFile = file("../../profile.json")
    if (profileFile.exists()) {
        @Suppress("UNCHECKED_CAST")
        val profile = JsonSlurper().parseText(profileFile.readText()) as Map<String, List<String>>
        val username = profile["username"].toString()
        val uuid = profile["uuid"].toString()
        val xuid = profile["xuid"].toString()
        val accessToken = profile["accessToken"].toString()
        programArgs = programArgs + listOf(
            "--username", username,
            "--uuid", uuid,
            "--xuid", xuid,
            "--accessToken", accessToken,
            "--userType", "msa",
            "--versionType", "release"
        )
    } else {
        programArgs = programArgs + listOf("--username", "PrinterTest")
    }
    runs {
        named("client") {
            ideConfigGenerated(true)
            vmArgs(commonVmArgs)
            programArgs(programArgs)
            runDir = "../../run/client"
        }
    }
}

tasks {
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod_version")}"))
        dependsOn("build")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
            version = modVersion
        }
    }
    repositories {
        mavenLocal()
        maven {
            url = uri("$rootDir/publish")
        }
    }
}