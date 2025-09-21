plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.nviaud"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.0.1"
extra["springCloudVersion"] = "2025.0.0"

dependencies {
    implementation (project(":lib-events"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    implementation("org.springframework.ai:spring-ai-advisors-vector-store")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-milvus")
//    implementation("org.springframework.boot:spring-boot-starter-cache")
//    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
//    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
//    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.amqp:spring-rabbit-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-rabbit")
//    implementation("org.apache.kafka:kafka-streams")
//    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams")
//    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.30")
    implementation("technology.tabula:tabula:1.0.4") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
    implementation("net.sourceforge.tess4j:tess4j:5.11.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
//    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        //mavenBom("io.milvus:milvus-sdk-java-bom:2.4.4")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}

// Integration tests configuration

configurations {
    val testIntegrationImplementation by creating {
        extendsFrom(configurations.testImplementation.get())
        // Declaration only, not resolvable
    }
    val testIntegrationRuntimeOnly by creating {
        extendsFrom(configurations.testRuntimeOnly.get())
        // Declaration only, not resolvable
    }
    val testIntegrationCompileClasspath by creating {
        extendsFrom(testIntegrationImplementation)
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    val testIntegrationRuntimeClasspath by creating {
        extendsFrom(testIntegrationRuntimeOnly)
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

sourceSets {
    val testIntegration by creating {
        kotlin.srcDir("src/test-integration/kotlin")
        resources.srcDir("src/test-integration/resources")
        compileClasspath += sourceSets["main"].output + configurations["testIntegrationCompileClasspath"]
        runtimeClasspath += output + compileClasspath + sourceSets["main"].runtimeClasspath + configurations["testIntegrationRuntimeClasspath"]
    }
}

tasks.register<Test>("testIntegration") {
    testClassesDirs = sourceSets["testIntegration"].output.classesDirs
    classpath = sourceSets["testIntegration"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

tasks.register<Exec>("dockerComposeUp") {
    group = "docker"
    description = "Start Docker Compose services"
    commandLine("docker-compose", "up")
}

tasks.register<Exec>("dockerComposeDown") {
    group = "docker"
    description = "Stop Docker Compose services"
    commandLine("docker-compose", "down")
}

tasks.named<Test>("test") {
    dependsOn("dockerComposeUp")
    finalizedBy("dockerComposeDown")
}
