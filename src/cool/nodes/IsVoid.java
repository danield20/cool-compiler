package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class IsVoid extends Expression {
    public Token op;
    public Expression void_expr;

    public IsVoid(Expression void_expr, Token op, Token token) {
        super(token);
        this.op = op;
        this.void_expr = void_expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
