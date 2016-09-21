package my.thereisnospoon.fri.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import my.thereisnospoon.fri.messages.Messages._

import scala.concurrent.Future

class OrderActor(private val orderCode: String, private val consignmentsData: List[ConsignmentData]) extends Actor {

  implicit val executionContext = context.system.dispatchers.lookup("akka.actor.default-dispatcher")

  private val consignments: List[ActorRef] = consignmentsData.map(data => context.actorOf(ConsignmentActor.props(data), data.code))

  consignments.foreach(context.watch)

  override def receive: Receive = {

    case GetData =>
      val currentSender = sender()
      for (consignmentsDataList <- Future.sequence(consignments.map(_ ? GetData).map(_.mapTo[ConsignmentData]))) {
        currentSender ! OrderData(orderCode, consignmentsDataList)
      }

    case Cancel(consignmentCode) =>
      context.actorSelection(consignmentCode) ! Cancel(consignmentCode)
  }
}

object OrderActor {

  def props(orderData: OrderData) = {
    Props(classOf[OrderActor], orderData.orderCode, orderData.consignments)
  }
}
