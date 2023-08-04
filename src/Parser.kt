import org.jsoup.Jsoup


object Parser {

    fun openUrl(url: String): String {
        val doc = Jsoup.connect(url)
            .timeout(10*1000)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            .ignoreHttpErrors(true)
            .referrer("https://www.google.ee")
            .get()
        return doc.text()
    }

}