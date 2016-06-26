package hoi4

import eug.shared.GenericObject
import org.jooq.impl.DSL._
import org.jooq.{Constraint, DSLContext, Field, Table}

import scala.reflect.ClassTag

/**
  * Created by Ataraxia on 08/06/2016.
  */
trait ObjectTable {

  /** Fields for this table in database. */
  val fields: Seq[Field[_]]

  /** Primary key field of this table in database. */
  val primaryFields: Seq[Field[_]]

  /** Unique key fields of this table in database.
    *
    * Currently, only one unique key set is allowed to be exposed.
    * */
  val uniqueFields: Seq[Field[_]] = primaryFields

  /** Reference to this table in database. */
  val self: Table[_]

  /** Creates this table in database. */
  def create(context: DSLContext): Unit

  /** Construct and insert a row of the table type into the database.
    *
    * @param context JOOQ database connection for insertion.
    * @param dataObject GenericObject which contains all properties and values insert.
    * @param versionID Partial key for the row, versionID is needed if more than one save is being dumped.
    * @return The primary key (if one exists) for this row.
    * */
  def insert(context: DSLContext, dataObject: GenericObject, versionID: Option[Int]): Option[Int]

  /** Given key name, makes a foreign key constraint to this table using its primary key(s). */
  def primaryForeignKey(name: String, fields: Field[_]*): Constraint = {
    constraint(name).foreignKey(fields: _*).references(self, primaryFields: _*)
  }

  /** Given key name, makes a foreign key constraint to this table using its unique key(s).
    * If there is no unique key, then this defaults to primaryForeignKey.
    */
  def uniqueForeignKey(name: String, fields: Field[_]*): Constraint = {
    constraint(name).foreignKey(fields: _*).references(self, uniqueFields: _*)
  }
}
