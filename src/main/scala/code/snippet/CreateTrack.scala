package code.snippet

import code.model.{User, Track}
import scala.xml.NodeSeq
import net.liftweb.http.{FileParamHolder, RequestVar, S, SHtml}
import net.liftweb.util.Helpers._
import net.liftweb.sitemap.Loc.Snippet
import net.liftweb.util.{CssBindFunc, Helpers}
import net.liftweb.common.{Empty, Logger, Box, Full}
import java.io.{FileOutputStream, File}
import scala.reflect.io.Path

class CreateTrack extends Logger {

  object track extends RequestVar(Track.create)

  object imageFile extends RequestVar[Box[FileParamHolder]](Empty)

  object fileName extends RequestVar[Box[String]](Full(Helpers.nextFuncName))


  private def saveFile(fp: FileParamHolder): Unit = {

    fp.file match {
      case null =>
      case x if x.length == 0 => info("File size is 0")
      case x => {

        info("File!")


        val userId = User.currentUser.get.id.is
        val filePath = "src/main/webapp/tracks/ " + userId
        createUserTrackDirectory(filePath)


        fileName.is.map {
          name => track.is.path.set(filePath + "/" + name + fp.fileName.takeRight((4)))
        }

        track.is.user.set(userId)
        track.save

        val oFile = new File(filePath, fileName.is.openOr("BrokenLink") + fp.fileName.takeRight(4))
        val output = new FileOutputStream(oFile)
        output.write(fp.file)
        output.close()

        info("Track Uploaded!")
        S.notice("Track uploaded")

      }

    }
  }

  def createUserTrackDirectory(userPath: String) {
    val path: Path = Path(userPath)
    path.createDirectory(failIfExists = false)
  }


  def render = {

    def process() {

      (imageFile.is, track.is.name.is, track.is.description.is) match {

        case (_, "", _) => S.error("Please enter a name")
        case (Empty, _, _) => S.error("Please choose a file")
        case (image, name, _) => {

          info("The RequestVar content is: %s".format(imageFile.is))
          imageFile.is.map {
            info("Starting upload")
            file => saveFile(file)
          }
          info("Done")

        }

      }

    }

    "name=name" #> SHtml.onSubmit(track.is.name.set(_)) &
      "name=desc" #> SHtml.onSubmit(track.is.description.set(_)) &
      uploadImg &
      "type=submit" #> SHtml.onSubmitUnit(process)


  }

  def uploadImg: CssBindFunc = {

    (S.get_?, imageFile.is, track.is.name.is) match {

      case (true, _, _) => "name=track" #> SHtml.fileUpload(s => imageFile(Full(s)))
      case (_, Empty, _) => "name=track" #> SHtml.fileUpload(s => imageFile(Full(s)))
      case (_, _, "") => "name=track" #> SHtml.fileUpload(s => imageFile(Full(s)))
      case (false, _, _) => "name=track" #> fileName.is.map {
        name => SHtml.link(
          "http://127.0.0.1:8080/tracks/" + name + imageFile.is.open_!.fileName.takeRight(4),
          () => Unit,
          <span>Click to see image:
            {name + imageFile.is.open_!.fileName.takeRight(4)}
          </span>
        )
      }

    }

  }

}
