plugins {
  `kotlin-dsl`
  id("com.ncorti.ktfmt.gradle") version "0.25.0"
}

dependencies {
  implementation(libs.android.gradlePlugin)
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.ktfmt.gradlePlugin)
  implementation(libs.mavenPublish.gradlePlugin)
}
