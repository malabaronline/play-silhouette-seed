package services.cardano.wallet
import scala.concurrent.Future

class WalletServiceImpl extends WalletService {

  override def createWallet(walletName: String): Future[CardanoWallet] = ???

  override def addReadOnlyWallet(walletName: String): Future[CardanoWallet] = ???
}
