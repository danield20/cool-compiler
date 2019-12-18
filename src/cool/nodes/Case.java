package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class Case extends Expression {
    public Expression case_value;
    public ArrayList<CaseRule> branch_cases;

    public Case(Token token, Expression case_value, ArrayList<CaseRule> branch_cases) {
        super(token);
        this.case_value = case_value;
        this.branch_cases = branch_cases;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
