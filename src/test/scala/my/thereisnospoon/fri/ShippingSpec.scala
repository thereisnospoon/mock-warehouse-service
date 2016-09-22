package my.thereisnospoon.fri

import akka.actor.ActorSelection
import my.thereisnospoon.fri.messages.Messages._

class ShippingSpec extends AbstractFriSpec {

  val ConsignmentCode = "consCode"
  val Sku1 = "sku1"
  val Sku2 = "sku2"
  val Qty1 = 3
  val Qty2 = 2

  val entryData1 = ConsignmentEntryData(Sku1, Qty1)
  val entryData2 = ConsignmentEntryData(Sku2, Qty2)
  val consignmentData = ConsignmentData(ConsignmentCode, ConsignmentStatus.Processing, List(entryData1, entryData2))

  implicit val consignmentDataList = List(consignmentData)

  "Packed consignment" should "be shipped after 'Ship' request receive" in {

    val orderActor = createOrderActorWithName("ShippingSpec1")
    val consignmentActorSelection = ActorSelection(orderActor, ConsignmentCode)
    val consignmentActor = getActorRefBySelection(consignmentActorSelection)
    consignmentActor ! Pack(ConsignmentCode)
    consignmentActor ! Ship(ConsignmentCode)
    consignmentActor ! GetData

    val receivedConsignmentData = expectMsgType[ConsignmentData]
    receivedConsignmentData.status should equal(ConsignmentStatus.Shipped)
    receivedConsignmentData.consignmentEntries.foreach(entry => entry.qty should equal(entry.shippedQty))
  }

  "Packed consignment" should "be fully shipped after all partial shipments" in {

    val orderActor = createOrderActorWithName("ShippingSpec2")
    val consignmentActorSelection = ActorSelection(orderActor, ConsignmentCode)
    val consignmentActor = getActorRefBySelection(consignmentActorSelection)
    consignmentActor ! Pack(ConsignmentCode)
    consignmentActor ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Packed)

    val partialShipment1 = PartiallyShip(ConsignmentCode,
      List(PartiallyShipEntry(Sku1, Qty1), PartiallyShipEntry(Sku2, Qty2 - 1)))

    consignmentActor ! partialShipment1
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.PartiallyShipped)
    consignmentActor ! PartiallyShip(ConsignmentCode, List(PartiallyShipEntry(Sku2, 1)))
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Shipped)
  }
}
