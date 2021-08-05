package models.errors

case class MandatoryFieldMissingException(message: String) extends RuntimeException(message)