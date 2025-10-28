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

mavenPublishing.pom {
  name = "DispatchersNA API"
  description = "API for selecting CoroutineDispatcher from a Context Element"
}

dependencies { lintPublish(project(":dispatchers-lint")) }
