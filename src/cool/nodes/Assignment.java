package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Assignment extends Expression {
    public Id id;
    public Expression value;

    public Assignment(Token var, Expression val, Token start, ParserRuleContext ctx) {
        super(start, ctx);
        id = new Id(var, ctx);
        value = val;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
