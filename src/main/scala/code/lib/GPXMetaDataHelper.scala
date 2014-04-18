package code.lib

import com.droelf.gpx.gpxtype._
import scala.Some


trait GPXMetaDataHelper {

  def getStringOrElse(data : Option[String]) : String = data.getOrElse("")

  def getMap(name : String, data : String) : Map[String, String] = if (data.length > 0)  Map(name -> data) else Map()

  def getMapFromGPXLink(data : Option[GPXTypeLink]) : String = data match{
    case Some(x) => x.href
    case None => ""
  }

  def getMapFromGPXCopyright(data : Option[GPXTypeCopyright]) : String = data match {
    case Some(x) => x.author + ", " + getStringOrElse(x.license) + " " + getStringOrElse(x.year)
    case None => ""
  }

  def getMapFromGPXBounds(data: Option[GPXTypeBounds]) : String = data match{
    case Some(x) => "Max Lat: " + x.maxLat + " Max Lon: "+ x.maxLon + " Min Lat: " + x.minLat + " Min Lon: " + x.minLon
    case None => ""
  }

  def getMapFromGPXTypeEmail(data : Option[GPXTypeEmail]) : String = data match {
    case Some(x) => x.id + "@" + x.domain
    case None => ""
  }

  def getMapFromGPXPerson(data: Option[GPXTypePerson]) : String = data match{
    case Some(x) => "Name: " + getStringOrElse(x.name) +" Link: " + getMapFromGPXLink(x.link) + " Email: " +getMapFromGPXTypeEmail(x.email)
    case None => ""
  }

  def getMetadata(metadata : GPXMetadata) : Map[String, String]  ={
    getMap("Name", getStringOrElse(metadata.name))++ getMap("Description", getStringOrElse(metadata.desc)) ++ getMap("Date", getStringOrElse(metadata.time)) ++
    getMap("Link", getMapFromGPXLink(metadata.link)) ++ getMap("Keywords", getStringOrElse(metadata.keywords)) ++ getMap("Copyright", getMapFromGPXCopyright(metadata.copyright)) ++
    getMap("Bounds", getMapFromGPXBounds(metadata.bounds)) ++ getMap("Author", getMapFromGPXPerson(metadata.author))
  }


}
