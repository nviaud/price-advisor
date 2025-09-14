//https://github.com/node-gradle/gradle-node-plugin/blob/main/docs/usage.md

plugins {
    id("com.github.node-gradle.node") version "7.0.2"
}

node {
    version = "22.19.0"
    npmVersion = "11.6.0"
    download = true
}
