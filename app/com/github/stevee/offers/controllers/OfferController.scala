package com.github.stevee.offers.controllers

import java.time.{LocalDateTime, ZoneId, format}
import java.util.{UUID, Currency => JCurrency}

import com.github.stevee.offers.api.{CreateOfferRequest, CreateOfferResponse, OfferResponse}
import com.github.stevee.offers.service.{FoundExpired, FoundValid, OfferNotFound}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class OfferController @Inject()(val controllerComponents: OffersControllerComponents)(implicit ec: ExecutionContext)
  extends BaseController
    with I18nSupport {

  import controllerComponents.offerService

  private val logger = Logger(getClass)

  def create: Action[AnyContent] = Action.async { implicit request =>
    logger.info("create: ")
    processJsonPost()
  }

  def cancel(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"get: $id")
    idHandler(id) { uuid =>
      offerService.cancel(uuid).map { cancelled =>
        if (cancelled) Ok else NotFound
      }
    }
  }

  def get(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"get: $id")
    idHandler(id) { uuid =>
      offerService.findValid(uuid).map {
        case FoundValid(offer) => Ok(Json.toJson(OfferResponse.fromOffer(offer)))
        case FoundExpired => Gone
        case OfferNotFound => NotFound
      }
    }
  }

  def idHandler(id: String)(handle: UUID => Future[Result]): Future[Result] =
    Try(UUID.fromString(id)).toOption.fold[Future[Result]](Future.successful(NotFound))(handle)

  private val form: Form[CreateOfferRequest] = {
    import play.api.data.Forms._
    val currencies = JCurrency.getAvailableCurrencies.asScala.map(_.getCurrencyCode)
    val formatter = format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val zoneId = ZoneId.of("UTC")
    Form(
      mapping(
        "price" -> bigDecimal,
        "currency" -> nonEmptyText(3, 3).verifying(s => currencies.contains(s)),
        "description" -> nonEmptyText(0, 1000),
        "start" -> nonEmptyText.
          verifying(s => Try(formatter.parse(s)).isSuccess)
          .transform[LocalDateTime](LocalDateTime.parse, _.toString),
        "duration" -> nonEmptyText.verifying(s => Try(Duration(s)).isSuccess)
          .transform[Duration](Duration.apply, _.toString)
      )(CreateOfferRequest.apply)(CreateOfferRequest.unapply)
    )
  }

  private def processJsonPost[A]()(
    implicit request: Request[A]): Future[Result] = {
    def failure(badForm: Form[CreateOfferRequest]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: CreateOfferRequest) = {
      offerService.create(input).map { offer =>
        Created(Json.toJson(CreateOfferResponse(offer.id.toString)))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
