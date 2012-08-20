package org.w3.vs.view

import org.joda.time.DateTime
import org.w3.vs.model.AssertionSeverity
import org.w3.util._

case class SortParam(name: String, ascending: Boolean) {val descending = !ascending}

trait PageOrdering[A] {

  def params: Iterable[String]
  def default: SortParam

  protected def order_(safeParam: SortParam): Ordering[A]

  final def order(param: SortParam): Ordering[A] = order_(validate(param))
  final def validate(param: SortParam): SortParam = if (params.exists(_ == param.name)) param else default
}

object PageOrdering {

  implicit val datetimeOptionOrdering: Ordering[Option[DateTime]] = new Ordering[Option[DateTime]] {
    // TODO check this (tom)
    def compare(x: Option[DateTime], y: Option[DateTime]): Int = (x, y) match {
      case (Some(date1), Some(date2)) => date1.compareTo(date2)
      case (None, Some(_)) => +1
      case (Some(_), None) => -1
      case (None, None) => 0
    }
  }

  implicit def jobViewOrdering: PageOrdering[JobView] = new PageOrdering[JobView] {
    val params: Iterable[String] = Iterable(
      "name",
      "url",
      "status",
      "completed",
      "warnings",
      "errors",
      "resources",
      "maxResources",
      "health")
    val default: SortParam = SortParam("name", ascending = true)
    def order_(safeParam: SortParam): Ordering[JobView] = {
      val ord = safeParam.name match {
        case "name"         => Ordering[String].on[JobView](_.name)
        case "url"          => Ordering[(String, String)].on[JobView](job => (job.url.toString, job.name))
        case "status"       => Ordering[(String, String)].on[JobView](job => (job.status, job.name))
        case "completed"    => Ordering[(Option[DateTime], String)].on[JobView](job => (job.lastCompleted, job.name))
        case "warnings"     => Ordering[(Int, String)].on[JobView](job => (job.warnings, job.name))
        case "errors"       => Ordering[(Int, String)].on[JobView](job => (job.errors, job.name))
        case "resources"    => Ordering[(Int, String)].on[JobView](job => (job.resources, job.name))
        case "maxResources" => Ordering[(Int, String)].on[JobView](job => (job.maxResources, job.name))
        case "health"       => Ordering[(Int, String)].on[JobView](job => (job.health, job.name))
      }
      if (safeParam.ascending) ord else ord.reverse
    }
  }

  implicit def resourceViewOrdering: PageOrdering[ResourceView] = new PageOrdering[ResourceView] {
    val params: Iterable[String] = Iterable(
      "url",
      "validated",
      "warnings",
      "errors")
    val default: SortParam = SortParam("errors", ascending = false)
    def order_(safeParam: SortParam): Ordering[ResourceView] = {
      val ord = safeParam.name match {
        case "url"       => Ordering[String].on[ResourceView](_.url.toString)
        case "validated" => Ordering[(DateTime, String)].on[ResourceView](view => (view.lastValidated, view.url.toString))
        case "warnings"  => Ordering[(Int, String)].on[ResourceView](view => (view.warnings, view.url.toString))
        case "errors"    => Ordering[(Int, String)].on[ResourceView](view => (view.errors, view.url.toString))
      }
      if (safeParam.ascending) ord else ord.reverse
    }
  }

  implicit def assertionViewOrdering: PageOrdering[AssertionView] = new PageOrdering[AssertionView] {
    val params: Iterable[String] = Iterable("")
    val default: SortParam = SortParam("", ascending = true)
    def order_(safeParam: SortParam): Ordering[AssertionView] = {
      val ord = safeParam.name match {
        case _ => {
          val a = Ordering[AssertionSeverity].reverse
          val b = Ordering[Int].reverse
          val c = Ordering[String]
          Ordering.Tuple3(a, b, c).on[AssertionView](assertion => (assertion.severity, assertion.occurrences, assertion.message.text))
        }
      }
      if (safeParam.ascending) ord else ord.reverse
    }
  }

}