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
    consignmentActorSelection ! Pack(ConsignmentCode)
    consignmentActorSelection ! Ship(ConsignmentCode)
    consignmentActorSelection ! GetData

    val receivedConsignmentData = expectMsgType[ConsignmentData]
    receivedConsignmentData.status should equal(ConsignmentStatus.Shipped)
    receivedConsignmentData.consignmentEntries.foreach(entry => entry.qty should equal(entry.shippedQty))
  }
}
