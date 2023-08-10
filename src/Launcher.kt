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
    // EE
    get("/c7af9aa83c5451aec8f900fd7e79e9d8f33eaf0c6d540d731e5b7b1b9b64415a") {
      Parser.openUrl("https://www.auto24.ee/kasutatud/nimekiri.php?bn=2&a=100&ae=1&af=10000&otsi=otsi&ak=0",
        hashMapOf("PHPSESSID" to "",
                  "cf_clearance" to "",
                  "CID" to ""))
    }

    // LT
    get("/1cd9a1739192fe1ef77e217f0deb59c7378d16b88fc9cc7679a15a31de846239") {
      Parser.openUrl("https://en.autoplius.lt/ads/used-cars?make_id_list=&engine_capacity_from=&engine_capacity_to=&power_from=&power_to=&kilometrage_from=&kilometrage_to=&has_damaged_id=&condition_type_id=&make_date_from=&make_date_to=&sell_price_from=&sell_price_to=&co2_from=&co2_to=&euro_id=&fk_place_countries_id=&qt=&qt_autocomplete=&number_of_seats_id=&number_of_doors_id=&gearbox_id=&steering_wheel_id=&older_not=&order_by=3&order_direction=DESC&page_nr=1",
        hashMapOf())
    }

    // PL
    get("/ef7358b0a8c9fd5e9b4c32f3b5adc00a87cf0109cad9d9827bf74598a8585ae0") {
      Parser.openUrl("https://gratka.pl/motoryzacja/osobowe", hashMapOf())
    }
    get("/186a9795bca02a4bc5a27944cdc2f51d803eb6a609601feae0c31e42340d7854") {
      Parser.openUrl("https://www.otomoto.pl/osobowe", hashMapOf())
    }

    // FI
    get("/7d4f21558d112ca60a7e780087b99c68763c0208a9ec2763d97ce908bc8fa842") {
      Parser.openUrl("https://www.nettiauto.com/en/listAdvSearchFindAgent.php?id=227134189&tb=tmp_find_agent&PN[0]=adv_search&PL[0]=en/advSearch.php?id_country[]=73@qs=Y@id_domicile=0?id=227134189@tb=tmp_find_agent",
        hashMapOf())
    }

    // DE
    get("/9ba1ec00c99adbabfde14784db00b33d0d22cc85e38cdb175641c2df7bd82b13") {
      Parser.openUrl("https://en.autode.net/search?brand=&model=0", hashMapOf())
    }
  }


  context("/api") {
    useOnly<JsonBody>() // in case only json should be supported in this context
    useHashCodeAsETag() // automatically send 304 NotModified if request generates the same response as before
    annotated<MyRoutes>() // read routes from an annotated class - such classes are easier to unit-test
  }
}
