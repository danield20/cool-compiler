package cool.nodes;

import cool.compiler.ASTVisitor;

@SuppressWarnings("WeakerAccess")

public abstract class ASTNode {
    public <T> T accept(ASTVisitor<T> visitor) {
        System.out.println("accept not defined for " + this);
        return null;
    }
}

