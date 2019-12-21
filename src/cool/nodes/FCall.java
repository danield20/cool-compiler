package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class FCall extends Expression {
    public Id id;
    public ArrayList<Expression> arglst;

    public FCall(Token token, ParserRuleContext ctx, Token id, ArrayList<Expression> arglst) {
        super(token, ctx);
        this.id = new Id(id, ctx);
        this.arglst = arglst;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
