import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

object SeleniumParser {
    private lateinit var driver: WebDriver

    fun initialize() {

        val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36")
        options.addArguments("--disable-dev-shm-usage")
        
        driver = ChromeDriver(options)
        println(driver)
    }


    fun scrapeSearchResults(): List<String> {
        driver.get("https://auto24.ee/")

        // Find the div element with classname "special"
        val specialDiv: WebElement = driver.findElement(By.cssSelector(".special"))
        // Get the HTML content of the special div
        val specialDivHtml = specialDiv.getAttribute("outerHTML")

        // Find all image elements within the special div
        val imageElements: List<WebElement> = specialDiv.findElements(By.tagName("img"))

        // Get the sources of the images
        val imageSources = imageElements.map { it.getAttribute("src") }


        return imageSources

    }

    fun scrapeSearchResultsSpecific(): List<String> {
        driver.get("https://www.auto24.ee/kasutatud/nimekiri.php")

        // Find the div element with classname "special"
        val specialDiv: WebElement = driver.findElement(By.id("usedVehiclesSearchResult-flex"))
        println(specialDiv)
        // Get the HTML content of the special div
        val specialDivHtml = specialDiv.getAttribute("outerHTML")
        println(specialDiv.text)

        // Find all image elements within the special div
        val imageElements: List<WebElement> = specialDiv.findElements(By.className("small-image"))
        imageElements.forEach { image -> println(image.text) }
        // Get the sources of the images
        val imageSources = imageElements.map { it.getAttribute("a") }

        return imageSources

    }

    fun close() {
        driver.quit()
    }
}