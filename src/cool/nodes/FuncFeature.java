package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class FuncFeature extends Feature {
    public Token id;
    public ArrayList<Formal> paramList;
    public Token returnType;
    public Expression body;

    public FuncFeature(Token id,
                       ArrayList<Formal> paramList,
                       Token returnType,
                       Expression body,
                       Token token) {
        super(token);
        this.id = id;
        this.paramList = paramList;
        this.returnType = returnType;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
