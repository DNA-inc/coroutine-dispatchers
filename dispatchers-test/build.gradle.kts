plugins { id("inc.dna.convention-library") }

kotlin {
  androidLibrary { namespace = "inc.dna.coroutines.test" }
  compilerOptions { optIn.add("inc.dna.coroutines.InternalDispatchersApi") }
  sourceSets {
    commonMain {
      dependencies {
        api(libs.coroutines.core)
        api(libs.coroutines.test)
        api(project(":dispatchers"))
      }
    }
  }
}

mavenPublishing.pom {
  name = "DispatchersNA Testing API"
  description = "APIs for replacing real dispatchers with test instances."
}
