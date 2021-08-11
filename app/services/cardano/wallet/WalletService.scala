package services.cardano.wallet

import forms.{ AddReadOnlyWalletForm, CreateWalletForm }

import scala.concurrent.Future

trait WalletService {
  def createWallet(form: CreateWalletForm.Data): Future[CardanoWallet] = createWallet(form.name)
  def createWallet(walletName: String): Future[CardanoWallet]
  def addReadOnlyWallet(form: AddReadOnlyWalletForm.Data): Future[CardanoWallet] = createWallet(form.name)
  def addReadOnlyWallet(walletName: String): Future[CardanoWallet]
}
