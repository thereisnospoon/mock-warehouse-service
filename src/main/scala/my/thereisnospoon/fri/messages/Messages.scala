package my.thereisnospoon.fri.messages

import akka.actor.ActorRef

import scala.concurrent.duration._
import akka.util.Timeout
import my.thereisnospoon.fri.messages.Messages.ConsignmentStatus.ConsignmentStatus

import scala.runtime.Nothing$

object Messages {

  implicit val timeout: Timeout = 10 seconds

  case class ConsignmentEntryData(sku: String, qty: Int, shippedQty: Int = 0)

  object ConsignmentStatus extends Enumeration {
    type ConsignmentStatus = Value
    val Processing, Cancelled, Packed, PartiallyShipped, Shipped = Value
  }

  case class ConsignmentData(code: String, status: ConsignmentStatus, consignmentEntries: List[ConsignmentEntryData])

  case class OrderData(orderCode: String, consignments: List[ConsignmentData])

  case object GetData

  case class Cancel(consignmentCode: String)

  case class Pack(consignmentCode: String)

  case class Ship(consignmentCode: String)

  case object ShipEntry

  case class PartiallyShip(consignmentCode: String, entriesToShip: List[PartiallyShipEntry])

  class ShippedStatusResponse

  case class EntryPartiallyShipped(sku: String) extends ShippedStatusResponse

  case class EntryFullyShipped(sku: String) extends ShippedStatusResponse

  case class PartiallyShipEntry(sku: String, qty: Int)

  case class EntriesShippedStatusReplies(replies: List[ShippedStatusResponse], originalSender: ActorRef)
}
