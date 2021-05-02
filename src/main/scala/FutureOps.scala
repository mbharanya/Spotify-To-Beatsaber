import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FutureFlattenOps {
  def flatten[A](f: Future[Option[Future[A]]]): Future[Option[A]] =
    f.flatMap({
      case None => Future.successful(None)
      case Some(g) => g.map(Some(_))
    })

  def flattenList[A](f: Future[Option[Future[List[A]]]]): Future[List[A]] =
    f.flatMap({
      case None => Future.successful(Nil)
      case Some(g) => g.flatMap {
        _ match {
          case list => Future.successful(list)
          case Nil => Future.successful(Nil)
        }
      }
    })

}
