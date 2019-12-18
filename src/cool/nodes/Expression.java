package cool.nodes;

import org.antlr.v4.runtime.Token;

public abstract class Expression extends ASTNode {
    public Token token;

    public Expression(Token token) {
        this.token = token;
    }
}
