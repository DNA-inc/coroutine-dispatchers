package inc.dna

plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  id("com.ncorti.ktfmt.gradle")
  `java-test-fixtures`
  id("com.vanniktech.maven.publish")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    androidLibrary {
        compileSdk = 36
    }
    jvm()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64())
}


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
