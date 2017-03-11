package support

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.exceptions.{TestFailedException, TestPendingException}
import org.scalatest.matchers.Matcher
import org.scalatest.time.{Millis, Seconds, Span}

import scala.language.experimental.macros

trait HandsOnSuite extends FunSpec with Matchers with ScalaFutures {
  implicit val suite: HandsOnSuite = this
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  def __ : Matcher[Any] = throw new TestPendingException

  def exercice(testName: String)(testFun: Unit)(implicit suite: HandsOnSuite): Unit = macro ExerciceMacro.apply

  protected override def runTest(testName: String, args: Args) = {
    if (!CustomStopper.oneTestFailed) {
      super.runTest(testName, args.copy(reporter = new CustomReporter(args.reporter), stopper = CustomStopper))
    } else {
      SucceededStatus
    }
  }
}

object HandsOnSuite {
  def runTest(testName: String, suite: HandsOnSuite)(testFun: => Unit)(ctx: TestContext) {
    suite.it(testName) {
      try {
        testFun
      } catch {
        case e: TestPendingException => throw MyException.pending(suite, ctx, e)
        case e: NotImplementedError => throw MyException.notImpl(suite, ctx, e)
        case e: TestFailedException => throw MyException.failed(suite, ctx, e)
        case e: Throwable => throw MyException.unknown(suite, ctx, e)
      }
    }
  }
}