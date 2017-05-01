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

/** Implementations of [[AsPathVar]] for primitive types.
  *
  * This trait contains implicit [[AsPathVar]] definitions for [[scala.Byte]], [[scala.Short]], [[scala.Int]],
  * [[scala.Long]] and [[scala.Predef.String]]
  *
  */
trait PrimitivePathVars {
  private def numericPathVar[T: ParseableIntegral] = new AsPathVar[T] {
    override def regexFragment: String = "-?\\d+"

    override def unapply(string: String): Option[T] = implicitly[ParseableIntegral[T]].parse(string)

    override def apply(t: T): String = t.toString
  }

  implicit val bytePathVar: AsPathVar[Byte] = numericPathVar[Byte]

  implicit val shortPathVar: AsPathVar[Short] = numericPathVar[Short]

  implicit val intPathVar: AsPathVar[Int] = numericPathVar[Int]

  implicit val longPathVar: AsPathVar[Long] = numericPathVar[Long]

  implicit object stringPathVar extends AsPathVar[String] {
    override def regexFragment: String = "[^/]+"

    override def unapply(string: String): Option[String] = Some(string)

    override def apply(string: String): String = string
  }

}
