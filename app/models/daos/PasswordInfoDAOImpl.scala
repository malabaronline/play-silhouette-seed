package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.daos.PasswordInfoDAOImpl.{ fromPasswordInfo, passwordLoginInfoKey, toPasswordInfo }
import models.errors.RedisInsertFailedException
import scredis.Redis

import javax.inject.Inject
import scala.concurrent.Future
import scala.reflect.ClassTag

class PasswordInfoDAOImpl @Inject() (redis: Redis) extends DelegableAuthInfoDAO[PasswordInfo] {

  override val classTag: ClassTag[PasswordInfo] = ClassTag(classOf[PasswordInfo])
  implicit private val ec = redis.dispatcher

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = getPasswordOpt(loginInfo)

  override def add(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = {
    (for {
      _ <- redis.hmSet(passwordLoginInfoKey(loginInfo), fromPasswordInfo(passwordInfo))
      passwordInfoOpt <- getPasswordOpt(loginInfo)
    } yield passwordInfoOpt) flatMap {
      case Some(value) => Future.successful(value)
      case None => Future.failed(RedisInsertFailedException("while saving passwordInfo"))
    }
  }

  override def update(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = add(loginInfo, passwordInfo)

  override def save(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] = add(loginInfo, passwordInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] = redis.del(passwordLoginInfoKey(loginInfo)).map(_ => ())

  private def getPasswordOpt(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    redis.hGetAll(passwordLoginInfoKey(loginInfo)) map {
      case Some(map) => Some(toPasswordInfo(map))
      case None => None
    }
  }

}

object PasswordInfoDAOImpl {

  private def passwordLoginInfoKey(loginInfo: LoginInfo): String = s"online.malabar.services.password.${loginInfo.providerID}:${loginInfo.providerKey}"

  def toPasswordInfo(map: Map[String, String]): PasswordInfo = {
    PasswordInfo(
      hasher = map.getOrElse("hasher", ""),
      password = map.getOrElse("password", ""),
      salt = map.get("salt") match {
        case Some(value) if value.nonEmpty => Some(value)
        case _ => None
      }
    )
  }

  def fromPasswordInfo(passwordInfo: PasswordInfo): Map[String, String] = {
    Map(
      "hasher" -> passwordInfo.hasher,
      "password" -> passwordInfo.password,
      "salt" -> passwordInfo.salt.getOrElse("")
    )
  }
}
