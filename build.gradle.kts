import org.gradle.internal.os.OperatingSystem

plugins {
	kotlin("multiplatform") version "1.6.10"
}

repositories {
	mavenCentral()
}

kotlin {
	val host: OperatingSystem = OperatingSystem.current()
	val nativeTarget = when {
		host.isLinux -> linuxX64("native")
		host.isMacOsX -> macosX64("native")
		host.isWindows -> mingwX64("native")
		else -> error("unsupported host '${host.name}'")
	}

	nativeTarget.apply {
		binaries {
			executable {
				entryPoint = "kotlin_llvm.main"
				runTask?.standardInput = System.`in`
			}
		}
		compilations.named("main") {
			cinterops.create("llvm")
		}
	}
}
