package vic2

import eug.shared.GenericObject
import org.jooq.{DSLContext}
import org.jooq.impl.DSL._
import org.jooq.impl.SQLDataType
import reader.{ObjectTable, ProvinceID}

/**
  * Created by Ataraxia on 27/06/2016.
  */
object Pop extends ObjectTable {
  import PopFields._
  override val fields = Seq(
      pop_id,
      pop_number,
      pop_type,
      size,
      culture,
      religion,
      money,
      con,
      literacy,
      bank,
      con_factor,
      everyday_needs,
      luxury_needs,
      random,
      promoted,
      mil,
      production_type,
      last_spending,
      current_producing,
      percent_afforded,
      percent_sold_domestic,
      percent_sold_expert,
      leftover,
      throttle,
      need_cost,
      production_income,
    prov_number,
      version_id
  )

  override val primaryFields = Seq(pop_id)
  override val uniqueFields = Seq(pop_number, prov_number, version_id)

  override val self = table("pop")

  private val idSequence = sequence("pop_id_sequence")

  override type IDType = ProvinceID
  override def insert(context: DSLContext, pop: GenericObject, id: IDType): Option[Int] = {
    import util.EUGInterop._
    import collection.JavaConverters._

    val query = context.insertInto(self, fields: _*)
    
    val culture = pop.getVariable(2) //paradox, why the f-????

    val popProps = pop.valueMap()

    val vs = Seq(
      idSequence.nextval(),
      popProps.getOrElse("id", throw new NullPointerException(s"Pop number cannot be null: $pop")),
      pop.name,
      popProps.getOrElse("size", "0"),
      culture.varname,
      culture.getValue,
      popProps.getOrElse("money", "0"),
      popProps.getOrElse("con", "0"),
      popProps.getOrElse("literacy", "0"),
      popProps.getOrElse("bank", "0"),
      popProps.getOrElse("con_factor", "0"),
      popProps.getOrElse("everyday_needs", "0"),
      popProps.getOrElse("luxury_needs", "0"),
      popProps.getOrElse("random", "0"),
      popProps.getOrElse("promoted", "0"),
      popProps.getOrElse("mil", "0"),
      popProps.getOrElse("production_type", ""),
      popProps.getOrElse("last_spending", "0"),
      popProps.getOrElse("current_producing", "0"),
      popProps.getOrElse("percent_afforded", "0"),
      popProps.getOrElse("percent_sold_domestic", "0"),
      popProps.getOrElse("percent_sold_expert", "0"),
      popProps.getOrElse("leftover", "0"),
      popProps.getOrElse("throttle", "0"),
      popProps.getOrElse("need_cost", "0"),
      popProps.getOrElse("production_income", "0"),
      id.provinceNumber,
      id.versionID
    )

    query.values(vs.asJavaCollection).execute()

    None
  }

  def insert(context: DSLContext, pops: Seq[GenericObject], id: IDType): Option[Int] = {
    import util.EUGInterop._
    import collection.JavaConverters._

    val query = context.insertInto(self, fields: _*)

    pops.foreach {
      pop =>
        val culture = pop.getVariable(2) //paradox, why the f-????

        val popProps = pop.valueMap()

        val vs = Seq(
          idSequence.nextval(),
          popProps.getOrElse("id", throw new NullPointerException(s"Pop number cannot be null: $pop")),
          pop.name,
          popProps.getOrElse("size", "0"),
          culture.varname,
          culture.getValue,
          popProps.getOrElse("money", "0"),
          popProps.getOrElse("con", "0"),
          popProps.getOrElse("literacy", "0"),
          popProps.getOrElse("bank", "0"),
          popProps.getOrElse("con_factor", "0"),
          popProps.getOrElse("everyday_needs", "0"),
          popProps.getOrElse("luxury_needs", "0"),
          popProps.getOrElse("random", "0"),
          popProps.getOrElse("promoted", "0"),
          popProps.getOrElse("mil", "0"),
          popProps.getOrElse("production_type", ""),
          popProps.getOrElse("last_spending", "0"),
          popProps.getOrElse("current_producing", "0"),
          popProps.getOrElse("percent_afforded", "0"),
          popProps.getOrElse("percent_sold_domestic", "0"),
          popProps.getOrElse("percent_sold_expert", "0"),
          popProps.getOrElse("leftover", "0"),
          popProps.getOrElse("throttle", "0"),
          popProps.getOrElse("need_cost", "0"),
          popProps.getOrElse("production_income", "0"),
          id.provinceNumber,
          id.versionID
        )

        query.values(vs.asJavaCollection)
    }
    query.execute()

    None
  }

  override def create(context: DSLContext): Unit = {
    context.createSequenceIfNotExists(idSequence).execute()

    val primaryKey = constraint("POP_PK").primaryKey(primaryFields: _*)
    val uniqueKeys = constraint("POP_UK").unique(uniqueFields: _*)
    val versionKeys = Version.primaryForeignKey("POP_VERSION_FK", version_id)
    val provinceKeys = Province.uniqueForeignKey("POP_PROVINCE_FK", prov_number, version_id)

    context.createTableIfNotExists(self)
      .columns(fields: _*)
      .constraints(primaryKey, uniqueKeys, versionKeys, provinceKeys)
      .execute()
  }
}

private object PopFields {
  val pop_id =          field("id",             SQLDataType.INTEGER.nullable(false))
  val pop_number =      field("number",         SQLDataType.INTEGER.nullable(false))
  val pop_type =        field("type",           SQLDataType.VARCHAR.nullable(false))
  val size =            field("size",           SQLDataType.INTEGER)
  val culture =         field("culture",        SQLDataType.VARCHAR)
  val religion =        field("religion",       SQLDataType.VARCHAR)
  val money =           field("money",          SQLDataType.FLOAT)
  val con =             field("con",            SQLDataType.FLOAT)
  val literacy =        field("literacy",       SQLDataType.FLOAT)
  val bank =            field("bank",           SQLDataType.FLOAT)
  val con_factor =      field("con_factor",     SQLDataType.FLOAT)
  val everyday_needs =  field("everyday_needs", SQLDataType.FLOAT)
  val luxury_needs =    field("luxury_needs",   SQLDataType.FLOAT)
  val random =          field("random",         SQLDataType.INTEGER)
  val promoted =        field("promoted",       SQLDataType.INTEGER)
  val mil =             field("mil",            SQLDataType.FLOAT)

  // artisan-esque specific (e.g. vanilla: artisans, pdm: capitalists)
  // TODO - jmo: move this block into a separate table
  val production_type =       field("production_type",        SQLDataType.VARCHAR)
  val last_spending =         field("last_spending",          SQLDataType.FLOAT)
  val current_producing =     field("current_producing",      SQLDataType.FLOAT)
  val percent_afforded =      field("percent_afforded",       SQLDataType.FLOAT)
  val percent_sold_domestic = field("percent_sold_domestic",  SQLDataType.FLOAT)
  val percent_sold_expert =   field("percent_sold_expert",    SQLDataType.FLOAT)
  val leftover =              field("leftover",               SQLDataType.FLOAT)
  val throttle =              field("throttle",               SQLDataType.FLOAT)
  val need_cost =             field("need_cost",              SQLDataType.FLOAT)
  val production_income =     field("production_income",      SQLDataType.FLOAT)

  //foreign keys
  val prov_number = field("prov_number",  SQLDataType.INTEGER.nullable(false))
  val version_id =  field("version_id",   SQLDataType.INTEGER.nullable(false))
}


