package com.smeup.rpgparser.ast

import com.smeup.rpgparser.AbstractDataDefinition
import com.smeup.rpgparser.InStatementDataDefinition
import com.smeup.rpgparser.ast.DataWrapUpChoice
import com.smeup.rpgparser.ast.Subroutine
import com.strumenta.kolasu.model.Derived
import com.strumenta.kolasu.model.Node
import com.strumenta.kolasu.model.Position
import com.strumenta.kolasu.model.ReferenceByName

interface StatementThatCanDefineData {
    fun dataDefinition() : InStatementDataDefinition?
}

abstract class Statement(override val position: Position? = null) : Node(position)
data class ExecuteSubroutine(var subroutine: ReferenceByName<Subroutine>, override val position: Position? = null) : Statement(position)
data class SelectStmt(var cases: List<SelectCase>,
                      var other: SelectOtherClause? = null,
                      override val position: Position? = null) : Statement(position)
data class SelectOtherClause(val body: List<Statement>, override val position: Position? = null) : Node(position)
data class SelectCase(val condition: Expression, val body: List<Statement>, override val position: Position? = null) : Node(position)
data class EvalStmt(var expression: Expression, override val position: Position? = null) : Statement(position)
data class CallStmt(val expression: Expression, override val position: Position? = null) : Statement(position)
data class IfStmt(val condition: Expression, val body: List<Statement>,
                  val elseIfClauses: List<ElseIfClause> = emptyList(),
                  val elseClause: ElseClause? = null,
                  override val position: Position? = null) : Statement(position)

data class ElseClause(val body: List<Statement>, override val position: Position? = null) : Node(position)
data class ElseIfClause(val condition: Expression, val body: List<Statement>, override val position: Position? = null) : Node(position)
data class SetOnStmt(val choice: DataWrapUpChoice, override val position: Position? = null) : Statement(position)
data class PlistStmt(val params: List<PlistParam>, override val position: Position? = null) : Statement(position)
data class PlistParam(val paramName: String, override val position: Position? = null) : Node(position)
data class ClearStmt(val value: Expression,
                     @Derived val dataDefinition: InStatementDataDefinition? = null,
                     override val position: Position? = null) : Statement(position), StatementThatCanDefineData {
    override fun dataDefinition() = dataDefinition
}
data class DisplayStmt(val value: Expression, override val position: Position? = null) : Statement(position)
data class DoStmt(val body: List<Statement>, override val position: Position? = null) : Statement(position)
data class LeaveStmt(override val position: Position? = null) : Statement(position)
data class IterStmt(override val position: Position? = null) : Statement(position)
data class OtherStmt(override val position: Position? = null) : Statement(position)
data class ForStmt(
        var init: Expression,
        val endValue: Expression,
        val body: List<Statement>, override val position: Position? = null) : Statement(position) {
    fun iterDataDefinition(): AbstractDataDefinition {
        if (init is AssignmentExpr) {
            if ((init as AssignmentExpr).target is DataRefExpr) {
                return ((init as AssignmentExpr).target as DataRefExpr).variable.referred!!
            } else {
                throw UnsupportedOperationException()
            }
        } else {
            throw UnsupportedOperationException()
        }
    }
}
