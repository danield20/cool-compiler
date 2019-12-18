package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class If extends Expression {
    public Expression cond;
    public Expression thenBranch;
    public Expression elseBranch;

    public If(Expression cond, Expression thenBranch, Expression elseBranch, Token start) {
        super(start);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
