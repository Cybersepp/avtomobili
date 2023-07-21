rootProject.name = "klite"

sourceControl {
    gitRepository(java.net.URI("https://github.com/codeborne/klite.git")) {
        producesModule("com.github.codeborne.klite:server")
        producesModule("com.github.codeborne.klite:jdbc")
    }
}