package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class LetDecl extends ASTNode {
    public Token id;
    public Token type;
    public Expression value;

    public LetDecl(Token id, Token type, Expression value, Token token) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
