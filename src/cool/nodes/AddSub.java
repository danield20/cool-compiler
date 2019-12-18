package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class AddSub extends Expression {
    public Expression left;
    public Expression right;
    public Token op;

    public AddSub(Expression left, Token op, Expression right, Token start) {
        super(start);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
