package vic2

import eug.shared.GenericObject
import org.jooq.impl.DSL._
import org.jooq.impl.SQLDataType
import org.jooq.{Constraint, DSLContext, Field, Table}
import reader.{ObjectTable, VersionID}

/**
  * Created by Ataraxia on 06/06/2016.
  */
object Country extends ObjectTable {
  import CountryFields._

  override val fields: Seq[Field[_]] = Seq(
    country_id,
    country_code,
    tax_base,
    capital,
    last_election,
    wage_reform,
    work_hours,
    safety_regulations,
    unemployment_subsidies,
    pensions,
    health_care,
    school_reforms,
    slavery,
    vote_franschise,
    upper_house_composition,
    voting_system,
    public_meetings,
    press_rights,
    trade_unions,
    political_parties,
    pre_indust,
    war_exhaustion,
    diplomatic_points,
    religion,
    plurality,
    revanchism,
    overseas_penalty,
    leadership,
    auto_assign_leaders,
    auto_create_leaders,
    last_mission_cancel,
    schools,
    prestige,
    money,
    last_bankrupt,
    civilized,
    badboy,
    next_quarterly_pulse,
    next_yearly_pulse,
    suppression,
    is_releasable_vassal,
    version_id
  )

  override val self: Table[_] = table("country")
  override val primaryFields = Seq(country_id)
  override val uniqueFields = Seq(country_code, version_id)

  private val countrySequence = sequence("country_id_sequence")

  override def create(context: DSLContext): Unit = {
    context.createSequenceIfNotExists(countrySequence).execute()

    val primaryKey = constraint("COUNTRY_PK").primaryKey(primaryFields: _*)
    val uniqueKeys = constraint("COUNTRY_UK").unique(uniqueFields: _*)
    val versionKeys: Constraint = Version.primaryForeignKey("COUNTRY_VERSION_FK", version_id)

    context.createTableIfNotExists(self)
      .columns(fields: _*)
      .constraints(primaryKey, uniqueKeys, versionKeys)
      .execute()
  }

  override type IDType = VersionID
  override def insert(context: DSLContext, country: GenericObject, id: IDType): Option[Int] = {
    import util.EUGInterop._
    import collection.JavaConverters._

    val order = context.insertInto(self, fields: _*)

    val countryValues = country.valueMap()
    val vs = Seq(
      countrySequence.nextval(),
      country.name,
      countryValues.getOrElse("tax_base",                 "0"),
      countryValues.getOrElse("capital",                  "0"),
      toDate(countryValues.getOrElse("last_election", ""), "yyyy.mm.dd"),
      countryValues.getOrElse("wage_reform",              ""),
      countryValues.getOrElse("work_hours",               ""),
      countryValues.getOrElse("safety_regulations",       ""),
      countryValues.getOrElse("unemployment_subsidies",   ""),
      countryValues.getOrElse("pensions",                 ""),
      countryValues.getOrElse("health_care",              ""),
      countryValues.getOrElse("school_reforms",           ""),
      countryValues.getOrElse("slavery",                  ""),
      countryValues.getOrElse("vote_franschise",          ""),
      countryValues.getOrElse("upper_house_composition",  ""),
      countryValues.getOrElse("voting_system",            ""),
      countryValues.getOrElse("public_meetings",          ""),
      countryValues.getOrElse("press_rights",             ""),
      countryValues.getOrElse("trade_unions",             ""),
      countryValues.getOrElse("political_parties",        ""),
      countryValues.getOrElse("pre_indust",               ""),
      countryValues.getOrElse("war_exhaustion",           "0"),
      countryValues.getOrElse("diplomatic_points",        "0"),
      countryValues.getOrElse("religion",                 ""),
      countryValues.getOrElse("plurality",                "0"),
      countryValues.getOrElse("revanchism",               "0"),
      countryValues.getOrElse("overseas_penalty",         "0"),
      countryValues.getOrElse("leadership",               "0"),
      countryValues.getOrElse("auto_assign_leaders",      "no"),
      countryValues.getOrElse("auto_create_leaders",      "no"),
      countryValues.getOrElse("last_mission_cancel",      ""),
      countryValues.getOrElse("schools",                  ""),
      countryValues.getOrElse("prestige",                 "0"),
      countryValues.getOrElse("money",                    "0"),
      countryValues.getOrElse("last_bankrupt",            ""),
      countryValues.getOrElse("civilized",                "no"),
      countryValues.getOrElse("badboy",                   "0"),
      countryValues.getOrElse("next_quarterly_pulse",     ""),
      countryValues.getOrElse("next_yearly_pulse",        ""),
      countryValues.getOrElse("suppression",              "0"),
      countryValues.getOrElse("is_releasable_vassal",     "no"),
      id.versionID
    )

    order.values(vs.asJavaCollection).execute()

    None
  }
}

