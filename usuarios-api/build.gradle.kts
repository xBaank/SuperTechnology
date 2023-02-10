import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.2"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	//Dokka Documentacion Kotlin
	id("org.jetbrains.dokka") version "1.7.20"
}

group = "resa.rodriguez"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot & Spring Data Reactive
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

	// Validaciones de Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Websocket
	implementation("org.springframework.boot:spring-boot-starter-websocket")

	// Dependencias Kotlin
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// Logging
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")

	// Bases de datos, comentamos la no reactiva
	// runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	//Dokka Documentación Kotlin
	dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
