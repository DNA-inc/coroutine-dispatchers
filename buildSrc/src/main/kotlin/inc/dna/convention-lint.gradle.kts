package inc.dna

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("com.ncorti.ktfmt.gradle")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }

tasks.test { useJUnitPlatform() }
