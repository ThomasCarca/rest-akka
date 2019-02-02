package com.example

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Route
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import com.example.ProductRegistryActor.CreateProduct

class ProductRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with ProductRoutes {

  override val productRegistryActor: ActorRef =
    system.actorOf(ProductRegistryActor.props, "productRegistry")

  lazy val routes: Route = productRoutes




  "GET /products" should {

    "should return a Products type as json" in {
      val request = HttpRequest(uri = "/products")

      request ~> routes ~> check {
        status              should be (StatusCodes.OK)
        contentType         should be (ContentTypes.`application/json`)
        entityAs[Products]
      }
    }
  }




  "POST /products" should {

    "should return the added Product" in {
      val productLabel = "Carrots"
      val productPrice = 5
      val entity = HttpEntity(ContentTypes.`application/json`, s"""{ "label": "$productLabel", "price": $productPrice}""")
      val request = Post("/products").withEntity(entity)

      request ~> routes ~> check {
        val createdProduct = entityAs[Product]
        status                should be (StatusCodes.Created)
        contentType           should be (ContentTypes.`application/json`)
        createdProduct.label  should be (productLabel)
        createdProduct.price  should be (productPrice)
      }
    }
  }




  "GET /products/:id" should {

    "should return a NotFound error if no product with this id has been found" in {
      val request = HttpRequest(uri = "/products/unknown-id")

      request ~> routes ~> check {
        status should be (StatusCodes.NotFound)
      }
    }

    "should return the Product if found" in {
      val newProduct = Product("id000", "Milk", 4)
      val request = HttpRequest(uri = s"/products/${newProduct.id}")

      productRegistryActor ! CreateProduct(newProduct.id, newProduct.label, newProduct.price)
      request ~> routes ~> check {
        val foundProduct = entityAs[Product]
        status        should be (StatusCodes.OK)
        contentType   should be (ContentTypes.`application/json`)
        foundProduct  should be (newProduct)
      }
    }
  }




  "PUT /products/:id/price" should {

    val newPrice = 3
    val entity = HttpEntity(ContentTypes.`application/json`, s"""{ "price": $newPrice}""")

    "should return a NotFound error if no product with this id has been found" in {
      val request = Put(uri = "/products/unknown-id/price").withEntity(entity)

      request ~> routes ~> check {
        status should be (StatusCodes.NotFound)
      }
    }

    "should return the updated product if found" in {

      val newProduct = Product("id111", "Butter", 2)
      val request = Put(uri = s"/products/${newProduct.id}/price").withEntity(entity)

      productRegistryActor ! CreateProduct(newProduct.id, newProduct.label, newProduct.price)

      request ~> routes ~> check {
        val updatedProduct = entityAs[Product]
        status                should be     (StatusCodes.OK)
        contentType           should be     (ContentTypes.`application/json`)
        updatedProduct        should not be newProduct
        updatedProduct.price  should be     (newPrice)
      }
    }
  }




  "PUT /products/:id/label" should {

    val newLabel = "Bread"
    val entity = HttpEntity(ContentTypes.`application/json`, s"""{ "label": "$newLabel"}""")

    "should return a NotFound error if no product with this id has been found" in {
      val request = HttpRequest(uri = "/products/unknown-id")

      request ~> routes ~> check {
        status should be (StatusCodes.NotFound)
      }
    }

    "should return the updated product if found" in {

      val newProduct = Product("id222", "Yogurt", 2)
      val request = Put(uri = s"/products/${newProduct.id}/label").withEntity(entity)

      productRegistryActor ! CreateProduct(newProduct.id, newProduct.label, newProduct.price)

      request ~> routes ~> check {
        val updatedProduct = entityAs[Product]
        status                should be     (StatusCodes.OK)
        contentType           should be     (ContentTypes.`application/json`)
        updatedProduct        should not be newProduct
        updatedProduct.label  should be     (newLabel)
      }
    }
  }




  "DELETE /products/:id" should {

    "should return a NotFound error if no product with this id has been found" in {
      val request = HttpRequest(uri = "/products/unknown-id")

      request ~> routes ~> check {
        status should be (StatusCodes.NotFound)
      }
    }

    "should return the deleted product if found" in {

      val newProduct = Product("id333", "Bleach", 8)
      val request = Delete(uri = s"/products/${newProduct.id}")

      productRegistryActor ! CreateProduct(newProduct.id, newProduct.label, newProduct.price)

      request ~> routes ~> check {
        val deletedProduct = entityAs[Product]
        status                should be     (StatusCodes.OK)
        contentType           should be     (ContentTypes.`application/json`)
        deletedProduct        should be     (newProduct)
      }
    }
  }

}

