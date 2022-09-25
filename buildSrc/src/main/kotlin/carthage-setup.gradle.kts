tasks.register<Exec>("carthageUpdate") {
    group = "Carthage"
    description = "Update Carthage dependencies"
    executable = "carthage"
    args(
        "update",
        "--project-directory",
        rootProject.projectDir,
        "--use-xcframeworks",
        "--cache-builds"
    )
}

tasks.register<Delete>("carthageClean") {
    group = "Carthage"
    description = "Clean Carthage dependencies"
    delete(
        rootDir.resolve("Carthage"),
        rootDir.resolve("Cartfile.resolved")
    )
}
