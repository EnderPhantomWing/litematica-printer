import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

plugins {
    id("java-library")
    id("maven-publish")
    id("mod-plugin")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

val jsonSlurper = JsonSlurper()

group = modMavenGroup
version = fullProjectVersion

base {
    archivesName.set("$modArchivesBaseName-versionpack")
}

val fabricSubprojects = rootProject.subprojects.filter { it.name != "fabricWrapper" }

fabricSubprojects.forEach {
    evaluationDependsOn(":${it.name}")
}

tasks {
    named<Jar>("jar") {
        outputs.upToDateWhen { false }

        from(rootProject.file("LICENSE"))
        from(layout.buildDirectory.dir("tmp/submods"))
    }

    named<ProcessResources>("processResources") {
        outputs.upToDateWhen { false }

        dependsOn(fabricSubprojects.map { it.tasks.named("buildAndCollect") })

        doLast {
            // 每次先清空临时目录，防止旧 JAR 混入
            val targetDir = layout.buildDirectory.dir("tmp/submods/META-INF/jars").get().asFile
            println("📁 目标JAR目录: ${targetDir.absolutePath}")

            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            targetDir.mkdirs()

            // 复制所有子模块JAR
            copy {
                from(fabricSubprojects.map { it.tasks.named("buildAndCollect").get().outputs.files })
                into(targetDir)
                include("*.jar")
                exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar")
                eachFile { println("📦 Copy JAR: ${this.name}") }
            }

            // ====================== 下面是原有逻辑，保持不变 ======================
            // 复制图标文件
            val rootIcon = rootProject.file("src/main/resources/assets/$modId/icon.png")
            val wrapperIconInResources =
                layout.projectDirectory.file("src/main/resources/assets/$wrapperModId/icon.png").asFile
            val wrapperIconInBuild = layout.buildDirectory.file("resources/main/assets/$wrapperModId/icon.png").get().asFile
            if (!wrapperIconInResources.exists()) {
                if (rootIcon.exists()) {
                    wrapperIconInBuild.parentFile.mkdirs()
                    rootIcon.copyTo(wrapperIconInBuild, overwrite = true)
                    println("✓ The icon has been copied from the root project: ${rootIcon.name}")
                } else {
                    println("⚠ Icon file not found, copy skipped")
                }
            }

            // 读取并更新fabric.mod.json
            val jars = if (targetDir.exists() && targetDir.isDirectory) {
                targetDir.listFiles { f ->
                    f.isFile && f.name.endsWith(".jar")
                            && !f.name.endsWith("-dev.jar")
                            && !f.name.endsWith("-sources.jar")
                            && !f.name.endsWith("-shadow.jar")
                }?.map { mapOf("file" to "META-INF/jars/${it.name}") } ?: emptyList()
            } else {
                emptyList()
            }

            val minecraftVersions = fabricSubprojects.mapNotNull { sub ->
                (sub.findProperty("minecraft_dependency") as? String)?.takeIf { it.isNotBlank() }
                    .also { if (it != null) println("✓ Collect Minecraft version: $it") }
            }

            val jsonFile = layout.buildDirectory.file("resources/main/fabric.mod.json").get().asFile
            if (jsonFile.exists()) {
                @Suppress("UNCHECKED_CAST")
                val json = jsonSlurper.parse(jsonFile) as MutableMap<String, Any>

                json["jars"] = jars

                @Suppress("UNCHECKED_CAST")
                (json["depends"] as? MutableMap<String, Any>)?.put("minecraft", minecraftVersions)

                jsonFile.bufferedWriter().use { it.write(JsonBuilder(json).toPrettyString()) }

                println("✅ fabric.mod.json has updated, include ${jars.size} subversion JAR")
                jars.forEach { println("  - ${it["file"]}") }
            } else {
                println("⚠ Not found fabric.mod.json: ${jsonFile.absolutePath}")
            }
        }
    }
}