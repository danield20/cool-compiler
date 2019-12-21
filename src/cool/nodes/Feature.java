package cool.nodes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class Feature extends ASTNode {
    public Feature(Token token, ParserRuleContext ctx) {
        super(token, ctx);
    }
}
