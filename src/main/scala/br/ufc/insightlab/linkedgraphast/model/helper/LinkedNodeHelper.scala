package br.ufc.insightlab.linkedgraphast.model.helper

import java.io.File
import java.sql.{Connection, DriverManager, ResultSet, Statement}

import org.apache.commons.io.FileUtils
import org.insightlab.graphast.exceptions.NodeNotFoundException
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.Map

object LinkedNodeHelper {
//  val store: IDStore = SQLiteIDStore
  val store: IDStore = MapIDStore

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

//  def getNodeId(node: Node, generateId: Boolean = true): Long =
//    getNodeId(node.getComponent(classOf[LinkedNodeComponnent]).value, generateId)

  def getNodeIdByURI(URI: String, generateId: Boolean = true): Long =
    store.get(URI, generateId)

  def setURIId(URI: String, id: Long): Unit = {
    store.setId(URI,id)
  }

}

trait IDStore {
  var nextId = 0
  def get (URI: String, generateId: Boolean): Long
  def setId(URI: String, id: Long): Unit
}

object MapIDStore extends IDStore{
  val URIToId: Map[String,Long] = Map()

  def get (URI: String, generateId: Boolean): Long = {
    //    log.debug(s"Getting id to $URI")
    val id: Option[Long] = URIToId.get(URI)

    if(id.isDefined) id.get
    else if(generateId){
      URIToId.put(URI, this.nextId)
      this.nextId += 1

      this.nextId - 1
    }
    else throw new NodeNotFoundException(s"$URI not found")
  }

  def setId(URI: String, id: Long): Unit = {
    URIToId(URI) = id
  }
}

object SQLiteIDStore extends IDStore{

  var c: Connection = _
  var stmt: Statement = _

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  Class.forName("org.sqlite.JDBC")
  loadDB(reset = true)

  def loadDB(path: String = "uris.db", reset: Boolean = false): Unit = {
    log.debug(s"Loading database $path")
    if(reset){
      log.debug(s"Reseting database $path")
      FileUtils.deleteQuietly(new File(path))
      c = DriverManager.getConnection(s"jdbc:sqlite:$path")
      stmt = c.createStatement()
      stmt.executeUpdate(
        s"CREATE TABLE IDS " +
          "(ID INT PRIMARY KEY     NOT NULL," +
          " URI TEXT    NOT NULL UNIQUE)"
      )
    }
    else {
      c = DriverManager.getConnection(s"jdbc:sqlite:$path")
      stmt = c.createStatement()
    }

    log.debug(s"Database $path loaded")
  }

  def closeConnection(): Unit = c.close()

  def get (URI: String, generateId: Boolean): Long = {

    val uri = URI.replace("'","''")

    val rs: ResultSet = stmt.executeQuery(
      s"SELECT * FROM IDS WHERE URI='$uri'"
    )

    if(rs.next()) rs.getLong("ID")
    else if(generateId){
      stmt.executeUpdate(
        "INSERT INTO IDS (ID, URI) VALUES" +
          s"($nextId, '$uri')"
      )

      this.nextId += 1

      this.nextId - 1

    } else throw new NodeNotFoundException(s"$URI not found")
  }

  def setId(URI: String, id: Long): Unit = {
    val uri = URI.replace("'","''")
    stmt.executeUpdate(
      "INSERT OR REPLACE INTO IDS (ID, URI) VALUES" +
        s"($nextId, '$uri')"
    )
  }
}
