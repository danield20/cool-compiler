package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class Let extends Expression {
    public ArrayList<LetDecl> vars;
    public Expression body;

    public Let(Token token, ArrayList<LetDecl> vars, Expression body) {
        super(token);
        this.vars = vars;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
