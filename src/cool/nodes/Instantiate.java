package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Instantiate extends Expression {
    public Id id;
    public Token type;

    public Instantiate(Token token, ParserRuleContext ctx, Token type) {
        super(token, ctx);
        this.type = type;
        id = new Id(token, ctx);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
