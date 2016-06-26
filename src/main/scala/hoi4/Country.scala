package hoi4

import eug.shared.GenericObject
import org.jooq.impl.DSL._
import org.jooq.impl.SQLDataType
import org.jooq.{Constraint, DSLContext, Field, Table}

/**
  * Created by Ataraxia on 06/06/2016.
  */
object Country extends ObjectTable {
  import CountryFields._

  override val fields: Seq[Field[_]] = Seq(
    country_id,
    country_code,
    instances_counter,
    research_slot,
    capital,
    original_capital,
    national_unity,
    major,
    is_top_ic_country,
    num_ships,
    focus_tree,
    version_id
  )

  override val self: Table[_] = table("country")
  override val primaryFields = Seq(country_id)
  override val uniqueFields = Seq(country_code, version_id)

  private val countrySequence = sequence("country_id_sequence")

  private val primaryKey = constraint("COUNTRY_PK").primaryKey(primaryFields: _*)
  private val uniqueKeys = constraint("COUNTRY_UK").unique(uniqueFields: _*)

  override def create(context: DSLContext): Unit = {
    context.createSequenceIfNotExists(countrySequence).execute()

    context.createTableIfNotExists(self)
      .columns(fields: _*)
      .constraints(primaryKey, uniqueKeys, Version.primaryForeignKey("COUNTRY_VERSION_FK", version_id))
      .execute()
  }

  override def insert(context: DSLContext,
                      country: GenericObject,
                      versionID: Option[Int]): Option[Int] = {
    import util.EUGInterop._
    import collection.JavaConverters._

    val order = context.insertInto(self, fields: _*)

    val countryValues = country.valueMap()
    val vs = Seq(
      countrySequence.nextval(),
      country.name,
      countryValues.getOrElse("instances_counter", "0"),
      countryValues.getOrElse("research_slot", "0"),
      countryValues.getOrElse("capital", "0"),
      countryValues.getOrElse("original_capital", "0"),
      countryValues.getOrElse("national_unity", "0"),
      countryValues.getOrElse("major", "no"),
      countryValues.getOrElse("is_top_ic_country", "no"),
      countryValues.getOrElse("num_ships", "0"),
      countryValues.getOrElse("focus_tree", "0"),
      versionID.getOrElse(0)
    )

    order.values(vs.asJavaCollection).execute()

    None
  }
}

private object CountryFields {
  val country_id =        field("country_id", SQLDataType.INTEGER.nullable(false))
  val country_code =      field("country_code", SQLDataType.VARCHAR.nullable(false))
  val instances_counter = field("instances_counter", SQLDataType.INTEGER)
  val research_slot =     field("research_slot", SQLDataType.INTEGER)
  val capital =           field("capital", SQLDataType.INTEGER)
  val original_capital =  field("original_capital", SQLDataType.INTEGER)
  val national_unity =    field("national_unity", SQLDataType.FLOAT)
  val major =             field("major", SQLDataType.BOOLEAN)
  val is_top_ic_country = field("is_top_ic_country", SQLDataType.BOOLEAN)
  val num_ships =         field("num_ships", SQLDataType.INTEGER)
  val focus_tree =        field("focus_tree", SQLDataType.VARCHAR)
  val version_id =        field("version_id", SQLDataType.INTEGER.nullable(false))
}