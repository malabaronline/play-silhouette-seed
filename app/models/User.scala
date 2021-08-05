package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

/**
 * The user object.
 *
 * @param userID The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param fullName Maybe the full name of the authenticated user.
 * @param email Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 * @param activated Indicates that the user has activated its registration.
 */
case class User(
  userID: UUID,
  loginInfo: LoginInfo,
  fullName: String,
  email: String,
  avatarURL: Option[String],
  activated: Boolean) extends Identity {

  /**
   * Tries to construct a name.
   *
   * @return Maybe a name.
   */
  def name = fullName

  def toMap: Map[String, String] = {
    Map(
      "userID" -> userID.toString,
      "fullName" -> fullName,
      "email" -> email,
      "avatarURL" -> avatarURL.getOrElse(""),
      "activated" -> activated.toString)
  }

}

object User {

  def apply(userID: UUID, map: Map[String, String], loginInfo: LoginInfo): User = {
    User(
      userID = userID,
      loginInfo = loginInfo,
      fullName = map.getOrElse("fullName", ""),
      email = map.getOrElse("email", ""),
      avatarURL = map.get("avatarURL") match {
        case Some(value) if value.nonEmpty => Some(value)
        case _ => None
      },
      activated = map.get("activated").exists(_.toBoolean))
  }
}
