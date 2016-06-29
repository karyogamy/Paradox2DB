package hoi4

import eug.shared.GenericObject
import org.jooq.DSLContext
import org.jooq.impl.DSL._
import org.jooq.impl.SQLDataType
import reader.{VersionID, ObjectTable}

object Version extends ObjectTable {
  import VersionField._

  override val fields = Seq(
    version_id,
    player,
    ideology,
    date,
    difficulty,
    version,
    save_version,
    session,
    game_unique_seed,
    unit,
    average_major_ic,
    checksum
  )

  override val self = table("version")
  override val primaryFields = Seq(version_id)
  override val uniqueFields = Seq(date, version, session, game_unique_seed)

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
      versionValues.getOrElse("player", ""),
      versionValues.getOrElse("ideology", ""),
      toDate(versionValues.getOrElse("date", ""), "yyyy.mm.dd.hh"),
      versionValues.getOrElse("difficulty", "0"),
      versionValues.getOrElse("version", ""),
      versionValues.getOrElse("save_version", "0"),
      versionValues.getOrElse("session", "0"),
      versionValues.getOrElse("game_unique_seed", "0"),
      versionValues.getOrElse("unit", "0"),
      versionValues.getOrElse("average_major_ic", "0"),
      versionValues.getOrElse("checksum", "")
    )

    order.values(vs.asJavaCollection).execute()

    Some(context.currval(versionSequence).intValue())
  }
}

private object VersionField {
  val version_id =        field("version_id", SQLDataType.INTEGER.nullable(false))
  val player =            field("player", SQLDataType.VARCHAR)
  val ideology =          field("ideology", SQLDataType.VARCHAR)
  val date =              field("date", SQLDataType.DATE.nullable(false))
  val difficulty =        field("difficulty", SQLDataType.INTEGER)
  val version =           field("version", SQLDataType.VARCHAR.nullable(false))
  val save_version =      field("save_version", SQLDataType.INTEGER)
  val session =           field("session", SQLDataType.INTEGER.nullable(false))
  val game_unique_seed =  field("game_unique_seed", SQLDataType.INTEGER.nullable(false))
  val unit =              field("unit", SQLDataType.INTEGER)
  val average_major_ic =  field("average_major_ic", SQLDataType.FLOAT)
  val checksum =          field("checksum", SQLDataType.VARCHAR)
}