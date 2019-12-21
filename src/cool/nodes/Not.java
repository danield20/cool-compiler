package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Not extends Expression {
    public Token op;
    public Expression value;

    public Not(Token op, Expression value, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.op = op;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
