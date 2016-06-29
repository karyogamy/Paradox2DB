package hoi4
import eug.shared.GenericObject
import org.jooq.impl.DSL._
import org.jooq.impl.SQLDataType
import org.jooq.{DSLContext, Field, Table}
import reader.{ObjectNotFoundException, ObjectTable, VersionID}

/**
  * Created by Ataraxia on 08/06/2016.
  */
object State extends ObjectTable {
  import StateFields._

  override val fields: Seq[Field[_]] = Seq(
    state_id,
    state_number,
    owner,
    demilitarized,
    is_border_conflict,
    state_category,
    available_manpower_pool,
    locked_manpower_pool,
    total_manpower_pool,
    version_id
  )
  override val self: Table[_] = table("state")

  override val uniqueFields = Seq(state_number, version_id)
  override val primaryFields = Seq(state_id)

  private val idSequence = sequence("state_id_sequence")

  override def create(context: DSLContext): Unit = {
    import CountryFields._
    context.createSequenceIfNotExists(idSequence).execute()

    val primaryKey = constraint("STATE_PK").primaryKey(primaryFields: _*)
    val uniqueKeys = constraint("STATE_UK").unique(uniqueFields: _*)
    val versionKeys = Version.primaryForeignKey("STATE_VERSION_FK", version_id)
    val countryKeys = Country.uniqueForeignKey("STATE_COUNTRY_FK", owner, version_id)

    context.createTableIfNotExists(self)
      .columns(fields: _*)
      .constraints(primaryKey, uniqueKeys, versionKeys, countryKeys)
      .execute()
  }

  override type IDType = VersionID
  override def insert(context: DSLContext, state: GenericObject, id: IDType): Option[Int] = {
    import util.EUGInterop._
    import collection.JavaConverters._

    val query = context.insertInto(self, fields: _*)

    val countryProps = state.valueMap()
    val manpowerProps = state.child("manpower_pool").getOrElse(throw new ObjectNotFoundException("manpower_pool")).valueMap()

    val vs = Seq(
      idSequence.nextval(),
      state.name,
      countryProps.getOrElse("owner", throw new IllegalArgumentException("owner cannot be empty.")),
      countryProps.getOrElse("demilitarized", "no"),
      countryProps.getOrElse("is_border_conflict", "no"),
      countryProps.getOrElse("state_category", ""),
      manpowerProps.getOrElse("available", "0"),
      manpowerProps.getOrElse("locked", "0"),
      manpowerProps.getOrElse("total", "0"),
      id.versionID
    )

    query.values(vs.asJavaCollection).execute()

    None
  }
}

object StateFields {
  val state_id =                field("state_id",               SQLDataType.INTEGER.nullable(false))
  val state_number =            field("state_number",           SQLDataType.INTEGER.nullable(false))
  val owner =                   field("owner",                  SQLDataType.VARCHAR.nullable(false))
  val demilitarized =           field("demilitarized",          SQLDataType.BOOLEAN)
  val is_border_conflict =      field("is_border_conflict",     SQLDataType.BOOLEAN)
  val state_category =          field("state_category",         SQLDataType.VARCHAR)
  val available_manpower_pool = field("available_manpower_pool",SQLDataType.INTEGER)
  val locked_manpower_pool =    field("locked_manpower_pool",   SQLDataType.INTEGER)
  val total_manpower_pool =     field("total_manpower_pool",    SQLDataType.INTEGER)
  val version_id =              field("version_id",             SQLDataType.INTEGER.nullable(false))
}