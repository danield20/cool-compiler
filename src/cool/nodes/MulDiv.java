package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class MulDiv extends Expression {
    public Token op;
    public Expression left;
    public Expression right;

    public MulDiv(Expression left, Token op, Expression right, Token start, ParserRuleContext ctx) {
        super(start, ctx);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
