package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class ClassNode extends ASTNode {
    public Token className;
    public Token inheritClass;
    public ArrayList<Feature> features;
    public Token token;

    public ClassNode(Token className, Token inheritClass, ArrayList<Feature> features, Token token) {
        this.className = className;
        this.inheritClass = inheritClass;
        this.features = features;
        this.token = token;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
