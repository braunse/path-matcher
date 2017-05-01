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

/** Type class for things that can be converted to and from path fragments.
  *
  */
trait AsPathVar[T] {
  /** Return a regular expression fragment that can be used to match a `T` in a path expression.
    *
    * The returned fragment must not declare any groups. Use non-grouping parens `(?:...)`.
    *
    * @return A regular expression fragment to be used to match `T` in a path fragment.
    */
  def regexFragment: String

  /** Parse the path fragment given into a `T`, if possible.
    *
    * Returning `None` will cause the next route entry to be considered.
    *
    * @param string The path fragment to be parsed
    * @return `Some(t)` if the given path fragment is the string representation of `t`, `None` otherwise.
    */
  def unapply(string: String): Option[T]

  /** Unparse the `T` into a path fragment.
    *
    * This must be the reverse operation for [[unapply]].
    *
    * @param t The `T` to be represented as a path fragment.
    * @return The string representation of `t`.
    */
  def apply(t: T): String
}

/** Contains the default set of predefined [[AsPathVar]] implicits.
  *
  */
object AsPathVar extends PrimitivePathVars