package cool.compiler;

import cool.structures.*;
import cool.nodes.*;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = null;

    @Override
    public Void visit(Program id) {
        System.out.println("Here we go");
        return null;
    }

    @Override
    public Void visit(Id id) {
        return null;
    }

    @Override
    public Void visit(Int id) {
        return null;
    }

    @Override
    public Void visit(StringNode id) {
        return null;
    }

    @Override
    public Void visit(Bool id) {
        return null;
    }

    @Override
    public Void visit(If id) {
        return null;
    }

    @Override
    public Void visit(Case id) {
        return null;
    }

    @Override
    public Void visit(Assignment id) {
        return null;
    }

    @Override
    public Void visit(AddSub id) {
        return null;
    }

    @Override
    public Void visit(MulDiv id) {
        return null;
    }

    @Override
    public Void visit(Comparasion id) {
        return null;
    }

    @Override
    public Void visit(IsVoid id) {
        return null;
    }

    @Override
    public Void visit(Neg id) {
        return null;
    }

    @Override
    public Void visit(Not id) {
        return null;
    }

    @Override
    public Void visit(ClassNode id) {
        return null;
    }

    @Override
    public Void visit(FuncFeature id) {
        return null;
    }

    @Override
    public Void visit(VarFeature id) {
        return null;
    }

    @Override
    public Void visit(Formal id) {
        return null;
    }

    @Override
    public Void visit(CaseRule id) {
        return null;
    }

    @Override
    public Void visit(Block id) {
        return null;
    }

    @Override
    public Void visit(Instantiate id) {
        return null;
    }

    @Override
    public Void visit(While id) {
        return null;
    }

    @Override
    public Void visit(FCall id) {
        return null;
    }

    @Override
    public Void visit(ComplexFCall id) {
        return null;
    }

    @Override
    public Void visit(Let id) {
        return null;
    }

    @Override
    public Void visit(LetDecl id) {
        return null;
    }
}