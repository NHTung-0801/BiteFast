plugins {
    id("bitefast.jvm.library")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    compileOnly("javax.inject:javax.inject:1")

    testImplementation(libs.junit5)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

tasks.test {
    useJUnitPlatform()
}
