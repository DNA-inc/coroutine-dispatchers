plugins { id("inc.dna.convention-library") }

kotlin {
  androidLibrary { namespace = "inc.dna.coroutines" }
  compilerOptions { optIn.add("inc.dna.coroutines.InternalDispatchersApi") }
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
