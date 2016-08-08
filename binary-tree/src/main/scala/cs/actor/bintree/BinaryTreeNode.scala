package cs.actor.bintree

import akka.actor.{ Actor, ActorRef, Props }

/**
 * Created by cristian on 17/02/16.
 */
object BinaryTreeNode {
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode], elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = {
    case copyTo @ CopyTo(treeNode) ⇒
      subtrees.values foreach { actorRef ⇒
        actorRef ! copyTo
      }
      if (!removed) treeNode ! Insert(self, 1, elem)

      val expected = subtrees.values.toSet
      val insertConfirmed = removed
      checkFinish(expected, insertConfirmed)
      context.become(copying(expected, insertConfirmed), discardOld = false)
    case operation @ Insert(requester, id, elemSearched) ⇒ elemSearched match {
      case x if x == elem ⇒
        removed = false
        requester ! OperationFinished(id)
      case x if x < elem ⇒
        if (subtrees.contains(Left)) subtrees(Left) ! operation
        else {
          val newChildRef = context.actorOf(props(elemSearched, initiallyRemoved = false))
          subtrees = subtrees + (Left -> newChildRef)
          requester ! OperationFinished(id)
        }
      case x ⇒
        if (subtrees.contains(Right)) subtrees(Right) ! operation
        else {
          val newChildRef = context.actorOf(props(elemSearched, initiallyRemoved = false))
          subtrees = subtrees + (Right -> newChildRef)
          requester ! OperationFinished(id)
        }
    }
    case operation @ Remove(requester, id, elemSearched) ⇒ elemSearched match {
      case x if x == elem ⇒
        removed = true
        requester ! OperationFinished(id)
      case x if x < elem ⇒
        if (subtrees.contains(Left)) subtrees(Left) ! operation
        else {
          requester ! OperationFinished(id)
        }
      case x ⇒
        if (subtrees.contains(Right)) subtrees(Right) ! operation
        else {
          requester ! OperationFinished(id)
        }
    }

    case operation @ Contains(requester, id, elemSearched) ⇒ elemSearched match {
      case x if x == elem ⇒
        requester ! ContainsResult(id, result = !removed)
      case x if x < elem ⇒
        if (subtrees.contains(Left)) subtrees(Left) ! operation
        else {
          requester ! ContainsResult(id, result = false)
        }
      case x ⇒
        if (subtrees.contains(Right)) subtrees(Right) ! operation
        else {
          requester ! ContainsResult(id, result = false)
        }
    }
  }

  // optional
  /**
   * `expected` is the set of ActorRefs whose replies we are waiting for,
   * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
   */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = {
    case CopyFinished ⇒
      val newExpected = expected - sender
      checkFinish(newExpected, insertConfirmed)
      context.become(copying(newExpected, insertConfirmed), discardOld = true)
    case operationFinish: OperationFinished ⇒
      val newInsertConfirmed = true
      checkFinish(expected, newInsertConfirmed)
      context.become(copying(expected, newInsertConfirmed), discardOld = true)
  }

  def checkFinish(expected: Set[ActorRef], insertConfirmed: Boolean): Unit = {
    if (insertConfirmed && expected.isEmpty) {
      context.parent ! CopyFinished
    }
  }

}
