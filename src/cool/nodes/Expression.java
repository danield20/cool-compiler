package cool.nodes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public abstract class Expression extends ASTNode {

    public Expression(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }
}
