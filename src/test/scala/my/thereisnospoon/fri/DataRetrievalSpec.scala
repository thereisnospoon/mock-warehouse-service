package my.thereisnospoon.fri

import akka.actor.ActorRef
import my.thereisnospoon.fri.messages.Messages._

class DataRetrievalSpec extends AbstractFriSpec {


  val consignmentEntryData = (1 to 10).map(i => ConsignmentEntryData(s"prod$i", i)).toList
  implicit val consignmentData = (11 to 20).map(i => ConsignmentData(s"$i", ConsignmentStatus.Processing, consignmentEntryData)).toList
  val orderActorName = "DataRetrievalSpec1"

  var orderActor: ActorRef = createOrderActorWithName(orderActorName)

  "Order actor" should "return order data upon request" in {

    orderActor ! GetData
    val receivedOrderData = expectMsgType[OrderData]
    receivedOrderData.orderCode should equal (orderActorName)
    receivedOrderData.consignments.map(_.code) should equal ((11 to 20).map(_.toString).toList)
  }
}
