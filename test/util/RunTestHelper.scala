package org.w3.vs.util

import org.w3.vs.model._
import akka.testkit.{TestKit, ImplicitSender}
import org.w3.vs.VSConfiguration
import org.scalatest._
import org.scalatest.matchers.MustMatchers
import org.w3.util.website._

/**
 * helper trait that can be used to test Runs
 * Be careful: all TypedActors are stopped after each test
 */
abstract class RunTestHelper(configuration: VSConfiguration = new DefaultTestConfiguration { })
extends TestKit(configuration.system) with ImplicitSender
with WordSpec with MustMatchers with BeforeAndAfterAll {
  
  def servers: Seq[Webserver]
  
  implicit val implicitConfiguration: VSConfiguration = configuration

  val http = system.actorFor(system / "http")

  val orgTestId = OrganizationId()
  val userTestId = UserId()
  val organizationTest = Organization(orgTestId, "test orga", userTestId)
  val userTest = User(userId = userTestId, organization = Some(orgTestId), email = "", name = "", password = "")
  
  val logger = play.Logger.of(classOf[RunTestHelper])
  
  val configurationBeforeTest = org.w3.vs.Prod.configuration
  
  override def beforeAll: Unit = {
    org.w3.vs.Prod.configuration = configuration
    servers foreach { _.start() }
  }
  
  override def afterAll: Unit = {
    configuration.system.shutdown()
    configuration.system.awaitTermination()
    servers foreach { _.stop() }
    org.w3.vs.Prod.configuration = configurationBeforeTest
  }
  
//  /**
//   * test the invariant that must be true for all observations
//   */
//  def testInvariants(run: Run): Example = {
//    logger.debug("TODO: for every authority, delay was respected")
//    logger.debug("TODO: for every authority, no fetch overlap")
//    1 must beEqualTo(1)
//  }
  
}
