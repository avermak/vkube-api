import com.google.protobuf.gradle.*

plugins {
    java
    idea
    id("io.vertx.vertx-plugin") version "1.1.1"
    id("com.google.protobuf") version "0.8.18"
}

group = "com.avermak"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val protobufVersion = "3.19.1"
val grpcVersion = "1.42.1"
val protobufPluginVersion = "0.8.18"

dependencies {
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("io.vertx:vertx-grpc-server:4.3.5")
    implementation("io.vertx:vertx-web:4.3.5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

vertx {
    mainVerticle = "com.avermak.vkube.api.hello.MainVerticle"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {task ->
            task.plugins {
                id("grpc") {
                    outputSubDir = "grpc"
                }
            }
            task.generateDescriptorSet = true;
        }
    }
}