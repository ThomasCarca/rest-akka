package com.example

import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import spray.json.DefaultJsonProtocol._
import java.util.UUID.randomUUID

import scala.concurrent.Future
import com.example.ProductRegistryActor._
import akka.pattern.ask
import akka.util.Timeout
import spray.json.JsValue

trait ProductRoutes extends JsonSupport {

  implicit def system: ActorSystem

  def productRegistryActor: ActorRef

  implicit lazy val timeout: Timeout = Timeout(5.seconds)

  def handleMaybeProductResponse(maybeProduct: Option[Product]): StandardRoute = {
    maybeProduct match {
      case Some(product) => complete((StatusCodes.OK, product))
      case None => complete(StatusCodes.NotFound)
    }
  }

  lazy val productRoutes: Route =
    pathPrefix("products") {
      pathEnd {
        // GET /products
        get {
          val products: Future[Products] =
            (productRegistryActor ? GetProducts).mapTo[Products]
          complete(products)
        } ~
        // POST /products
        post {
          entity(as[JsValue]) { json =>
            val generatedId = randomUUID().toString
            val label = json.asJsObject.fields("label").convertTo[String]
            val price = json.asJsObject.fields("price").convertTo[Int]
            val productCreated: Future[Product] = (productRegistryActor ? CreateProduct(generatedId, label, price)).mapTo[Product]
            complete((StatusCodes.Created, productCreated))
          }
        }
      } ~
      pathPrefix(Segment) { id =>
        pathEnd {
          // GET /products/:id
          get {
            val maybeProduct: Future[Option[Product]] = (productRegistryActor ? GetProduct(id)).mapTo[Option[Product]]
            onSuccess(maybeProduct) {
              handleMaybeProductResponse
            }
          } ~
          // DELETE /products/:id
          delete {
            val maybeProductDeleted: Future[Option[Product]] =
              (productRegistryActor ? DeleteProduct(id)).mapTo[Option[Product]]
            onSuccess(maybeProductDeleted) {
              handleMaybeProductResponse
            }
          }
        } ~
        put {
          // PUT /products/:id/price
          path("price") {
            entity(as[JsValue]) { json =>
              val newPrice = json.asJsObject.fields("price").convertTo[Int]
              val maybeProductUpdated: Future[Option[Product]] = (productRegistryActor ? UpdateProductPrice(id, newPrice)).mapTo[Option[Product]]
              onSuccess(maybeProductUpdated) {
                handleMaybeProductResponse
              }
            }
          } ~
          // PUT /products/:id/label
          path("label") {
            entity(as[JsValue]) { json =>
              val newLabel = json.asJsObject.fields("label").convertTo[String]
              val maybeProductUpdated: Future[Option[Product]] = (productRegistryActor ? UpdateProductLabel(id, newLabel)).mapTo[Option[Product]]
              onSuccess(maybeProductUpdated) {
                handleMaybeProductResponse
              }
            }
          }
        }
      }
    }
}
