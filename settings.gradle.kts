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
        // create version catalog for all EDC modules
        create("edc") {
            version("edc", "0.0.1-SNAPSHOT")
            library("spi-catalog", "org.eclipse.edc", "catalog-spi").versionRef("edc")
            library("spi-core", "org.eclipse.edc", "core-spi").versionRef("edc")
            library("spi-web", "org.eclipse.edc", "web-spi").versionRef("edc")
            library("util", "org.eclipse.edc", "util").versionRef("edc")
            library("boot", "org.eclipse.edc", "boot").versionRef("edc")
            library("core-controlplane", "org.eclipse.edc", "control-plane-core").versionRef("edc")
            library("core-connector", "org.eclipse.edc", "connector-core").versionRef("edc")
            library("core-jetty", "org.eclipse.edc", "jetty-core").versionRef("edc")
            library("core-jersey", "org.eclipse.edc", "jersey-core").versionRef("edc")
            library("core-junit", "org.eclipse.edc", "junit").versionRef("edc")
            library("api-management-config", "org.eclipse.edc", "management-api-configuration").versionRef("edc")
            library("ext-http", "org.eclipse.edc", "http").versionRef("edc")
            library("spi-ids", "org.eclipse.edc", "ids-spi").versionRef("edc")
            library("ext-azure-cosmos-core", "org.eclipse.edc", "azure-cosmos-core").versionRef("edc")
            library("ext-azure-test", "org.eclipse.edc", "azure-test").versionRef("edc")
            library("ext-jdklogger", "org.eclipse.edc", "monitor-jdk-logger").versionRef("edc")
            library("ext-observability", "org.eclipse.edc", "api-observability").versionRef("edc")

            bundle(
                "connector",
                listOf("boot", "core-connector", "core-jersey", "core-controlplane", "ext-observability")
            )
        }
    }
}

include(":core:federated-catalog")
include(":extensions:store:fcc-node-directory-cosmos")
include(":extensions:api:federated-catalog-api")
include(":spi:federated-catalog-spi")
include(":launchers")
include(":system-tests:end2end-tests")
