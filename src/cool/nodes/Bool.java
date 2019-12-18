package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class Bool extends Expression {
    public Bool(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
