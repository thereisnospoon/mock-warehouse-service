package my.thereisnospoon.fri.actors

import akka.actor.{Actor, Props}
import my.thereisnospoon.fri.messages.Messages._

class ConsignmentEntryActor(private val sku: String, private val qty: Int, private var shippedQty: Int = 0)
  extends Actor {

  override def receive: Receive = {

    case GetData => sender() ! ConsignmentEntryData(sku, qty, shippedQty)

    case ShipEntry => shippedQty = qty

    case PartiallyShipEntry(entrySku, qtyToShip) if entrySku == sku =>
      shippedQty = Math.min(qty, shippedQty + qtyToShip)
      if (shippedQty == qty)
        sender() ! EntryFullyShipped(sku)
      else
        sender() ! EntryPartiallyShipped(sku)
  }
}

object ConsignmentEntryActor {

  def props(consignmentEntryData: ConsignmentEntryData) = Props(
    new ConsignmentEntryActor(consignmentEntryData.sku, consignmentEntryData.qty))
}