plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("io.ktor.plugin") version "2.2.2"
    application
}

group = "org.dam"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktorVersion = "2.2.3"
val kotlinSerializationVersion = "1.4.1"
val kmongoVersion = "4.8.0"
val coroutinesVersion = "1.6.4"
val retroFitVersion = "2.9.0"
val retroFitKtorVersion = "0.8.0"
val configVersion = "1.4.2"
val koinVersion = "3.3.2"
val koinKtorVersion = "3.3.0"
val arrowVersion = "1.1.5"
val kluentVersion = "1.72"
val testContainersVersion = "1.17.6"
val junitVersion = "5.9.0"


dependencies {
    //junit 5
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")

    testImplementation("io.mockk:mockk:1.13.4")

    //testing con ktor
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    // for JVM:
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.testcontainers:mongodb:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")

    //koin test
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-request-validation:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-network-tls-certificates:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    //kmongo
    implementation("org.litote.kmongo:kmongo-async:$kmongoVersion")
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongoVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:$retroFitVersion")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:$retroFitKtorVersion")

    //config
    implementation("com.typesafe:config:$configVersion")

    //koin
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:3.3.0")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    //koin ktor
    implementation("io.insert-koin:koin-ktor:$koinKtorVersion")
    //arrow core
    implementation("io.arrow-kt:arrow-core:$arrowVersion")

    implementation("io.arrow-kt:arrow-core-retrofit:1.1.5")

    //swagger
    implementation("io.github.smiley4:ktor-swagger-ui:1.1.0")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("pedidosApi.MainKt")
}