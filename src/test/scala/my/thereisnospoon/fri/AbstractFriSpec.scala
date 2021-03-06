package my.thereisnospoon.fri

import akka.actor.{ActorIdentity, ActorRef, ActorSelection, ActorSystem, Identify}
import akka.testkit.{ImplicitSender, TestKit}
import my.thereisnospoon.fri.actors.OrderActor
import my.thereisnospoon.fri.messages.Messages.{ConsignmentData, OrderData}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, Matchers}

abstract class AbstractFriSpec extends TestKit(ActorSystem("TestSystem")) with ImplicitSender
  with FlatSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfter {

  override def afterAll() = TestKit.shutdownActorSystem(system)

  def createOrderActorWithName(orderActorName: String)(implicit consignmentDataList: List[ConsignmentData]) =
    system.actorOf(OrderActor.props(OrderData(orderActorName, consignmentDataList)), orderActorName)

  def getActorRefBySelection(selection: ActorSelection): ActorRef = {

    selection ! Identify(1)
    expectMsgType[ActorIdentity].ref match {
      case Some(actorRef) => actorRef
      case _ => getActorRefBySelection(selection)
    }
  }
}
