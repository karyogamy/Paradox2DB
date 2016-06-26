package reader

import java.io.File
import java.sql.DriverManager

import eug.parser.EUGFileIO
import util.EUGInterop._
import org.jooq.SQLDialect
import org.jooq.impl.DSL

/**
  * Created by Ataraxia on 30/05/2016.
  */
object Reader {

  type SaveDirectory = String

  def main(args: Array[String]) = {
    args match {
      case Array(folderLocation: SaveDirectory) =>
        val folder = new File(folderLocation)
        folder.listFiles().sorted.foreach(readHoi4(_))
        server()

        System.exit(0)
      case _ => printUsage()
    }
  }

  private def printUsage() = {
    println(s"Usage: ${this.getClass.getCanonicalName} <file location>")

    System.exit(1)
  }

  private def server() = {
    import org.h2.tools.Server

    //TODO - jmo: break this up in gui
    val webServer = Server.createWebServer().start()
    val tcpServer = Server.createTcpServer("-tcp","-tcpAllowOthers","-tcpPort","9092").start()

    println(s"Web Server Running: ${webServer.isRunning(true)}....")
    println(s"TCP Server Running: ${tcpServer.isRunning(true)}....")

    //TODO - jmo: remove this after gui is done
    while(true)()
  }

  private def readVic2(file: File) = {
    import vic2._

    val connection = DriverManager.getConnection("jdbc:h2:mem:vic2;DATABASE_TO_UPPER=false", "sa", "")
    val context = DSL.using(connection, SQLDialect.H2)

    val readFile = EUGFileIO.load(file)

    val child = readFile.childrenSeq()

    val countryRegex = "[A-Z]{3}".r
    val countries = child.filter(block => countryRegex.pattern.matcher(block.name).matches())

    val provinceRegex = "\\d+".r
    val provinces = child.filter(block => provinceRegex.pattern.matcher(block.name).matches())

    Province.insertProvinces(context, provinces)
  }

  private def readHoi4(file: File): Unit = {
    import hoi4._

    val connection = DriverManager.getConnection("jdbc:h2:mem:hoi4;DATABASE_TO_UPPER=false", "sa", "")
    val context = DSL.using(connection, SQLDialect.H2)

    val readFile = EUGFileIO.load(file)
    val savedObjects = readFile.childrenSeq()

    val tables = Seq(Version, Country, State)
    tables.foreach( _.create(context) )

    val versionID = Version.insert(context, readFile)

    val countries = savedObjects.find( _.name == "countries").getOrElse(throw new ObjectNotFoundException("countries"))
    countries.childrenSeq().foreach( country => Country.insert(context, country, versionID) )

    val states = savedObjects.find( _.name == "states").getOrElse(throw new ObjectNotFoundException("states"))
    states.childrenSeq().foreach( state => State.insert(context, state, versionID) )
  }
}
