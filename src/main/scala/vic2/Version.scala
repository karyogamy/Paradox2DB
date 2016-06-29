package vic2

import eug.shared.GenericObject
import org.jooq.DSLContext
import org.jooq.impl.DSL._
import org.jooq.impl.SQLDataType
import reader.{VersionID, ObjectTable}

object Version extends ObjectTable {
  import VersionField._

  override val fields = Seq(
    version_id,
    date,
    player,
    government,
    automate_trade,
    automate_sliders,
    rebel,
    unit,
    state,
    start_date,
    start_pop_index,
    great_wars_enabled,
    player_monthly_pop_growth_tag,
    player_monthly_pop_growth_date
  )

  override val self = table("version")
  override val primaryFields = Seq(version_id)
  override val uniqueFields = Seq(date, player, start_pop_index)

  private val versionSequence = sequence("version_id_sequence")

  private val primaryKey = constraint("VERSION_PK").primaryKey(primaryFields: _*)
  private val uniqueKeys = constraint("VERSION_UK").unique(uniqueFields: _*)

  override def create(context: DSLContext): Unit = {
    context.createSequenceIfNotExists(versionSequence).execute()

    context.createTableIfNotExists(self)
      .columns(fields: _*)
      .constraints(primaryKey, uniqueKeys)
      .execute()
  }

  //TODO - jmo: get rid of this dummy id hack
  override type IDType = VersionID
  override def insert(context: DSLContext,
                      version: GenericObject,
                      unused: IDType = VersionID(-1)): Option[Int] = {
    import util.EUGInterop._
    import collection.JavaConverters._

    val order = context.insertInto(self, fields: _*)

    val versionValues = version.valueMap()
    val vs = Seq(
      versionSequence.nextval(),
      toDate(versionValues.getOrElse("date", ""), "yyyy.mm.dd"),
      versionValues.getOrElse("player", "UNKNOWN_PLAYER"),
      versionValues.getOrElse("government", "0"),
      versionValues.getOrElse("automate_trade", "no"),
      versionValues.getOrElse("automate_sliders", "0"),
      versionValues.getOrElse("rebel", "0"),
      versionValues.getOrElse("unit", "0"),
      versionValues.getOrElse("state", "0"),
      toDate(versionValues.getOrElse("start_date", ""), "yyyy.mm.dd"),
      versionValues.getOrElse("start_pop_index", "0"),
      versionValues.getOrElse("great_wars_enabled", "no"),
      versionValues.getOrElse("player_monthly_pop_growth_tag", "UNKNOWN_PLAYER"),
      toDate(versionValues.getOrElse("player_monthly_pop_growth_date", ""), "yyyy.mm.dd")
    )

    order.values(vs.asJavaCollection).execute()

    Some(context.currval(versionSequence).intValue())
  }
}

private object VersionField {
  val version_id =                      field("version_id", SQLDataType.INTEGER.nullable(false))
  val date =                            field("date", SQLDataType.DATE.nullable(false))
  val player =                          field("player", SQLDataType.VARCHAR)
  val government =                      field("government", SQLDataType.INTEGER)
  val automate_trade =                  field("automate_trade", SQLDataType.BOOLEAN)
  val automate_sliders =                field("automate_sliders", SQLDataType.FLOAT)
  val rebel =                           field("rebel", SQLDataType.INTEGER)
  val unit =                            field("unit", SQLDataType.INTEGER)
  val state =                           field("state", SQLDataType.INTEGER)
  val start_date =                      field("start_date", SQLDataType.DATE)
  val start_pop_index =                 field("start_pop_index", SQLDataType.INTEGER)
  val great_wars_enabled =              field("great_wars_enabled", SQLDataType.BOOLEAN)
  val player_monthly_pop_growth_tag =   field("player_monthly_pop_growth_tag", SQLDataType.VARCHAR)
  val player_monthly_pop_growth_date =  field("player_monthly_pop_growth_date", SQLDataType.VARCHAR)
}
