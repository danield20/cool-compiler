package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

public class CaseRule extends ASTNode {
    public Token id;
    public Token type;
    public Expression block;

    public CaseRule(Token id, Token type, Expression block) {
        this.id = id;
        this.type = type;
        this.block = block;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
