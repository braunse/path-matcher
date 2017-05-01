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

import scala.collection.mutable
import scala.reflect.macros.blackbox
import scala.util.matching.Regex

/**
  *
  */
private class PathMatcherMacro(val c: blackbox.Context) {

  import c.universe._

  private val IllegalArgumentExceptionTpe = typeOf[IllegalArgumentException].typeSymbol
  private val OptionTpe = typeOf[Option[_]].typeSymbol
  private val PathMatcherTpe = typeOf[PathMatcher[_]].typeSymbol
  private val AsPathVarTpe = typeOf[AsPathVar[_]].typeSymbol

  def generatePathMatcher[T: WeakTypeTag](routes: c.Tree*): c.Tree = {
    val TTpe = weakTypeOf[T].typeSymbol

    val cases = routes map analyzeCase

    val pathVariableTypes = collectVarTypes(cases).toSeq

    val asPathVarNames = pathVariableTypes map { _ => c.freshName("asPathVar"): TermName }

    val asPathVarNameByType = Map(pathVariableTypes zip asPathVarNames: _*)

    val asPathVarDecls = (pathVariableTypes zip asPathVarNames) map makeAsPathVarDecl

    q"""
      new $PathMatcherTpe[$TTpe]() {
        ..${cases.flatMap(declareMatcher)}
        ..$asPathVarDecls
        def unapply(path: String): $OptionTpe[$TTpe] = {
          path match {
            case ..${cases.map(makeUnapplyCase(asPathVarNameByType))}
            case _ => None
          }
        }
        def apply(t: $TTpe): String = t match {
          case ..${cases.map(makeApplyCase)}
          case _ => throw new $IllegalArgumentExceptionTpe("Un-unparseable route: " + t.toString())
        }
      }
    """
  }

  private def analyzeConstructorParams(pos: Position, cls: ClassSymbol): Seq[(String, Type)] = {
    val paramList = cls.primaryConstructor.asMethod.paramLists match {
      case List(params1) if params1.forall(!_.isImplicit) =>
        params1
      case List(params1, params2) if params1.forall(_.isImplicit) && params2.forall(_.isImplicit) =>
        params1
      case _ =>
        c.abort(pos, s"Class $cls must have exactly one non-implicit argument list")
    }

    paramList map { param => (param.name.encodedName.toString, param.typeSignature)}
  }

  private def parsePath(position: c.universe.Position, params: Seq[(String, Type)], str: String): ParameterizedPath = {
    val paramNames = params.map(_._1)
    // val allVariables = Set(namesInOrder: _*)
    val paramMap = mutable.Map(params: _*)
    var i = 0
    var parts = Seq[(String, PathVariable)]()

    for(mtch <- PathMatcherMacro.PathVar.findAllMatchIn(str)) {
      val prefix = str.substring(i, mtch.start)
      val varName = mtch.group(1)
      i = mtch.end
      if(!(paramMap contains varName)) {
        if(paramNames contains varName) {
          c.abort(position, s"Path template mentions variable $varName, but it has already been used prior to this mention.")
        } else {
          c.abort(position, s"Path template mentions variable $varName, but that is not a known constructor parameter (${paramNames.mkString(", ")}")
        }
      }

      val pathVar = PathVariable(varName, paramMap(varName).typeSymbol.asInstanceOf[TypeSymbol])
      parts :+= (prefix, pathVar)
      paramMap.remove(varName)
    }

    val suffix = str.substring(i)
    ParameterizedPath(parts, suffix)
  }

  private def analyzeCase(route: c.Tree): RoutingCase = route match {
    case q"$_.StringToRoutingEntry(${Literal(Constant(str: String))}).-->($caseObject)"
      if caseObject.tpe.typeSymbol.isModuleClass && caseObject.tpe.typeSymbol.asClass.isCaseClass =>

      RoutingCase.Constant(
        caseObject.pos.asInstanceOf[Position],
        str,
        caseObject.symbol.asModule.asInstanceOf[ModuleSymbol])

    case q"$_.StringToRoutingEntry(${Literal(Constant(str: String))}).-->($companionObject)"
      if companionObject.tpe.typeSymbol.isModuleClass && companionObject.symbol.asModule.companionSymbol.asClass.isCaseClass =>

      val position = companionObject.pos.asInstanceOf[Position]

      val params = analyzeConstructorParams(position, companionObject.symbol.asModule.companionSymbol.asClass.asInstanceOf[ClassSymbol])

      val matcherName = c.freshName("matcher")

      RoutingCase.Parameterized(
        position,
        parsePath(position, params, str),
        companionObject.symbol.asModule.asInstanceOf[ModuleSymbol],
        params.map(_._1),
        matcherName
      )

    case x =>
      c.abort(x.pos, s"Unable to parse ${show(x)}, not of a recognized form")
  }

