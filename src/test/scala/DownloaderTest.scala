import java.io.File
import java.nio.file.{Files, Paths}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.matchers.should.Matchers


class DownloaderTest extends AnyFlatSpec with Matchers {
  "Downloader" should "download url" in {
    val testFile = "67e4aee65b16a1a59e47d6404a71c321c1e1ab41.zip"
    Downloader.download("https://beatsaver.com/cdn/3364/67e4aee65b16a1a59e47d6404a71c321c1e1ab41.zip", testFile )
    val path = Paths.get(testFile)
    Files.exists(path) should be(true)
    Files.size(path) shouldNot be(0)
    Files.delete(path)
  }

  it should "download url as well" in {
    val list = List(
      "https://beatsaver.com/cdn/1482/236228d06dfc7855c14569e8b97ba785ccd0d477.zip",
      "https://beatsaver.com/cdn/4070/0dba780be553e3f55db0198c6f78a468405c4724.zip",
      "https://beatsaver.com/cdn/7d48/30892ca188fba3f0c36e767581751b62bbdb4fff.zip",
      "https://beatsaver.com/cdn/7f54/80bb81177cd5299555a1ca11af0cc32adbebbeac.zip",
      "https://beatsaver.com/cdn/2520/9fd2e4495696a5ea70e23ba0717d084a5b619f5e.zip",
      "https://beatsaver.com/cdn/351a/0e5bc16713f04198f9e42145b967b7790c5e5660.zip"
    )

    list.foreach(url => {
      val location = url.substring(url.lastIndexOf("/") + 1, url.length)
      Downloader.download(url, location )

      val path = Paths.get(location)
      Files.exists(path) should be(true)
      Files.size(path) shouldNot be(0)
      Files.delete(path)
    })

  }

}
