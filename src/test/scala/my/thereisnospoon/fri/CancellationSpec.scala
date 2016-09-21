package my.thereisnospoon.fri

import scala.concurrent.duration._
import akka.pattern.ask
import akka.actor.{ActorIdentity, ActorSelection, Identify}
import my.thereisnospoon.fri.messages.Messages._

import scala.concurrent.Await

class CancellationSpec extends AbstractFriSpec {

  val ConsignmentCode = "cons1"

  val consignmentEntryData = ConsignmentEntryData("prod1", 2)
  val consignmentData = ConsignmentData(ConsignmentCode, ConsignmentStatus.Processing, List(consignmentEntryData))

  implicit val consignmentDataList: List[ConsignmentData] = List(consignmentData)

  "Consignment" should "have 'Cancelled' status after receive of cancellation request" in {

    val orderActorName = "CancellationSpec1"
    val orderActor = createOrderActorWithName(orderActorName)
    val consignmentSelection = ActorSelection(orderActor, ConsignmentCode)
    consignmentSelection ! Cancel(ConsignmentCode)
    consignmentSelection ! GetData

    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Cancelled)
  }

  "Packed consignment" should "not be allowed to be cancelled" in {

    val orderActorName = "CancellationSpec2"
    val orderActor = createOrderActorWithName(orderActorName)
    val consignmentSelection = ActorSelection(orderActor, ConsignmentCode)
    consignmentSelection ! Pack(ConsignmentCode)
    consignmentSelection ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Packed)

    consignmentSelection ! Cancel(ConsignmentCode)
    consignmentSelection ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Packed)
  }

  "Shipped consignment" should "not be allowed to be cancelled" in {

    val orderActorName = "CancellationSpec3"
    val orderActor = createOrderActorWithName(orderActorName)
    val consignmentSelection = ActorSelection(orderActor, ConsignmentCode)

    val consignmentActor = Await.result((consignmentSelection ? Identify(1)).mapTo[ActorIdentity], 5 seconds).ref.get

    consignmentActor ! Pack(ConsignmentCode)
    consignmentActor ! Ship(ConsignmentCode)
    consignmentActor ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Shipped)

    consignmentActor ! Cancel(ConsignmentCode)
    consignmentActor ! GetData
    expectMsgType[ConsignmentData].status should equal(ConsignmentStatus.Shipped)
  }
}
