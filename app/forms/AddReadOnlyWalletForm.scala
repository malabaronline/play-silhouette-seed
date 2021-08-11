package forms

import play.api.data.Form
import play.api.data.Forms._

object AddReadOnlyWalletForm {

  val form = Form(
    mapping(
      "name" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    name: String)
}
