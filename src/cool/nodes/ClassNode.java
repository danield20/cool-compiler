package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class ClassNode extends ASTNode {
    public Id id;
    public Token inheritClass;
    public ArrayList<Feature> features;
    public Token token;

    public ClassNode(Token className, Token inheritClass, ArrayList<Feature> features, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.id = new Id(className, ctx);
        this.inheritClass = inheritClass;
        this.features = features;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
