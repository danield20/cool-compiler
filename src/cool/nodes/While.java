package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class While extends Expression {
    public Expression cond;
    public Expression block;

    public While(Token token, Expression cond, Expression block) {
        super(token);
        this.cond = cond;
        this.block = block;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
