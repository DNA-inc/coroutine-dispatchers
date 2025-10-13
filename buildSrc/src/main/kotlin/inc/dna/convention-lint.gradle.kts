package inc.dna

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("com.ncorti.ktfmt.gradle")
  id("com.vanniktech.maven.publish")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }

mavenPublishing {
  coordinates(
      groupId = group.toString(),
      version = version.toString(),
  )
  pom {
    licenses {
      name = "MIT"
      url = "https://opensource.org/licenses/MIT"
    }
    name = "Coroutine Dispatchers Lint Rules"
    url = "https://github.com/DNA-inc/coroutine-dispatchers"
    inceptionYear = "2025"
    developers {
      developer {
        id = "remcomokveld"
        name = "Remco Mokveld"
        email = "remco.mokveld@dna.inc"
      }
    }
    scm { url = "https://github.com/DNA-inc/coroutine-dispatchers" }
  }
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/DNA-inc/coroutine-dispatchers")
      credentials(PasswordCredentials::class)
    }
  }
}

tasks.test { useJUnitPlatform() }
