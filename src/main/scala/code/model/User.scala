package code.model

import net.liftweb.mapper._
import net.liftweb.sitemap.Loc.LocGroup
import net.liftweb.common.{Full, Box}
import scala.xml.Node
import net.liftweb.common.Full
import net.liftweb.sitemap.Loc.LocGroup


object User extends User
  with KeyedMetaMapper[Long, User]
  with MetaMegaProtoUser[User]{

  override def dbTableName = "customers"
  override def fieldOrder = id :: firstName :: lastName :: email :: password :: Nil

  override val basePath = "account" :: Nil
  override def homePage = "/"
  override def skipEmailValidation = true
  override def createUserMenuLocParams = LocGroup("public") :: super.createUserMenuLocParams
  override def screenWrap: Box[Node] =
    Full(
      <lift:surround with="default" at="content">
        <div id="box1" class="topbg">
          <lift:msgs showAll="true" />
          <lift:bind />
        </div>
      </lift:surround>
    )


}

class User extends MegaProtoUser[User] with CreatedUpdated with OneToMany[Long,User]{
  override def getSingleton: KeyedMetaMapper[Long, User] = User

  object tracks extends MappedOneToMany(Track, Track.user) with Owned[Track] with Cascade[Track]

}
