package modules

import com.google.inject.{ AbstractModule, Provides }
import models.daos.{ AuthTokenDAO, AuthTokenDAOImpl }
import models.services.{ AuthTokenService, AuthTokenServiceImpl }
import net.codingwell.scalaguice.ScalaModule
import scredis.Redis
import services.cardano.wallet.{ WalletService, WalletServiceImpl }

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[WalletService].to[WalletServiceImpl]
  }

  @Provides
  def provideRedis(): Redis = {
    new Redis()
  }

}
