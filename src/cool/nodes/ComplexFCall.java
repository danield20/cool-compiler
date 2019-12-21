package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class ComplexFCall extends Expression {
    public Id id;
    public Token type;
    public Expression caller;
    public ArrayList<Expression> arglst;

    public ComplexFCall(Token token, ParserRuleContext ctx, Token id, Token type, Expression caller, ArrayList<Expression> arglst) {
        super(token, ctx);
        this.id = new Id(id, ctx);
        this.type = type;
        this.caller = caller;
        this.arglst = arglst;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
