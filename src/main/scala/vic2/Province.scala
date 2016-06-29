package vic2
import eug.shared.GenericObject
import org.jooq.impl.DSL._
import org.jooq.impl.SQLDataType
import org.jooq.{DSLContext, Field, Table}
import reader.{ObjectTable, ProvinceID, VersionID}

/**
  * Created by Ataraxia on 08/06/2016.
  */
object Province extends ObjectTable {
  import ProvinceFields._

  override val fields: Seq[Field[_]] = Seq(
    province_id,
    province_number,
    name,
    owner,
    controller,
    garrison,
    colonial,
    life_rating,
    last_imigration,
    crime,
    version_id
  )
  override val self: Table[_] = table("province")

  override val uniqueFields = Seq(province_number, version_id)
  override val primaryFields = Seq(province_id)

  private val idSequence = sequence("province_id_sequence")

  override def create(context: DSLContext): Unit = {
    import ProvinceFields._
    context.createSequenceIfNotExists(idSequence).execute()

    val primaryKey = constraint("PROVINCE_PK").primaryKey(primaryFields: _*)
    val uniqueKeys = constraint("PROVINCE_UK").unique(uniqueFields: _*)
    val versionKeys = Version.primaryForeignKey("PROVINCE_VERSION_FK", version_id)

    context.createTableIfNotExists(self)
      .columns(fields: _*)
      .constraints(primaryKey, uniqueKeys, versionKeys)
      .execute()
  }

  override type IDType = VersionID
  override def insert(context: DSLContext, province: GenericObject, id: IDType): Option[Int] = {
    import util.EUGInterop._
    import collection.JavaConverters._

    val query = context.insertInto(self, fields: _*)

    val provinceProps = province.valueMap()

    val provinceNumber = province.name.toInt
    val vs = Seq(
      idSequence.nextval(),
      provinceNumber,
      provinceProps.getOrElse("name", ""),
      provinceProps.getOrElse("owner", ""),
      provinceProps.getOrElse("controller", ""),
      provinceProps.getOrElse("garrison", "0"),
      provinceProps.getOrElse("colonial", "0"),
      provinceProps.getOrElse("life_rating", "0"),
      provinceProps.getOrElse("last_imigration", ""),
      provinceProps.getOrElse("crime", "0"),
      id.versionID
    )

    query.values(vs.asJavaCollection).execute()

    val provinceID = ProvinceID(provinceNumber, id.versionID)

    val except = Seq("rgo", "building_construction", "party_loyalty", "unit_names", "modifier", "military_construction")
    val pops = province.childrenSeq().filter( child => !except.contains(child.name) )

    Pop.insert(context, pops, provinceID)

    None
  }
}

object ProvinceFields {
  val province_id =     field("province_id",      SQLDataType.INTEGER.nullable(false))
  val province_number = field("province_number",  SQLDataType.INTEGER.nullable(false))
  val name =            field("name",             SQLDataType.VARCHAR)
  val owner =           field("owner",            SQLDataType.VARCHAR.nullable(false))
  val controller =      field("controller",       SQLDataType.VARCHAR)
  val garrison =        field("garrison",         SQLDataType.FLOAT)
  val colonial =        field("colonial",         SQLDataType.FLOAT)
  val life_rating =     field("life_rating",      SQLDataType.FLOAT)
  val last_imigration = field("last_imigration",  SQLDataType.VARCHAR)
  val crime =           field("crime",            SQLDataType.FLOAT)
  val version_id =      field("version_id",       SQLDataType.INTEGER.nullable(false))
}