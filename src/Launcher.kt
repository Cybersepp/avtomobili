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

  if (Config.isDev) startDevDB() // start docker-compose db automatically
  use(DBModule(PooledDataSource())) // configure a DataSource
  use<DBMigrator>() //  migrate the DB
  use<RequestTransactionHandler>() // runs each request in a transaction

  assets("/", AssetsHandler(Path.of("public"), useIndexForUnknownPaths = true))

  register(HttpClient.newBuilder().connectTimeout(ofSeconds(5)).build())

  before<AdminChecker>()
  after { ex, err -> ex.header("X-Error", err?.message ?: "none") }

  context("/hello") {
    get { "Hello World" }

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

    // TODO control page number using param

    get("/auto24") {
      Parser.openUrl("https://www.auto24.ee/kasutatud/nimekiri.php?bn=2&a=100&ae=1&af=10000&otsi=otsi&ak=0")
    }

    get("/autoplius") {
      Parser.openUrl("https://en.autoplius.lt/ads/used-cars?make_id_list=&engine_capacity_from=&engine_capacity_to=&power_from=&power_to=&kilometrage_from=&kilometrage_to=&has_damaged_id=&condition_type_id=&make_date_from=&make_date_to=&sell_price_from=&sell_price_to=&co2_from=&co2_to=&euro_id=&fk_place_countries_id=&qt=&qt_autocomplete=&number_of_seats_id=&number_of_doors_id=&gearbox_id=&steering_wheel_id=&older_not=&order_by=3&order_direction=DESC&page_nr=1")
    }
  }


  context("/api") {
    useOnly<JsonBody>() // in case only json should be supported in this context
    useHashCodeAsETag() // automatically send 304 NotModified if request generates the same response as before
    annotated<MyRoutes>() // read routes from an annotated class - such classes are easier to unit-test
  }
}
