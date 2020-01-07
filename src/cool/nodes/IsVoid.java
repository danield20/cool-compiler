package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class IsVoid extends Expression {
    public Id id;
    public Token op;
    public Expression void_expr;

    public IsVoid(Expression void_expr, Token op, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        id = new Id(token, ctx);
        this.op = op;
        this.void_expr = void_expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
