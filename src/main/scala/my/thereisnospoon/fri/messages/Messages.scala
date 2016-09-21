package my.thereisnospoon.fri.messages

import scala.concurrent.duration._
import akka.util.Timeout
import my.thereisnospoon.fri.messages.Messages.ConsignmentStatus.ConsignmentStatus

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

  case class PartiallyShip(data: ConsignmentData)

  case class EntryPartiallyShipped(sku: String)

  case class EntryFullyShipped(sku: String)
}
