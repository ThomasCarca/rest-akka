package com.example

import spray.json.RootJsonFormat

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val productJsonFormat: RootJsonFormat[Product] = jsonFormat3(Product)
  implicit val productsJsonFormat: RootJsonFormat[Products] = jsonFormat1(Products)
}
//#json-support
