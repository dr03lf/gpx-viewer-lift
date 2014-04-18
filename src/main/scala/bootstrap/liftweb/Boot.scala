package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.mapper.{DB,Schemifier,DefaultConnectionIdentifier,StandardDBVendor,MapperRules}

import code.model.{User,Track}
import net.liftmodules.widgets.flot.Flot

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable{
  def boot {
    // where to search snippet
    LiftRules.addToPackages("code")

    MapperRules.columnName = (_,name) => StringHelpers.snakify(name)
    MapperRules.tableName =  (_,name) => StringHelpers.snakify(name)

    // set the JNDI name that we'll be using
    DefaultConnectionIdentifier.jndiName = "jdbc/liftinaction"

    // handle JNDI not being avalible
    if (!DB.jndiJdbcConnAvailable_?){
      logger.warn("No JNDI configured - making a direct application connection")
      DB.defineConnectionManager(DefaultConnectionIdentifier, Database)
      // make sure cyote unloads database connections before shutting down
      LiftRules.unloadHooks.append(() => Database.closeAllConnections_!())
    }

    // automatically create the tables
    Schemifier.schemify(true, Schemifier.infoF _, User, Track)

    // setup the 404 handler
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })

    LiftRules.setSiteMap(
      SiteMap(
        List(
          Menu("Home") / "index" >> LocGroup("public"),
          Menu("Tracks") / "tracks" >> LocGroup("public"),
          Menu("Upload Track") / "uploadtrack" >> LocGroup("public"),
          Menu("Track Detail") / "track" >> LocGroup("public") >> Hidden
        ) ::: User.menus: _*
      )
    )


    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("track" :: key :: Nil,"",true,_),_,_) =>
        RewriteResponse("track" :: Nil, Map("id" -> key))
    }

    // setup the load pattern
    S.addAround(DB.buildLoanWrapper)

    // make requests utf-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))


    // h2
    if (Props.devMode || Props.testMode) {
      LiftRules.liftRequest.append({case r if (r.path.partPath match {
        case "console" :: _ => true
        case _ => false}
        ) => false})
    }

    // user exp

    LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) => Full(2 seconds, 2 seconds))

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)


    import net.liftweb.http.ResourceServer
    Flot.init
    ResourceServer.allow({
      case "flot" :: "jquery.flot.stack.js" :: Nil => true
    })

    LiftRules.useXhtmlMimeType = false

  }


  object Database extends StandardDBVendor(
    Props.get("db.class").openOr("org.h2.Driver"),
    Props.get("db.url").openOr("jdbc:h2:database/temp"),
    Props.get("db.user"),
    Props.get("db.pass"))
}
