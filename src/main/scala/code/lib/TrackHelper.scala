package code.lib

import net.liftweb._,
common.Loggable,
util.Helpers._,
util.CssSel

import code.model.{User, Track}
import net.liftweb.mapper.By
import scala.xml.Text

trait TrackHelper {

  protected def single(track: Track): CssSel =
    ".name" #> track.name &
    ".desc" #> track.description &
    "a [href]" #> "/track/%s".format(track.id.toString) &
    ".user" #> User.find(By(User.id, track.user)).map(a =>  a.firstName + " " + a.lastName)


  protected def many(tracks: List[Track]) = tracks.map(t => single(t))

}
