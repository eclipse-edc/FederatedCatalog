rootProject.name = "federated-catalog"

include(":core:federated-catalog")
include(":extensions:store:fcc-node-directory-cosmos")
include(":extensions:api:federated-catalog-api")
include(":spi:federated-catalog-spi")
include(":launchers")
include(":system-tests:component-tests")
include(":system-tests:end2end-test:connector-runtime")
include(":system-tests:end2end-test:catalog-runtime")
include(":system-tests:end2end-test:e2e-junit-runner")


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
            from("org.eclipse.edc:edc-versions:0.0.1-20221220-SNAPSHOT")
        }
        // create version catalog for all EDC modules
        create("edc") {
            version("edc", "0.0.1-SNAPSHOT")
            library("spi-catalog", "org.eclipse.edc", "catalog-spi").versionRef("edc")
            library("spi-core", "org.eclipse.edc", "core-spi").versionRef("edc")
            library("spi-web", "org.eclipse.edc", "web-spi").versionRef("edc")
            library("util", "org.eclipse.edc", "util").versionRef("edc")
            library("boot", "org.eclipse.edc", "boot").versionRef("edc")
            library("config-filesystem", "org.eclipse.edc", "configuration-filesystem").versionRef("edc")
            library("core-controlplane", "org.eclipse.edc", "control-plane-core").versionRef("edc")
            library("core-connector", "org.eclipse.edc", "connector-core").versionRef("edc")
            library("core-jetty", "org.eclipse.edc", "jetty-core").versionRef("edc")
            library("core-jersey", "org.eclipse.edc", "jersey-core").versionRef("edc")
            library("junit", "org.eclipse.edc", "junit").versionRef("edc")
            library("api-management-config", "org.eclipse.edc", "management-api-configuration").versionRef("edc")
            library("api-management", "org.eclipse.edc", "management-api").versionRef("edc")
            library("api-observability", "org.eclipse.edc", "api-observability").versionRef("edc")
            library("ext-http", "org.eclipse.edc", "http").versionRef("edc")
            library("spi-ids", "org.eclipse.edc", "ids-spi").versionRef("edc")
            library("ids", "org.eclipse.edc", "ids").versionRef("edc")
            library("iam-mock", "org.eclipse.edc", "iam-mock").versionRef("edc")
            library("ext-azure-cosmos-core", "org.eclipse.edc", "azure-cosmos-core").versionRef("edc")
            library("ext-azure-test", "org.eclipse.edc", "azure-test").versionRef("edc")

            // DPF modules
            library("dpf-transferclient", "org.eclipse.edc", "data-plane-transfer-client").versionRef("edc")
            library("dpf-selector-client", "org.eclipse.edc", "data-plane-selector-client").versionRef("edc")
            library("dpf-selector-spi", "org.eclipse.edc", "data-plane-selector-spi").versionRef("edc")
            library("dpf-selector-core", "org.eclipse.edc", "data-plane-selector-core").versionRef("edc")
            library("dpf-framework", "org.eclipse.edc", "data-plane-framework").versionRef("edc")

            bundle(
                "connector",
                listOf("boot", "core-connector", "core-jersey", "core-controlplane", "api-observability")
            )

            bundle(
                "dpf",
                listOf(
                    "dpf-transferclient",
                    "dpf-selector-client",
                    "dpf-selector-spi",
                    "dpf-selector-core",
                    "dpf-framework"
                )
            )
        }
    }
}
