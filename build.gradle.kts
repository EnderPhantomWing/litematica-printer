plugins {
    id("maven-publish")
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT" apply false

    // https://github.com/ReplayMod/preprocessor
    // https://github.com/Fallen-Breath/preprocessor
    // https://jitpack.io/#Fallen-Breath/preprocessor
    id("com.replaymod.preprocess") version "c5abb4fb12"
}

preprocess {
    strictExtraMappings.set(false)

    val mc12101 = createNode(   "1.21.1",   1_21_01,    "mojang")
    val mc12103 = createNode(   "1.21.3",   1_21_03,    "mojang")
    val mc12104 = createNode(   "1.21.4",   1_21_04,    "mojang")
    val mc12105 = createNode(   "1.21.5",   1_21_05,    "mojang")
    val mc12108 = createNode(   "1.21.8",   1_21_08,    "mojang")
    val mc12110 = createNode(   "1.21.10",  1_21_10,    "mojang")
    val mc12111 = createNode(   "1.21.11",  1_21_11,    "mojang")
    val mc260102 = createNode(  "26.1.2",     26_01_02,   "mojang")

    mc12101.link(mc12103, null)
    mc12103.link(mc12104, null)
    mc12104.link(mc12105, file("mappings/mapping-1.21.4-1.21.5.txt"))
    mc12105.link(mc12108, null)
    mc12108.link(mc12110, null)
    mc12110.link(mc12111, null)
    mc12111.link(mc260102, file("mappings/mapping-1.21.11-26.1.txt"))

    // See https://github.com/Fallen-Breath/fabric-mod-template/blob/1d72d77a1c5ce0bf060c2501270298a12adab679/build.gradle#L55-L63
    for (node in getNodes()) {
        findProject(node.project)
            ?.ext
            ?.set("mcVersion", node.mcVersion)
    }
}