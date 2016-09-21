package my.thereisnospoon.fri

import akka.actor.ActorSystem

object AppStarter extends App {

  println("Starting Akka-FRI")
  val actorSystem = ActorSystem.create()
}