package controllers

import play.api._
import play.api.mvc._
import views.html.index
import play.api.libs.ws.WS
import play.api.libs.json.Json
import scala.concurrent.Future
import org.springframework.scheduling.annotation.Async
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import java.util.concurrent.TimeoutException
import play.api.libs.json.JsValue

/**
 * This is the main application code for REVERSE LOOKUP directory. It uses Twilio 
 * API's for receiving calls and then uses fullcontact API's to retrieve 
 * information for that number. Please try with (800) 432-1000 as a demo to 
 * retrieve information for this number such as who is it registered to, what 
 * age and location. 
 */

/**
 * This is the Info Object, we store values in this object once retrieved from global API.
 */
case class InfoObject(name: String, age: String, location: String)

/**
 * Main Application
 */
object Application extends Controller {
  /**
   * Digits captured by Twilio
   */
  var digits: String = ""

  /**
   * Default Index Page
   */
  def index = Action {
    Ok(views.html.index("New application test page."))
  }

  /**
   * The function that gets called when somebody calls our Twilio number
   * It returns an TwiML asking for digits to enter
   */
  def numbers = Action {
    Ok(views.xml.getsymbol.render)
  }

  /**
   * The function that gets called when users have actually entered digits. 
   * It retrieves information for that number and plays to users.
   * 
   * @FIXME: This is just a short hack. There are a lot of error conditions
   * that can be checked in this function along with retry capability.
   * Also proper logging framework for debug ability can be added. 
   */
  def getNumInfo = Action {
    request =>
      {
        val requestParams = request.queryString.map { case (k, v) => k -> v.mkString }
        digits = requestParams.get("Digits").getOrElse("")
        if (digits.length < 9) {
          Ok(views.xml.getsymbol.render)
        }
      }

      Async {
        val personAPI: Future[play.api.libs.ws.Response] = WS.url("https://api.fullcontact.com/v2/person.json?phone=+1" +
          digits + "&apiKey=8dd4441dc58f8434").withTimeout(5000).get

        personAPI.map {
          response =>
            {
              val result = (response.json \ "status").as[Int]
              result match {
                case ok if result == 200 => {
                  val name = (response.json \ "contactInfo" \ "fullName").as[String]
                  val age = (response.json \ "demographics" \ "age").as[String]
                  val location = (response.json \ "demographics" \ "locationGeneral").as[String]
                  val obj = InfoObject(name, age, location)
                  Ok(views.xml.info.render(obj))
                }
                case _ => Ok(views.xml.notfound.render)
              }
            }
        }
      }
  }
}