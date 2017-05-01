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

package de.sebbraun.helpers

import scala.annotation.compileTimeOnly

/**
  * Created by braunse on 01.05.17.
  */
package object pathmatching {

  /** Provide the `-->` operator.
    * This conversion exists purely in order to guide matcher generation.
    *
    * @see [[PathMatcher.apply]]
    */
  @compileTimeOnly("Be sure to use PathMatcher[T]() to construct your matcher")
  implicit class StringToRoutingEntry(private val str: String) extends AnyVal {

    /** Provide the `Template --> Companion Object` syntax used by [[PathMatcher.apply]].
      * This method exists purely in order to guide matcher generation.
      *
      * @see [[PathMatcher.apply]]
      */
    //noinspection NotImplementedCode
    def -->(companion: Any): RoutingEntry = ???
  }

}
