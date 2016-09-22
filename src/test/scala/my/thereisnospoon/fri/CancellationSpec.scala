package my.thereisnospoon.fri

import akka.actor.ActorSelection
import my.thereisnospoon.fri.messages.Messages._

class CancellationSpec extends AbstractFriSpec {

  val ConsignmentCode = "cons1"

  val consignmentEntryData = ConsignmentEntryData("prod1", 2)
  val consignmentData = ConsignmentData(ConsignmentCode, ConsignmentStatus.Processing, List(consignmentEntryData))

  implicit val consignmentDataList: List[ConsignmentData] = List(consignmentData)

  "Consignment" should "have 'Cancelled' status after receive of cancellation request" in {

    val orderActorName = "CancellationSpec1"
    val orderActor = createOrderActorWithName(orderActorName)
    val consignmentSelection = ActorSelection(orderActor, ConsignmentCode)
    val consignmentActor = getActorRefBySelection(consignmentSelection)
    consignmentActor ! Cancel(ConsignmentCode)
    consignmentActor ! GetData

    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Cancelled)
  }

  "Packed consignment" should "not be allowed to be cancelled" in {

    val orderActorName = "CancellationSpec2"
    val orderActor = createOrderActorWithName(orderActorName)
    val consignmentSelection = ActorSelection(orderActor, ConsignmentCode)
    val consignmentActor = getActorRefBySelection(consignmentSelection)

    consignmentActor ! Pack(ConsignmentCode)
    consignmentActor ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Packed)

    consignmentActor ! Cancel(ConsignmentCode)
    consignmentActor ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Packed)
  }

  "Shipped consignment" should "not be allowed to be cancelled" in {

    val orderActorName = "CancellationSpec3"
    val orderActor = createOrderActorWithName(orderActorName)
    val consignmentSelection = ActorSelection(orderActor, ConsignmentCode)

    val consignmentActor = getActorRefBySelection(consignmentSelection)

    consignmentActor ! Pack(ConsignmentCode)
    consignmentActor ! Ship(ConsignmentCode)
    consignmentActor ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Shipped)

    consignmentActor ! Cancel(ConsignmentCode)
    consignmentActor ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Shipped)
  }
}
