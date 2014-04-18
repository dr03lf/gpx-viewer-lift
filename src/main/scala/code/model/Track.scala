package code.model

import net.liftweb.mapper._
import net.liftweb.common.{Box, Full}


object Track extends Track
  with LongKeyedMetaMapper[Track] {
  override def dbTableName = "tracks"
}

class Track extends  LongKeyedMapper[Track]
  with IdPK
  with CreatedUpdated{

  override def getSingleton: KeyedMetaMapper[Long, Track] = Track

  object name extends MappedString(this, 150)
  object description extends MappedString(this, 150)
  object path extends MappedString(this,150)

  object user extends MappedLongForeignKey(this, User){
    override def dbColumnName = "user_id"

    override def validSelectValues =
      Full(Track.findMap(){
        case s: Track => Full(s.id.is -> s.name.is)
      })
  }


}
