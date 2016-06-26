package reader

/**
  * Created by Ataraxia on 07/06/2016.
  */
sealed class ReaderException(error: String) extends Exception(error)

sealed case class ObjectNotFoundException(objectName: String) extends ReaderException(s"Object [$objectName] is not found.")
