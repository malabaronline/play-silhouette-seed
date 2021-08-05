package models.errors

case class RedisInsertFailedException(message: String) extends RuntimeException(message)