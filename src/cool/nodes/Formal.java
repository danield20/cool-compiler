package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Formal extends ASTNode {
    public Id id;
    public Token type;

    public Formal(Token id, Token type, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = new Id(id, ctx);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
