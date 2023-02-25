import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.3"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	//Dokka Documentacion Kotlin
	id("org.jetbrains.dokka") version "1.7.20"
	kotlin("plugin.serialization") version "1.7.20"
}

group = "resa.rodriguez"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	// Spring Boot & Spring Data Reactive
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

	// Validaciones de Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// WebFlux Reactive
	implementation ("org.springframework.boot:spring-boot-starter-webflux")

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
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testImplementation("io.projectreactor:reactor-test")

	// Dokka Documentación Kotlin
	dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")

	// BCrypt para cifrar las contraseñas de los users
	implementation("com.ToxicBakery.library.bcrypt:bcrypt:+")

	// Para tokens
	implementation("com.auth0:java-jwt:4.2.1")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

	testImplementation("com.ninja-squad:springmockk:4.0.0")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
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
