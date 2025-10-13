plugins { `kotlin-dsl` }

dependencies {
  implementation("com.android.tools.build:gradle:8.13.0")
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.ktfmt.gradlePlugin)
  implementation("com.vanniktech.maven.publish:com.vanniktech.maven.publish.gradle.plugin:0.34.0")
}
