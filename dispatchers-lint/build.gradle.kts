plugins { id("inc.dna.convention-lint") }

tasks.jar {
  manifest { attributes["Lint-Registry-v2"] = "inc.dna.coroutines.lint.CoroutinesIssueRegistry" }
}

dependencies {
  compileOnly(libs.lint.api)
  testImplementation(libs.lint.api)
  testImplementation(libs.lint.test)

  testImplementation(libs.junit)
}