  private def declareMatcher(routingCase: RoutingCase): Option[ValDef] = routingCase match {
    case RoutingCase.Constant(_, _, _) => None
    case RoutingCase.Parameterized(_, ParameterizedPath(prefixAndVars, suffix), _, _, name) =>
      val variablesMatcher = prefixAndVars.foldLeft(Literal(Constant("^")): Tree) { (sofar, prefixAndVar) =>
        val groupStart = s"("
        val groupEnd = ")"
        q"$sofar + ${RegexQuoter.quote(prefixAndVar._1)} + $groupStart + implicitly[$AsPathVarTpe[${prefixAndVar._2.tpe}]].regexFragment + $groupEnd"
      }
      Some(q"""
        val $name = ($variablesMatcher + $suffix + "$$").r
      """)
  }

  private def makeUnapplyCase(asPathVarNameByType: Map[TypeSymbol, TermName])(routingCase: RoutingCase): CaseDef = routingCase match {
    case RoutingCase.Constant(_, path, caseObject) =>

      cq"$path => Some($caseObject)"

    case RoutingCase.Parameterized(_, ParameterizedPath(prefixAndVar, _), companion, applicationOrder, matcherName) =>

      val matchVars = prefixAndVar.map(pav => {
        val varPat = pq"${TermName(pav._2.name)} @ _"
        pq"${asPathVarNameByType(pav._2.tpe)}($varPat)"
      })
      val applVars = applicationOrder.map(nm => nm: TermName)
      val pat = pq"$matcherName(..$matchVars)"
      cq"$pat => Some($companion.apply(..$applVars))"
  }

  private def makeApplyCase(routingCase: RoutingCase): CaseDef = routingCase match {
    case RoutingCase.Constant(_, path, caseObject) =>

      val co = q"$caseObject"
      val pat = pq"$co"
      cq"$pat => $path"

    case RoutingCase.Parameterized(_, ParameterizedPath(prefixAndVar, suffix), companion, applicationOrder, _) =>

      val matchVars = applicationOrder.map(name => pq"${name: TermName}")
      val pat = pq"$companion(..$matchVars)"
      val sbName = c.freshName("stringBuilder"): TermName
      val appends = prefixAndVar.map(pav => {
        val asPathInst = q"implicitly[$AsPathVarTpe[${pav._2.tpe}]]"
        q"$sbName.append(${pav._1}).append($asPathInst.apply(${pav._2.name: TermName}))"
      })
      val wholeExpr = q"""
        {
          val $sbName = new _root_.java.lang.StringBuilder()
          ..$appends
          $sbName.append($suffix)
          $sbName.toString()
        }
      """
      cq"$pat => $wholeExpr"
  }

  private def collectVarTypes(cases: Seq[RoutingCase]): Set[TypeSymbol] = {
    def collect1(caze: RoutingCase): Seq[TypeSymbol] = caze match {
      case RoutingCase.Constant(_, _, _) => Seq()
      case RoutingCase.Parameterized(_, path, _, _, _) => path.prefixAndVar.map(_._2.tpe)
    }

    Set(cases flatMap collect1: _*)
  }

  private def makeAsPathVarDecl(typeAndName: (TypeSymbol, TermName)): c.Tree = {
    q"val ${typeAndName._2} = implicitly[$AsPathVarTpe[${typeAndName._1}]]"
  }

  private sealed trait RoutingCase

  private object RoutingCase {

    case class Constant(definedAt: Position, literalPath: String, caseObject: ModuleSymbol) extends RoutingCase

    case class Parameterized(definedAt: Position, path: ParameterizedPath, companion: ModuleSymbol,
                             applicationOrder: Seq[String], matcherName: TermName) extends RoutingCase

  }

  private case class ParameterizedPath(prefixAndVar: Seq[(String, PathVariable)], suffix: String)

  private case class PathVariable(name: String, tpe: TypeSymbol)

}

private object PathMatcherMacro {
  val PathVar: Regex = ":([a-zA-Z$_][a-zA-Z0-9$_]*)".r
}