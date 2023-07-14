plugins {
    kotlin("jvm")
    id("net.c306.dependencydiagram.plugin")
}

projectDependencyGraphConfig {
    message.set("Just trying this gradle plugin...")
}

dependencies {
    implementation(project(path = ":example:ui"))
    implementation(project(path = ":example:domain"))
}