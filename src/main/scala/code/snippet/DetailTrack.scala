package code.snippet

import net.liftmodules.widgets.flot.{FlotSerie, FlotAxisOptions, FlotOptions, Flot}
import net.liftweb.http.js.JE._
import net.liftweb.http.{DispatchSnippet, S}
import code.model.Track
import net.liftweb.mapper.By
import code.lib.{GPXMetaDataHelper, TrackHelper}
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full, Box, Logger}
import scala.util.control.Exception._
import com.droelf.gpx.gpxtype.{GPXMetadata, GPXDecoder}
import net.liftweb.util.Helpers
import Helpers._
import scala.xml.NodeSeq
import org.joda.time.DateTime
import net.liftweb.http.js.{JsCmd, JsObj}
import net.liftweb.http.js.JsCmds.{JsCrVar, OnLoad, Script}
import net.liftweb.common.Full
import scala.Some
import net.liftweb.http.js.JE.JsObj
import net.liftweb.http.js.JsObj
import net.liftweb.json.JArray


class DetailTrack extends DispatchSnippet with TrackHelper with Logger with GPXMetaDataHelper{


  def getTrackIdFromSesstion : Box[Long] = {
    catching(classOf[NumberFormatException]) opt S.param("id").map(o => o.toLong).openOr(0L)
  }


  lazy val track = Track.find(By(Track.id, getTrackIdFromSesstion.getOrElse(0L)))

  lazy val analyseTrack = GPXDecoder.decodeFromFile(track.map(_.path.is).getOrElse("Not Found"))


  def show = {
    track.map{
      single(_) &
      ".row" #> (trackDetailsToMap ++ getTracks ).map{
          e =>
            ".key" #> e._1 &
            ".value" #> e._2
      }
    } openOr("*" #> "That track does not exist.")
  }

  def trackDetailsToMap : Map[String, String] =
    analyseTrack.metadata match {
      case None => Map("Metadata" -> "Not available")
      case Some(x) => getMetadata(x)
    }



  def getWaypoints() : Map[String, String] = analyseTrack.waypoints.map(e => (e.cmt.getOrElse("<unknwon>"), e.latitude +" " + e.longitude)).toMap

  def getTracks() : Map[String, String] = analyseTrack.tracks.map(e => (e.name.getOrElse("<unknwon>") , e.trackSegments.size.toString)).toMap

  def isoStringToMillis(isoString : Option[String]) : Double = isoString match { case Some(x) => DateTime.parse(x).getMillis.toDouble case None => 0.0}

  def getElevationFromTrack() : List[(Double, Double)] = analyseTrack.tracks.map(e => ( e.trackSegments.map(f => f.trackPoints.map(g => (isoStringToMillis(g.time), g.ele.getOrElse(0.0F).toDouble) ) ) )).flatten.flatten.filter(a => (a._1!=0))

  def getElecationFromWaypoints() : List[(Double, Double)] = analyseTrack.waypoints.map(e => (isoStringToMillis(e.time), e.ele.getOrElse(0.0F).toDouble)).filter(a => (a._1!=0))

  def getWaypointsForMap() : List[JsObj] = analyseTrack.waypoints.map(e => makeLocation(e.name.getOrElse("<unknown>"), e.latitude, e.longitude))

  def getTracksForMap() : List[JsObj] = analyseTrack.tracks.map(e => e.trackSegments.map(f => f.trackPoints.map(g => makeLocation(g.name.getOrElse("<unknown>"), g.latitude, g.longitude )))).flatten.flatten

  def plotGraph(in : NodeSeq) : NodeSeq = {
    val options = new FlotOptions(){
      override val xaxis = Full( new FlotAxisOptions() {
        override val mode = Full("time")
      })
    }
    val dataTracks = new FlotSerie {
      override def data = getElevationFromTrack
      override def color = Full(Left("blue"))

    }

    val dataWP = new FlotSerie {
      override def data = getElecationFromWaypoints
      override def color = Full(Left("red"))

    }

    Flot.render("elevation", List(dataTracks, dataWP), options, Flot.script(in))
  }

  def makeLocation(title: String, lat: Float, lng: Float): JsObj = {
    JsObj(("title", title),
      ("lat", lat.toString),
      ("lng", lng.toString))
  }

  def ajaxFunc(locobj: List[JsObj]): JsCmd = {
    JsCrVar("locations", JsObj(("loc", JsArray(locobj: _*)))) & JsRaw("drawmap(locations)").cmd
  }

  def map(in : NodeSeq) : NodeSeq = {

    val waypoitns: List[JsObj] = getWaypointsForMap()
    val tracks : List[JsObj] = getTracksForMap()

    val locations = JsObj(("wp", JsArray(waypoitns: _*))) :: JsObj(("trk", JsArray(tracks: _*))) :: List()

    (<head>
      {Script(OnLoad(ajaxFunc(locations)))}
    </head>)

  }



  override def dispatch: DispatchIt = {
    case "show" => show
    case "plotGraph" =>plotGraph
    case "map" => map
  }
}
