import klite.*
import klite.annotations.annotated
import klite.jdbc.*
import klite.json.JsonBody
import kotlinx.coroutines.delay
import java.net.InetSocketAddress
import java.net.http.HttpClient
import java.nio.file.Path
import java.time.Duration.ofSeconds

fun main() {
    sampleServer().start()
}

fun sampleServer(port: Int = 8080) = Server(listen = InetSocketAddress(port)).apply {
    Config.useEnvFile()
    use<JsonBody>() // enables parsing/sending of application/json requests/responses, depending on the Accept header

//    if (!Config.isDev) startDevDB() // start docker-compose db automatically
//    use(DBModule(PooledDataSource())) // configure a DataSource
//    use<DBMigrator>() //  migrate the DB
//    use<RequestTransactionHandler>() // runs each request in a transaction

    assets("/", AssetsHandler(Path.of("public"), useIndexForUnknownPaths = true))

    register(HttpClient.newBuilder().connectTimeout(ofSeconds(5)).build())

//  before<AdminChecker>()
//  after { ex, err -> ex.header("X-Error", err?.message ?: "none") }

    context("/hello") {
        get {
            val imageUrls = arrayOf(
                "<img src=\"https://img12.img-bcg.eu/auto24/used/other/900/175045900.jpg\"<br>",
                "<img src=\"https://img12.img-bcg.eu/auto24/used/other/900/175045900.jpg\"<br>",
                "<img src=\"https://img12.img-bcg.eu/auto24/used/other/900/175045900.jpg\"<br>",
                "<img src=\"https://img12.img-bcg.eu/auto24/used/other/900/175045900.jpg\"<br>",
                "<img src=\"https://img12.img-bcg.eu/auto24/used/other/900/175045900.jpg\"<br>",
                "<img src=\"https://img12.img-bcg.eu/auto24/used/other/900/175045900.jpg\"<br>"
            )

            val responseString = buildString { imageUrls.forEach { appendLine(it) } }

            this.send(StatusCode.OK, responseString)
        }

        get("/delay") {
            delay(1000)
            "Waited for 1 sec"
        }

        get("/failure") { error("Failure") }

        get("/admin") @AdminOnly {
            "Only for admins"
        }

        get("/param/:param") {
            "Path: ${path("param")}, Query: $queryParams"
        }

        post("/post") {
            data class JsonRequest(val required: String, val hello: String = "World")
            body<JsonRequest>()
        }

        decorator { ex, h -> "<${h(ex)}>" }
        get("/decorated") { "!!!" }
    }


    context("/scrape") {
        // EE
        get("/auto24/main") {
            SeleniumParser.initialize()

            val responseString = buildString {
                SeleniumParser.scrapeSearchResults().forEach { imageUrl ->
                    appendLine("<img src=$imageUrl alt=\"Scraped Image\">")
                }
            }
            this.send(StatusCode.OK, responseString)
        }
      get("/auto24/specific") {
        SeleniumParser.initialize()

        val responseString = buildString {
          SeleniumParser.scrapeSearchResultsSpecific().forEach { imageUrl ->
            appendLine(imageUrl)
          }
        }
        this.send(StatusCode.OK, responseString)
      }

    }



context("/api") {
    useOnly<JsonBody>() // in case only json should be supported in this context
    useHashCodeAsETag() // automatically send 304 NotModified if request generates the same response as before
    annotated<MyRoutes>() // read routes from an annotated class - such classes are easier to unit-test
}
}
