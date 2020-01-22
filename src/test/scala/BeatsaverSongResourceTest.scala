import org.scalatest.matchers.should._
import org.scalatest.flatspec.AnyFlatSpec

import collection.mutable.Stack


class BeatsaverSongResourceTest extends AnyFlatSpec with Matchers{
  "Beatsaver.com" should "return values from API" in {
    val dlUrl = BeatsaverSongResource.findDownloadUrl("bad lip reading", "seagulls")
    dlUrl.downloadUrl shouldNot have length(0)
  }



}
