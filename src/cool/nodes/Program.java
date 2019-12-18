package cool.nodes;

import cool.compiler.ASTVisitor;

import java.util.ArrayList;

public class Program extends ASTNode {
    public ArrayList<ClassNode> classes;

    public Program(ArrayList<ClassNode> classes) {
        this.classes = classes;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
