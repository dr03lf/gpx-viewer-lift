package code.snippet

import net.liftweb._,
util.Helpers._,
http.DispatchSnippet,
mapper.{MaxRows,By,OrderBy,Descending,StartAt},
mapper.view.MapperPaginatorSnippet
import code.model.Track
import code.lib.TrackHelper


class Listings extends DispatchSnippet with TrackHelper{


  override def dispatch: DispatchIt = {

    case "all" => all
    case "paginate" => paginator.paginate _

  }

  private val paginator = new MapperPaginatorSnippet(Track){
    override def itemsPerPage = 20
  }

  def all = "li *" #> many(paginator.page)






}
