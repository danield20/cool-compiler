package cool.compiler;

import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")

public abstract class ASTNode {
    public <T> T accept(ASTVisitor<T> visitor) {
        System.out.println("accept not defined for " + this);
        return null;
    }
}

abstract class Expression extends ASTNode {
    Token token;

    Expression(Token token) {
        this.token = token;
    }
}

class Id extends Expression {
    Id(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Int extends Expression {
    Int(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Bool extends Expression {
    public Bool(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class StringNode extends Expression {
    public StringNode(Token token) {
        super(token);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class If extends Expression {
    Expression cond;
    Expression thenBranch;
    Expression elseBranch;

    If(Expression cond, Expression thenBranch, Expression elseBranch, Token start) {
        super(start);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Case extends Expression {
    Expression case_value;
    ArrayList<CaseRule> branch_cases;

    public Case(Token token, Expression case_value, ArrayList<CaseRule> branch_cases) {
        super(token);
        this.case_value = case_value;
        this.branch_cases = branch_cases;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class CaseRule extends ASTNode {
    Token id;
    Token type;
    Expression block;

    public CaseRule(Token id, Token type, Expression block) {
        this.id = id;
        this.type = type;
        this.block = block;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assignment extends Expression {
    Token varName;
    Expression value;

    Assignment(Token var, Expression val, Token start) {
        super(start);
        varName = var;
        value = val;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class AddSub extends Expression {
    Expression left;
    Expression right;
    Token op;

    AddSub(Expression left, Token op, Expression right, Token start) {
        super(start);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class MulDiv extends Expression {
    Token op;
    Expression left;
    Expression right;

    MulDiv(Expression left, Token op, Expression right, Token start) {
        super(start);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Comparasion extends Expression {
    Expression left;
    Expression right;
    Token op;

    Comparasion(Expression left, Token op, Expression right, Token start) {
        super(start);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class IsVoid extends Expression {
    Token op;
    Expression void_expr;

    public IsVoid(Expression void_expr, Token op, Token token) {
        super(token);
        this.op = op;
        this.void_expr = void_expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Neg extends Expression {
    Token op;
    Expression value;

    public Neg(Token op, Expression value, Token token) {
        super(token);
        this.op = op;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Not extends Expression {
    Token op;
    Expression value;

    public Not(Token op, Expression value, Token token) {
        super(token);
        this.op = op;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Program extends ASTNode {
    ArrayList<ClassNode> classes;

    public Program(ArrayList<ClassNode> classes) {
        this.classes = classes;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ClassNode extends ASTNode {
    Token className;
    Token inheritClass;
    ArrayList<Feature> features;
    Token token;

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

class Feature extends ASTNode {
    Token token;

    public Feature(Token token) {
        this.token = token;
    }

}

class FuncFeature extends Feature {
    Token id;
    ArrayList<Formal> paramList;
    Token returnType;
    Expression body;

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

class VarFeature extends Feature {
    Token id;
    Token type;
    Expression value;

    public VarFeature(Token id, Token type, Expression value, Token token) {
        super(token);
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Formal extends ASTNode {
    Token id;
    Token type;

    public Formal(Token id, Token type) {
        this.id = id;
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    ArrayList<Expression> exprList;

    public Block(Token token, ArrayList<Expression> exprList) {
        super(token);
        this.exprList = exprList;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Instantiate extends Expression {
    Token type;

    public Instantiate(Token token, Token type) {
        super(token);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class While extends Expression {
    Expression cond;
    Expression block;

    public While(Token token, Expression cond, Expression block) {
        super(token);
        this.cond = cond;
        this.block = block;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class FCall extends Expression {
    Token id;
    ArrayList<Expression> arglst;

    public FCall(Token token, Token id, ArrayList<Expression> arglst) {
        super(token);
        this.id = id;
        this.arglst = arglst;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ComplexFCall extends Expression {
    Token id;
    Token type;
    Expression caller;
    ArrayList<Expression> arglst;

    public ComplexFCall(Token token, Token id, Token type, Expression caller, ArrayList<Expression> arglst) {
        super(token);
        this.id = id;
        this.type = type;
        this.caller = caller;
        this.arglst = arglst;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    ArrayList<LetDecl> vars;
    Expression body;

    public Let(Token token, ArrayList<LetDecl> vars, Expression body) {
        super(token);
        this.vars = vars;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class LetDecl extends ASTNode {
    Token id;
    Token type;
    Expression value;

    public LetDecl(Token id, Token type, Expression value, Token token) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}