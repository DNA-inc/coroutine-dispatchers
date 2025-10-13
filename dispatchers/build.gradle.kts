plugins { id("inc.dna.convention-library") }

kotlin {
  androidLibrary { namespace = "inc.dna.coroutines.dispatchers" }
  sourceSets {
    commonMain { dependencies { api(libs.coroutines.core) } }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(project(":dispatchers-test"))
      }
    }
    androidMain {}
  }
}

dependencies { lintPublish(project(":dispatchers-lint")) }
