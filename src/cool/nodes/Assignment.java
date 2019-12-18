package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class Assignment extends Expression {
    public Token varName;
    public Expression value;

    public Assignment(Token var, Expression val, Token start) {
        super(start);
        varName = var;
        value = val;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
