package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class Formal extends ASTNode {
    public Token id;
    public Token type;

    public Formal(Token id, Token type) {
        this.id = id;
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
