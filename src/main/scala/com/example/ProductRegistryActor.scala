package com.example

//#product-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }

//#product-case-classes
final case class Product(id: String, label: String, price: Int)
final case class Products(products: Seq[Product])
//#product-case-classes

object ProductRegistryActor {
  final case object GetProducts
  final case class CreateProduct(id: String, label: String, price: Int)
  final case class GetProduct(id: String)
  final case class DeleteProduct(id: String)
  final case class UpdateProductPrice(id: String, newPrice: Int)
  final case class UpdateProductLabel(id: String, newLabel: String)

  def props: Props = Props[ProductRegistryActor]
}

class ProductRegistryActor extends Actor with ActorLogging {
  import ProductRegistryActor._

  var products: Seq[Product] = Nil

  def receive: Receive = {
    case GetProducts =>
      sender() ! Products(products)
    case CreateProduct(id, label, price) =>
      val newProduct = Product(id, label, price)
      products = products :+ newProduct
      sender() ! newProduct
    case GetProduct(id) =>
      sender() ! products.find(_.id == id)
    case DeleteProduct(id) =>
      val product = products.find(p => p.id == id)
      products = products.filter(_.id != id)
      sender() ! product
    case UpdateProductLabel(id, newLabel) =>
      products = products.map(p => if(p.id == id) p.copy(label = newLabel) else p)
      sender() ! products.find(p => p.id == id)
    case UpdateProductPrice(id, newPrice) =>
      products = products.map(p => if(p.id == id) p.copy(price = newPrice) else p)
      sender() ! products.find(p => p.id == id)
  }
}
//#product-registry-actor