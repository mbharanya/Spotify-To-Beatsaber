import java.io.File
import java.net.URL
import java.nio.file.Files

import org.apache.commons.io.IOUtils


object Downloader {
  def download(url: String, filename: String) ={
    val requestProperties = Map(
      "User-Agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:72.0) Gecko/20100101 Firefox/72.0"
    )

    Thread.sleep(100)

    val connection = new URL(url).openConnection
    requestProperties.foreach({
      case (name, value) => connection.setRequestProperty(name, value)
    })

    Files.write(new File(filename).toPath, IOUtils.toByteArray(connection.getInputStream))
    println(s"Downloaded ${new File(filename).getAbsolutePath}")
  }
}
