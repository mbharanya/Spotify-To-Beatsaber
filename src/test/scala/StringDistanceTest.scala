import com.github.vickumar1981.stringdistance.StringDistance.{Levenshtein, NGram}
import org.scalatest.matchers.should._
import org.scalatest.flatspec.AnyFlatSpec


class StringDistanceTest extends AnyFlatSpec with Matchers {

  "StringDistance" should "get the optimal distance" in {

    val shouldMatch = List(
    "Thirty Seconds To Mars This Is War" -> "this is war - thirty seconds to mars",
      "Queen Don't Stop Me Now - 2011 Mix" -> "don't stop me now - queen"
    )
    shouldMatch.foreach(matches =>{
      println(s"ngram ${matches}", NGram.score(matches._1, matches._2))
      println(s"Levenshtein ${matches}", Levenshtein.score(matches._1, matches._2))
      println()
      //      ngram should be < 0.5
    })
  }

  it should "not match unsimilar" in {
    val shouldNotMatch = List(
      "Closure in Moscow Here’s To Entropy" -> "break in to break out",
      "Skindred Nobody" -> "shinigami - nobody",
      "Dan Bull Destiny" -> "destiny - headhunterz",
      "Dan Bull Halo 5 Epic Rap" -> "creeper rap - dan bull",
      "Eluveitie Druid - Re-recorded" -> "rhapsody of fire - legendary tales (re-recorded)",
      "Eluveitie Thousandfold" -> "eluveitie - the call of the mountains",
    )

    shouldNotMatch.foreach(matches =>{
      println(s"ngram ${matches}", NGram.score(matches._1, matches._2))
      println(s"Levenshtein ${matches}", Levenshtein.score(matches._1, matches._2))
      println()

      //      ngram should be < 0.5
    })

  }


}
