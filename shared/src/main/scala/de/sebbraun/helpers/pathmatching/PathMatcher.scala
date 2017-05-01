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

package de.sebbraun.helpers.pathmatching

import scala.language.experimental.macros

/** Bidirectional mapping between a `T` and its representation as a path.
  *
  */
trait PathMatcher[T] {
  /** Parse a path and find a representation as a `T`.
    *
    * @param path The path to be matched.
    * @return `Some(x)` whenever an `x` is found that represents the data given in the path, or `None` if no match is found.
    */
  def unapply(path: String): Option[T]

  /** Represent a `T` in path form.
    *
    * @param t The `T` to convert to path form.
    * @return The path representation of `t`.
    */
  def apply(t: T): String
}

object PathMatcher {
  /** Generates an instance of [[PathMatcher]] according to the path templates given.
    *
    * All arguments to this method '''MUST''' be of the form `"path template" --> Object`, where the `Object`
    * is either
    *  - A case object extending `T`, or
    *  - The companion object of a case class extending `T`.
    *
    * The path templates given contain parameter names prefixed by `':'`. Those parameter names are matched by name
    * to the declared parameters of the case class' primary constructor.
    *
    * Acceptable syntax and conversion operations are defined by the applicable [[AsPathVar]] instance in scope for
    * the type of the constructor parameter.
    *
    * At the moment, the macro does no handle default parameters or overloaded constructors.
    *
    * @param routes A sequence of `"path template" --> Object` mappings.
    * @tparam T The common supertype of all paths matched.
    * @return A generated instance of [[PathMatcher]].
    */
  def apply[T](routes: RoutingEntry*): PathMatcher[T] = macro PathMatcherMacro.generatePathMatcher[T]
}