private object CountryFields {
  val country_id =              field("country_id",               SQLDataType.INTEGER.nullable(false))
  val country_code =            field("country_code",             SQLDataType.VARCHAR.nullable(false))
  val tax_base =                field("tax_base",                 SQLDataType.FLOAT)
  val capital =                 field("capital",                  SQLDataType.INTEGER)
  val last_election =           field("last_election",            SQLDataType.DATE)
  val wage_reform =             field("wage_reform",              SQLDataType.VARCHAR)
  val work_hours =              field("work_hours",               SQLDataType.VARCHAR)
  val safety_regulations =      field("safety_regulations",       SQLDataType.VARCHAR)
  val unemployment_subsidies =  field("unemployment_subsidies",   SQLDataType.VARCHAR)
  val pensions =                field("pensions",                 SQLDataType.VARCHAR)
  val health_care =             field("health_care",              SQLDataType.VARCHAR)
  val school_reforms =          field("school_reforms",           SQLDataType.VARCHAR)
  val slavery =                 field("slavery",                  SQLDataType.VARCHAR)
  val vote_franschise =         field("vote_franschise",          SQLDataType.VARCHAR)
  val upper_house_composition = field("upper_house_composition",  SQLDataType.VARCHAR)
  val voting_system =           field("voting_system",            SQLDataType.VARCHAR)
  val public_meetings =         field("public_meetings",          SQLDataType.VARCHAR)
  val press_rights =            field("press_rights",             SQLDataType.VARCHAR)
  val trade_unions =            field("trade_unions",             SQLDataType.VARCHAR)
  val political_parties =       field("political_parties",        SQLDataType.VARCHAR)
  val pre_indust =              field("pre_indust",               SQLDataType.VARCHAR)
  val war_exhaustion =          field("war_exhaustion",           SQLDataType.VARCHAR)
  val diplomatic_points =       field("diplomatic_points",        SQLDataType.FLOAT)
  val religion =                field("religion",                 SQLDataType.VARCHAR)
  val plurality =               field("plurality",                SQLDataType.FLOAT)
  val revanchism =              field("revanchism",               SQLDataType.FLOAT)
  val overseas_penalty =        field("overseas_penalty",         SQLDataType.FLOAT)
  val leadership =              field("leadership",               SQLDataType.FLOAT)
  val auto_assign_leaders =     field("auto_assign_leaders",      SQLDataType.BOOLEAN)
  val auto_create_leaders =     field("auto_create_leaders",      SQLDataType.BOOLEAN)
  val last_mission_cancel =     field("last_mission_cancel",      SQLDataType.VARCHAR)
  val schools =                 field("schools",                  SQLDataType.VARCHAR)
  val prestige =                field("prestige",                 SQLDataType.FLOAT)
  val money =                   field("money",                    SQLDataType.FLOAT)
  val last_bankrupt =           field("last_bankrupt",            SQLDataType.VARCHAR)
  val civilized =               field("civilized",                SQLDataType.BOOLEAN)
  val badboy =                  field("badboy",                   SQLDataType.FLOAT)
  val next_quarterly_pulse =    field("next_quarterly_pulse",     SQLDataType.VARCHAR)
  val next_yearly_pulse =       field("next_yearly_pulse",        SQLDataType.VARCHAR)
  val suppression =             field("suppression",              SQLDataType.FLOAT)
  val is_releasable_vassal =    field("is_releasable_vassal",     SQLDataType.BOOLEAN)
  val version_id =              field("version_id",               SQLDataType.INTEGER.nullable(false))
}