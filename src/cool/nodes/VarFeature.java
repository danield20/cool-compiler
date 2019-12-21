package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class VarFeature extends Feature {
    public Id id;
    public Token type;
    public Expression value;

    public VarFeature(Token id, Token type, Expression value, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = new Id(id, ctx);
        this.type = type;
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
