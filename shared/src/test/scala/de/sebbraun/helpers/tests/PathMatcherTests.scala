/*
 * Copyright (C) 2017 Sébastien Braun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.sebbraun.helpers.tests

import de.sebbraun.helpers.pathmatching._
import org.scalatest.FlatSpec

/**
  * Created by braunse on 30.04.17.
  */
class PathMatcherTests extends FlatSpec {
  it should "match a case object" in {
    object T1 {

      sealed trait Route

      case object MyRoute extends Route

      val matcher: PathMatcher[Route] = PathMatcher[Route]("/" --> MyRoute)
    }

    assert(T1.matcher.unapply("/").contains(T1.MyRoute))
    assert(T1.matcher.unapply("/someting").isEmpty)
    assert(T1.matcher.apply(T1.MyRoute) == "/")
  }

  it should "match a case class" in {
    object T2 {

      sealed trait Route

      case class MyClass(intParam: Int, strParam: String) extends Route

      val matcher: PathMatcher[Route] = PathMatcher[Route](
        "/:intParam/:strParam" --> MyClass
      )
    }

    assert(T2.matcher.unapply("/1/hello").contains(T2.MyClass(1, "hello")))
  }

  it should "distinguish between several different routes" in {
    object T3 {

      sealed trait Route

      case object MyRoute1 extends Route

      case object MyRoute2 extends Route

      val matcher: PathMatcher[Route] = PathMatcher[Route](
        "/route-1" --> MyRoute1,
        "/route-2" --> MyRoute2
      )
    }

    assert(T3.matcher.unapply("/route-1").contains(T3.MyRoute1))
    assert(T3.matcher.unapply("/route-2").contains(T3.MyRoute2))
  }

  it should "respect the given order of routes" in {
    object T4 {

      sealed trait Route

      case object MyRoute1 extends Route

      case object MyHiddenRoute extends Route

      case class CatchAllRoute(str: String) extends Route

      val matcher: PathMatcher[Route] = PathMatcher[Route](
        "/my-route" --> MyRoute1,
        "/my-route" --> MyHiddenRoute,
        "/:str" --> CatchAllRoute
      )
    }

    assert(T4.matcher.unapply("/my-route").contains(T4.MyRoute1))
    assert(T4.matcher.unapply("/my-catchall-route").contains(T4.CatchAllRoute("my-catchall-route")))
  }
}

