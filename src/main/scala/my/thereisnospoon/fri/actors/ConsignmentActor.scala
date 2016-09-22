package my.thereisnospoon.fri.actors

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import akka.pattern.ask
import my.thereisnospoon.fri.messages.Messages._

import scala.concurrent.Future
import scala.concurrent.Future._

class ConsignmentActor(private val code: String,
                       private val consignmentEntriesData: List[ConsignmentEntryData]) extends Actor {

  import context._
  import ConsignmentStatus._

  implicit val executionContext = system.dispatchers.lookup("akka.actor.default-dispatcher")

  private var status: ConsignmentStatus = Processing
  private val consignmentEntries: List[ActorRef] = consignmentEntriesData.map(data =>
    actorOf(ConsignmentEntryActor.props(data), data.sku))

  private var fullyShippedEntries: Set[String] = Set()

  override def receive: Receive = respondWithData.orElse(processingStateHandler)

  private def respondWithData: Receive = {

    case GetData =>
      val currentSender = sender()
      val currentStatus = status
      for (entryDataList <- sequence(consignmentEntries.map(_ ? GetData).map(_.mapTo[ConsignmentEntryData]))) {
        currentSender ! ConsignmentData(code, currentStatus, entryDataList)
      }
  }

  private def processingStateHandler: Receive = {

    case Cancel(consignmentCode) if consignmentCode == code =>
      status = Cancelled
      become(respondWithData)

    case Pack(consignmentCode) if consignmentCode == code =>
      status = Packed
      become(respondWithData.orElse(packedStateHandler))
  }

  private def packedStateHandler: Receive = {

    case Ship(consignmentCode) if consignmentCode == code =>
      consignmentEntries.foreach(_ ! ShipEntry)
      status = Shipped
      become(respondWithData)

    case PartiallyShip(consignmentCode, entriesToShip) if consignmentCode == code =>

      val currentSender = sender()
      val statusResponses: Future[List[ShippedStatusResponse]] = sequence(
        entriesToShip.map(entry => ActorSelection(self, entry.sku) ? PartiallyShipEntry(entry.sku, entry.qty)))
        .mapTo[List[ShippedStatusResponse]]

      for (sr <- statusResponses) {
        self ! EntriesShippedStatusReplies(sr, currentSender)
      }

    case EntriesShippedStatusReplies(statusResponses, originalSender) =>
      fullyShippedEntries ++= statusResponses.collect({
        case EntryFullyShipped(sku) => sku
      })
      if (fullyShippedEntries.size == consignmentEntries.length) {
        status = Shipped
        become(respondWithData)
      } else {
        status = PartiallyShipped
      }
      originalSender ! ConsignmentData(code, status, Nil)
  }
}

object ConsignmentActor {
  def props(consignmentData: ConsignmentData) = Props(classOf[ConsignmentActor], consignmentData.code, consignmentData.consignmentEntries)
}