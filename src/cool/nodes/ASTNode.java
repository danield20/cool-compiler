package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

@SuppressWarnings("WeakerAccess")

public abstract class ASTNode {
    public Token token;
    public ParserRuleContext ctx;

    ASTNode(Token token, ParserRuleContext ctx) {
        this.token = token;
        this.ctx = ctx;
    }

    public Token getToken() {
        return token;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        System.out.println("Accept not defined for " + this);
        return null;
    }
}

