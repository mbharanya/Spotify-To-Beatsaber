import com.github.vickumar1981.stringdistance.StringDistance.NGram
import org.scalatest.matchers.should._
import org.scalatest.flatspec.AnyFlatSpec


class BeatsaverSongResourceTest extends AnyFlatSpec with Matchers{
  implicit val ngram: (String,String) => Double = NGram.score

  "Beatsaver.com" should "return values from API" in {
    val either = BeatsaverSongResource.find("bad lip reading", "seagulls")
    either.right.get.downloadUrl shouldNot have length(0)
    either.right.get.name shouldNot have length(0)
  }
  it should "not get results if there are none" in {
    val either = BeatsaverSongResource.find("Skindred", "Ratrace")
    either match {
      case Right(_) => fail()
      case Left(_) => succeed
    }
  }


}
