rootProject.name = "federated-catalog"

// this is needed to have access to snapshot builds of plugins
pluginManagement {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        mavenLocal()
    }
    versionCatalogs {
        create("libs") {
            from("org.eclipse.edc:edc-versions:0.0.1-SNAPSHOT")
        }
    }
}