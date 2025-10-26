plugins {
	kotlin("jvm") version "2.2.0"
	id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
	id("xyz.jpenilla.run-paper") version "2.3.1"
	id("com.gradleup.shadow") version "9.0.2"
	kotlin("plugin.serialization") version "2.2.0"
}

group = "org.example"
version = "0.0-SNAPSHOT"

apply(plugin = "com.gradleup.shadow")

repositories {
	mavenCentral()
	maven {
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
	implementation("net.dv8tion:JDA:5.6.1")
	paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
	implementation("com.dbeaver.jdbc:com.dbeaver.jdbc.driver.libsql:1.0.4")
	compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
	testImplementation(kotlin("test"))
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.assemble {
	dependsOn(tasks.reobfJar)
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(21)
}

tasks.jar {
	enabled = false
}

tasks.shadowJar {
	archiveFileName = "UHCPlugin2.jar"
}

tasks {
	runServer {
		// Configure the Minecraft version for our task.
		// This is the only required configuration besides applying the plugin.
		// Your plugin's jar (or shadowJar if present) will be used automatically.
		minecraftVersion("1.21.8")
	}
}
