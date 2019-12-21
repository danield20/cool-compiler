package cool.nodes;

import cool.compiler.ASTVisitor;
import cool.structures.Scope;
import cool.structures.Symbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class Let extends Expression {
    public ArrayList<LetDecl> vars;
    public Expression body;
    public Scope scope;
    public Symbol symbol;

    public Let(Token token, ParserRuleContext ctx, ArrayList<LetDecl> vars, Expression body) {
        super(token, ctx);
        this.vars = vars;
        this.body = body;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
