plugins { id("inc.dna.convention-library") }

kotlin {
  androidLibrary { namespace = "inc.dna.coroutines.dispatchers" }
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

dependencies {
  lintPublish(project(":dispatchers-lint"))
  //  api(libs.coroutines.core)
  testFixturesApi(libs.coroutines.core)
  testFixturesApi(libs.coroutines.test)
  //  testImplementation(dependencies.platform(libs.junit.bom))
  //  testImplementation(libs.junit.jupiter)
  //  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
