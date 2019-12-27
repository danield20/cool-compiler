package cool.compiler;

import cool.nodes.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

public class CodeGenVisitor implements ASTVisitor<ST> {
    static STGroupFile templates = new STGroupFile("cgen.stg");
    ST mainSection;	// filled directly (through visitor returns)
    ST dataSection; // filled collaterally ("global" access)
    ST funcSection; // filled collaterally ("global" access)

    static int count = 0;

    @Override
    public ST visit(Id id) {
        return null;
    }

    @Override
    public ST visit(Int id) {
        return null;
    }

    @Override
    public ST visit(StringNode id) {
        return null;
    }

    @Override
    public ST visit(Bool id) {
        return null;
    }

    @Override
    public ST visit(If id) {
        return null;
    }

    @Override
    public ST visit(Case id) {
        return null;
    }

    @Override
    public ST visit(Assignment id) {
        return null;
    }

    @Override
    public ST visit(AddSub id) {
        return null;
    }

    @Override
    public ST visit(MulDiv id) {
        return null;
    }

    @Override
    public ST visit(Comparasion id) {
        return null;
    }

    @Override
    public ST visit(IsVoid id) {
        return null;
    }

    @Override
    public ST visit(Neg id) {
        return null;
    }

    @Override
    public ST visit(Not id) {
        return null;
    }

    @Override
    public ST visit(Program id) {
        return null;
    }

    @Override
    public ST visit(ClassNode id) {
        return null;
    }

    @Override
    public ST visit(FuncFeature id) {
        return null;
    }

    @Override
    public ST visit(VarFeature id) {
        return null;
    }

    @Override
    public ST visit(Formal id) {
        return null;
    }

    @Override
    public ST visit(CaseRule id) {
        return null;
    }

    @Override
    public ST visit(Block id) {
        return null;
    }

    @Override
    public ST visit(Instantiate id) {
        return null;
    }

    @Override
    public ST visit(While id) {
        return null;
    }

    @Override
    public ST visit(FCall id) {
        return null;
    }

    @Override
    public ST visit(ComplexFCall id) {
        return null;
    }

    @Override
    public ST visit(Let id) {
        return null;
    }

    @Override
    public ST visit(LetDecl id) {
        return null;
    }
    /*
     * Plain numbers
     * TODO 1:
     */



}