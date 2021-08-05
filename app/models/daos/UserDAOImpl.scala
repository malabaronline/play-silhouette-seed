package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.daos.UserDAOImpl._
import models.errors.RedisInsertFailedException
import scredis.Redis

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

/**
 * Give access to the user object.
 */
class UserDAOImpl @Inject() (redis: Redis) extends UserDAO {

  implicit private val ec = redis.dispatcher

  /**
   * Finds a user by its login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    for {
      userOpt <- redis.get(userLoginInfoKey(loginInfo)) flatMap {
        case Some(userId) => getUserOpt(UUID.fromString(userId), loginInfo)
        case None => Future.successful(None)
      }
    } yield userOpt
  }

  /**
   * Finds a user by its user ID.
   */
  def find(userID: UUID): Future[Option[User]] = {
    //TODO Change providerID = "", providerKey = ""
    getUserOpt(userID, LoginInfo(providerID = "", providerKey = ""))
  }

  private def getUserOpt(userID: UUID, loginInfo: LoginInfo): Future[Option[User]] = {
    for {
      userOpt <- redis.hGetAll(userKey(userID)) map {
        case Some(userMap) => Some(User(userID, userMap, loginInfo))
        case None => None
      }
    } yield userOpt
  }

  /**
   * Saves a user.
   */
  def save(user: User): Future[User] = {
    (for {
      _ <- redis.hmSet(userKey(user.userID), user.toMap)
      userOpt <- getUserOpt(user.userID, user.loginInfo)
    } yield userOpt).flatMap {
      case Some(value) => Future.successful(value)
      case None => Future.failed(RedisInsertFailedException("while saving user"))
    }
  }

}

object UserDAOImpl {

  private def userKey(userId: UUID): String = s"online.malabar.services.user.${userId.toString}"
  private def userLoginInfoKey(loginInfo: LoginInfo): String = s"online.malabar.services.user.${loginInfo.providerID}:${loginInfo.providerKey}"

}
