package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class FCall extends Expression {
    public Token id;
    public ArrayList<Expression> arglst;

    public FCall(Token token, Token id, ArrayList<Expression> arglst) {
        super(token);
        this.id = id;
        this.arglst = arglst;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
