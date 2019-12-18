package cool.compiler;

import cool.nodes.*;

public interface ASTVisitor<T> {
    T visit(Id id);
    T visit(Int id);
    T visit(StringNode id);
    T visit(Bool id);
    T visit(If id);
    T visit(Case id);
    T visit(Assignment id);
    T visit(AddSub id);
    T visit(MulDiv id);
    T visit(Comparasion id);
    T visit(IsVoid id);
    T visit(Neg id);
    T visit(Not id);
    T visit(Program id);
    T visit(ClassNode id);
    T visit(FuncFeature id);
    T visit(VarFeature id);
    T visit(Formal id);
    T visit(CaseRule id);
    T visit(Block id);
    T visit(Instantiate id);
    T visit(While id);
    T visit(FCall id);
    T visit(ComplexFCall id);
    T visit(Let id);
    T visit(LetDecl id);
}
