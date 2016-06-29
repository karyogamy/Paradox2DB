package reader

/**
  * Created by Ataraxia on 27/06/2016.
  */
abstract class ID()
case class VersionID(versionID: Int) extends ID
case class ProvinceID(provinceNumber: Int, versionID: Int) extends ID
