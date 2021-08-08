package models.daos

import models.AuthToken
import models.daos.AuthTokenDAOImpl._
import models.errors.RedisInsertFailedException
import org.joda.time.DateTime
import scredis.Redis

import java.util.UUID
import javax.inject.Inject
import scala.collection.mutable
import scala.concurrent.Future

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOImpl @Inject() (redis: Redis) extends AuthTokenDAO {

  implicit private val ec = redis.dispatcher

  /**
   * Finds a token by its ID.
   */
  def find(id: UUID): Future[Option[AuthToken]] = {
    redis.hGetAll(authTokenKey(id)) map {
      case Some(map) => Some(toAuthToken(map))
      case None => None
    }
  }

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dateTime: DateTime): Future[Seq[AuthToken]] = Future.successful {
    tokens.filter {
      case (_, token) =>
        token.expiry.isBefore(dateTime)
    }.values.toSeq
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] = {
    (for {
      _ <- redis.hmSet(authTokenKey(token.id), fromAuthToken(token))
      passwordInfoOpt <- find(token.id)
    } yield passwordInfoOpt) flatMap {
      case Some(value) => Future.successful(value)
      case None => Future.failed(RedisInsertFailedException("while saving AuthToken"))
    }
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Unit] = {
    tokens -= id
    Future.successful(())
  }
}

/**
 * The companion object.
 */
object AuthTokenDAOImpl {

  /**
   * The list of tokens.
   */
  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()

  private def authTokenKey(id: UUID): String = s"online.malabar.services.auth.auth-token.${id.toString}"

  def toAuthToken(map: Map[String, String]): AuthToken = {
    AuthToken(
      id = UUID.fromString(map.getOrElse("id", "")),
      userID = UUID.fromString(map.getOrElse("userID", "")),
      expiry = DateTime.parse(map.getOrElse("expiry", ""))
    )
  }

  def fromAuthToken(authToken: AuthToken): Map[String, String] = {
    Map(
      "id" -> authToken.id.toString,
      "userID" -> authToken.userID.toString,
      "expiry" -> authToken.expiry.toString
    )
  }

}
