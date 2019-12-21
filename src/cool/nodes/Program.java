package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class Program extends ASTNode {
    public ArrayList<ClassNode> classes;

    public Program(ArrayList<ClassNode> classes, Token token, ParserRuleContext ctx) {
        super(token, ctx);
        this.classes = classes;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
