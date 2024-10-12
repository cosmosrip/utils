plugins {
	kotlin("jvm") version "2.0.20"
	`java-library`
}

group = "rip.cosmos"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("com.google.jimfs:jimfs:1.3.0")
}

kotlin {
	jvmToolchain(21)
}

java.withSourcesJar()
java.withJavadocJar()