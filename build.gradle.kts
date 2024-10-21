import org.gradle.internal.logging.text.StyledTextOutput.Style.Failure
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
	signing
	`maven-publish`
	`version-catalog`
}

group = "fyi.pauli"

version = "1.6.3"

val authors = setOf(
	"kxmpxtxnt"
)

repositories {
	mavenCentral()
	maven("https://repo.nyon.dev/releases")
}

val githubRepo = "ichor-dev/ichor-catalog"

catalog {
	versionCatalog {
		version("kotlin", "2.0.21")

		plugin("jetbrains.dokka", "org.jetbrains.dokka").version("1.9.20")
		plugin("jetbrains.jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
		plugin("jetbrains.multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
		plugin("jetbrains.serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

		plugin("github.release", "com.github.breadmoirai.github-release").version("2.5.2")

		library("pauli.prorialize", "fyi.pauli.prorialize", "prorialize").version("1.1.3")
		library("pauli.nbterialize", "fyi.pauli.nbterialize", "nbterialize").version("1.0.3")

		library("kotlinx.io", "org.jetbrains.kotlinx", "kotlinx-io-core").version("0.5.4")
		library("kotlinx.datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.6.1")
		library("kotlinx.serialization", "org.jetbrains.kotlinx", "kotlinx-serialization-core").version("1.7.3")
		library("kotlinx.json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.7.3")
		library("kotlinx.coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.9.0")

		bundle(
			"kotlinx", listOf(
				"kotlinx.io", "kotlinx.datetime", "kotlinx.json", "kotlinx.coroutines", "kotlinx.serialization"
			)
		)

		version("ktor", "3.0.0")

		library("ktor.cio", "io.ktor", "ktor-server-cio").versionRef("ktor")
		library("ktor.network", "io.ktor", "ktor-network").versionRef("ktor")
		library("ktor.core", "io.ktor", "ktor-server-core").versionRef("ktor")
		library("ktor.sockets", "io.ktor", "ktor-server-websockets").versionRef("ktor")
		library("ktor.serialization", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")

		bundle(
			"ktor.server", listOf(
				"ktor.cio",
				"ktor.core",
				"ktor.network",
				"ktor.sockets",
				"ktor.serialization",
			)
		)

		library("ktor.client", "io.ktor", "ktor-client-core").versionRef("ktor")
		library("ktor.client.cio", "io.ktor", "ktor-client-cio").versionRef("ktor")
		library("ktor.client.negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")

		bundle(
			"ktor.client", listOf(
				"ktor.client",
				"ktor.client.cio",
				"ktor.client.negotiation",
			)
		)

		version("crypto", "0.4.0")

		library("crypto.core", "dev.whyoleg.cryptography", "cryptography-core").versionRef("crypto")
		library("crypto.providers.jdk", "dev.whyoleg.cryptography", "cryptography-provider-jdk").versionRef("crypto")
		library("crypto.providers.apple", "dev.whyoleg.cryptography", "cryptography-provider-apple").versionRef("crypto")
		library(
			"crypto.providers.openssl", "dev.whyoleg.cryptography", "cryptography-provider-openssl3-api"
		).versionRef("crypto")

		library("uuid", "com.benasher44", "uuid").version("0.8.4")
		library("ktoml", "com.akuleshov7", "ktoml-core").version("0.5.2")
		library("bignum", "com.ionspin.kotlin", "bignum").version("0.3.10")
		library("logging", "io.github.oshai", "kotlin-logging").version("7.0.0")
		library("logging.jvm", "io.github.oshai", "kotlin-logging-jvm").version("7.0.0")
		library("logging.linuxX64", "io.github.oshai", "kotlin-logging-linuxx64").version("7.0.0")

		library("koin.core", "io.insert-koin", "koin-core").version("4.0.0")
		library("koin.coroutines", "io.insert-koin", "koin-core-coroutines").version("4.0.0")

		bundle(
			"koin", listOf("koin.core", "koin.coroutines")
		)

		library("testing.koin", "io.insert-koin", "koin-test").version("4.0.0")
		library("testing.junit", "org.junit.jupiter", "junit-jupiter-engine").version("5.11.3")
		library("testing.kotlin.junit", "org.jetbrains.kotlin", "kotlin-test-junit5").versionRef("kotlin")
		library("testing.kotlin.common", "org.jetbrains.kotlin", "kotlin-test-common").versionRef("kotlin")
		library("testing.kotlin.annotations", "org.jetbrains.kotlin", "kotlin-test-annotations-common").versionRef("kotlin")
	}
}

signing {
	val publications = extensions.getByType<PublishingExtension>().publications
	val publicationCount = publications.size
	val message = "The following $publicationCount publication(s) are getting signed: ${publications.map(Named::getName)}"
	val style = when (publicationCount) {
		0 -> Failure
		else -> Success
	}
	serviceOf<StyledTextOutputFactory>().create("signing").style(style).println(message)
	sign(*publications.toTypedArray())

	val signingTasks = tasks.filter { it.name.startsWith("sign") && it.name.endsWith("Publication") }
	tasks.matching { it.name.startsWith("publish") }.configureEach {
		signingTasks.forEach {
			mustRunAfter(it.name)
		}
	}
}

publishing {
	repositories {

		//Add maven central later.
		maven {
			name = "nyon"
			url = uri("https://repo.nyon.dev/releases")
			credentials {
				username = findProperty("REPO_NYON_USERNAME").toString()
				password = findProperty("REPO_NYON_PASSWORD").toString()
			}
		}
	}

	publications {
		register<MavenPublication>(project.name) {
			from(components["versionCatalog"])

			this.groupId = project.group.toString()
			this.artifactId = project.name
			this.version = version.toString()

			pom {
				name = project.name
				description = project.description

				developers {
					authors.forEach { developer { name = it } }
				}

				licenses {
					license {
						name = "GNU General Public License 3"
						url = "https://www.gnu.org/licenses/gpl-3.0.txt"
					}
				}

				url = "https://github.com/$githubRepo"

				scm {
					connection = "scm:git:git://github.com/$githubRepo.git"
					url = "https://github.com/$githubRepo/tree/main"
				}
			}
		}
	}
}