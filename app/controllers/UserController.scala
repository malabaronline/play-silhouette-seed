package controllers

import com.mohiva.play.silhouette.api.actions._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

/**
 * The basic application controller.
 */
class UserController @Inject() (
  scc: SilhouetteControllerComponents,
  userProfile: views.html.profile
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def profile: Action[AnyContent] = UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(userProfile(request.identity, None))
  }

}
