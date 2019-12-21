package cool.nodes;

import cool.compiler.ASTVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class Block extends Expression {
    public ArrayList<Expression> exprList;

    public Block(Token token, ParserRuleContext ctx, ArrayList<Expression> exprList) {
        super(token, ctx);
        this.exprList = exprList;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
