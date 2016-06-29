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
  type Directory = File

  def main(args: Array[String]) = {
    args match {
      case Array(folderLocation: SaveDirectory) =>
        val folder = new File(folderLocation)
        readVic2(folder)
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

  private def readVic2(folder: Directory) = {
    import vic2._

    val connection = DriverManager.getConnection("jdbc:h2:mem:vic2;DATABASE_TO_UPPER=false", "sa", "")
    val context = DSL.using(connection, SQLDialect.H2)

    Version.create(context)
    Country.create(context)
    Province.create(context)
    Pop.create(context)

    folder.listFiles().sorted.foreach {
      file =>
        val readFile = EUGFileIO.load(file)
        val saveObjects = readFile.childrenSeq()

        val versionID = Version.insert(context, readFile)
        val id = VersionID(versionID.getOrElse(0))

        val countryRegex = "[A-Z]{3}".r
        val countries = saveObjects.filter(block => countryRegex.pattern.matcher(block.name).matches())
        countries.foreach( country => Country.insert(context, country, id) )

        val provinceRegex = "\\d+".r
        val provinces = saveObjects.filter(block => provinceRegex.pattern.matcher(block.name).matches())

        val t0 = System.nanoTime()
        provinces.foreach( province => Province.insert(context, province, id) )
        val t1 = System.nanoTime()

        println(s"Pop loading took ${t1 - t0} ns.")
    }
  }

  private def readHoi4(folder: Directory): Unit = {
    import hoi4._

    val connection = DriverManager.getConnection("jdbc:h2:mem:hoi4;DATABASE_TO_UPPER=false", "sa", "")
    val context = DSL.using(connection, SQLDialect.H2)

    val tables = Seq(Version, Country, State)
    tables.foreach( _.create(context) )

    folder.listFiles().sorted.foreach {
      file =>
        val readFile = EUGFileIO.load(file)
        val savedObjects = readFile.childrenSeq()

        val versionID = Version.insert(context, readFile)
        val id = VersionID(versionID.getOrElse(0))

        val countries = savedObjects.find( _.name == "countries").getOrElse(throw new ObjectNotFoundException("countries"))
        countries.childrenSeq().foreach( country => Country.insert(context, country, id) )

        val states = savedObjects.find( _.name == "states").getOrElse(throw new ObjectNotFoundException("states"))
        states.childrenSeq().foreach( state => State.insert(context, state, id) )
    }
  }
}
