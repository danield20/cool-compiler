package cool.nodes;

import org.antlr.v4.runtime.Token;

public class Feature extends ASTNode {
    public Token token;

    public Feature(Token token) {
        this.token = token;
    }

}
