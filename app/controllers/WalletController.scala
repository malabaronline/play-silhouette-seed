package controllers

import com.mohiva.play.silhouette.api.actions._
import forms.{ AddReadOnlyWalletForm, CreateWalletForm }
import play.api.mvc._
import services.cardano.wallet.WalletService

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

/**
 * The basic application controller.
 */
class WalletController @Inject() (
  scc: SilhouetteControllerComponents,
  wallets: views.html.wallets,
  walletDetails: views.html.walletDetails,
  createWallet: views.html.createWallet,
  walletService: WalletService
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def listWallets: Action[AnyContent] = SecuredAction { implicit request: SecuredRequest[EnvType, AnyContent] =>
    Ok(wallets(request.identity))
  }

  def create: Action[AnyContent] = SecuredAction.async { implicit request =>
    CreateWalletForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(createWallet(request.identity, form, AddReadOnlyWalletForm.form))),
      data => {
        walletService.createWallet(data).map(cardanoWallet => Ok(walletDetails(request.identity, cardanoWallet)))
      }
    )
  }

  def createWalletPage: Action[AnyContent] = SecuredAction { implicit request =>
    Ok(createWallet(request.identity, CreateWalletForm.form, AddReadOnlyWalletForm.form))
  }

  def addReadOnly(): Action[AnyContent] = SecuredAction.async { implicit request =>
    AddReadOnlyWalletForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(createWallet(request.identity, CreateWalletForm.form, form))),
      data => {
        walletService.addReadOnlyWallet(data).map(cardanoWallet => Ok(walletDetails(request.identity, cardanoWallet)))
      }
    )
  }

}
