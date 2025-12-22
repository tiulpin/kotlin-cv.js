plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "me.tiulpin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    sourceSets {
        jsMain {
            dependencies {
                implementation(libs.kotlinx.html)
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
