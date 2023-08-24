import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream


val mainClassName = "LauncherKt"


// Kotlin version info & general info
plugins {
  kotlin("jvm") version "1.8.22"
}

allprojects {
//  group = "ee.bortal"
//  version = "master-SNAPSHOT"
}

apply(plugin = "kotlin")

kotlin {
  jvmToolchain(11)
}

java {
  withSourcesJar()
}



// Dependencies
repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  val kliteVersion = "1.5.4"
  val selenium_version = "4.11.0"
  val webdriver_manager_version = "5.5.0"
  implementation("com.github.codeborne.klite:klite-server:$kliteVersion")
  implementation("com.github.codeborne.klite:klite-json:$kliteVersion")
  implementation("com.github.codeborne.klite:klite-i18n:$kliteVersion")
  implementation("com.github.codeborne.klite:klite-jdbc:$kliteVersion")
  implementation("com.github.codeborne.klite:klite-slf4j:$kliteVersion")
  implementation("org.postgresql:postgresql:42.6.0")
  implementation("org.jsoup:jsoup:1.13.1")
  testImplementation("com.github.codeborne.klite:klite-jdbc-test:$kliteVersion")
  implementation("org.seleniumhq.selenium:selenium-java:$selenium_version")
  implementation("io.github.bonigarcia:webdrivermanager:$webdriver_manager_version")
}


// Sources & folders
sourceSets {
  main {
    java.setSrcDirs(emptyList<String>())
    kotlin.setSrcDirs(listOf("src"))
    resources.setSrcDirs(listOf("src")).exclude("**/*.kt")
    resources.srcDirs("db", "i18n")
  }
  test {
    java.setSrcDirs(emptyList<String>())
    kotlin.setSrcDirs(listOf("test"))
    resources.setSrcDirs(listOf("test")).exclude("**/*.kt")
  }
}



// Tasks
tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-opt-in=kotlin.ExperimentalStdlibApi"
  }
}

tasks.register<Copy>("deps") {
  into("$buildDir/libs/deps")
  from(configurations.runtimeClasspath)
}

tasks.jar {
  dependsOn("deps")
  doFirst {
    manifest {
      attributes(
        "Main-Class" to mainClassName,
        "Class-Path" to File("$buildDir/libs/deps").listFiles()?.joinToString(" ") { "deps/${it.name}"}
      )
    }
  }

  archiveBaseName.set("${rootProject.name}-${project.name}")
  manifest {
    attributes(mapOf(
      "Implementation-Title" to archiveBaseName,
      "Implementation-Version" to project.version
    ))
  }
}

tasks.test {
  useJUnitPlatform()
  // enable JUnitAssertionImprover from klite.jdbc-test
  jvmArgs("-Djunit.jupiter.extensions.autodetection.enabled=true", "--add-opens=java.base/java.lang=ALL-UNNAMED")
}

tasks.register<JavaExec>("run") {
  mainClass.set(mainClassName)
  classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("types.ts") {
  dependsOn("classes")
  mainClass.set("klite.json.TSGenerator")
  classpath = sourceSets.main.get().runtimeClasspath
  args("${project.buildDir}/classes/kotlin/main")
  standardOutput = ByteArrayOutputStream()
  doLast {
    project.file("build/types.ts").writeText(standardOutput.toString())
  }
}

tasks.withType<KotlinCompile> {
  finalizedBy("types.ts")
}