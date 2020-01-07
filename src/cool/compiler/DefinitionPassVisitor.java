package cool.compiler;

import cool.structures.*;
import cool.nodes.*;

public class DefinitionPassVisitor implements ASTVisitor<ClassSymbol> {
    Scope currentScope = null;

    @Override
    public ClassSymbol visit(Program id) {
        currentScope = SymbolTable.globals;
        boolean main = false;

        for (ClassNode my_class : id.classes) {
            if (my_class.id.getToken().getText().compareTo("Main") == 0) {
                for (Feature m : my_class.features) {
                    if (m instanceof FuncFeature && ((FuncFeature) m).id.getToken().getText().compareTo("main") == 0) {
                        main = true;
                    }
                }
            }
            my_class.accept(this);
        }

        if (!main) {
            SymbolTable.error(id.classes.get(0).ctx, id.classes.get(0).id.getToken(), "No method main in class Main");
            return null;
        }

        return null;
    }

    @Override
    public ClassSymbol visit(ClassNode cls) {

        if (cls.id.token.getText().compareTo("SELF_TYPE") == 0) {
            SymbolTable.error(cls.ctx, cls.id.token, "Class has illegal name SELF_TYPE");
            return null;
        }

        var symbol = currentScope.lookup(cls.id.textInScope);

        if (symbol != null) {
            SymbolTable.error(cls.ctx, cls.id.token, "Class " + cls.id.token.getText() + " is redefined");
            return null;
        }

        //create new class symbol
        var newClassSymbol = new ClassSymbol(currentScope, cls.id.token.getText());
        currentScope.add(newClassSymbol);
        currentScope = newClassSymbol;

        //check if the parrent class exists, if it does, set the parent class
        //if not, dummy with name until the second pass
        if (cls.inheritClass != null) {
            String inheritedClass = cls.inheritClass.getText();

            if (currentScope.lookup(inheritedClass) != null) {
                newClassSymbol.setParentClass((ClassSymbol) currentScope.lookup(inheritedClass));
            } else {
                newClassSymbol.setParentClass(new ClassSymbol(null, inheritedClass));
            }
        }

        cls.id.setSymbol(newClassSymbol);
        cls.id.setScope(currentScope);

        var selfSymbol = new IdSymbol("self");
        selfSymbol.setType(ClassSymbol.SELF_TYPE);
        currentScope.add(selfSymbol);

        for (Feature f : cls.features) {
            f.accept(this);
        }

        currentScope = newClassSymbol.getParent();

        return null;
    }

    @Override
    public ClassSymbol visit(VarFeature var) {
        var id = var.id;
        var type = var.type;
        ClassSymbol currentSc = (ClassSymbol)currentScope;

        if (id.getToken().getText().compareTo("self") == 0) {
            SymbolTable.error(var.ctx, id.getToken(), "Class " + currentSc.getName() + " has attribute with illegal name self");
            return null;
        }

        if (currentSc.lookup(id.textInScope) != null) {
            if (currentSc.lookup(id.getToken().getText()) instanceof IdSymbol) {
                SymbolTable.error(var.ctx, id.getToken(), "Class " + currentSc.getName() + " redefines attribute " + id.getToken().getText());
                return null;
            } else {
                id.textInScope = id.getToken().getText() + "_M";
            }
        }

        var newSymbol = new IdSymbol(id.textInScope);
        ClassSymbol typeSymbol;

        if (currentSc.lookup(type.getText()) == null) {
            typeSymbol = new ClassSymbol(null, type.getText());
        } else {
            typeSymbol = (ClassSymbol) currentSc.lookup(type.getText());
        }

        newSymbol.setType(typeSymbol);
        currentScope.add(newSymbol);
        id.setScope(currentScope);
        id.setSymbol(newSymbol);

        if (var.value != null)
            var.value.accept(this);

        return null;
    }

    @Override
    public ClassSymbol visit(FuncFeature func) {
        var id = func.id;
        var type = func.returnType;
        ClassSymbol currentSc = (ClassSymbol) currentScope;

        if (currentScope.lookup(id.textInScope) != null) {
            if (currentSc.lookup(id.textInScope) instanceof FunctionSymbol) {
                SymbolTable.error(func.ctx, id.getToken(), "Class " +
                        currentSc.getName() + " redefines method " + id.getToken().getText());
                return null;
            } else {
                id.textInScope = id.getToken().getText() + "_F";
            }
        }

        var functionSymbol = new FunctionSymbol(currentScope, id.token.getText());
        currentScope.add(functionSymbol);
        currentScope = functionSymbol;
        id.setSymbol(functionSymbol);
        id.setScope(functionSymbol);

        var functionTypeSymbol = currentScope.lookup(type.getText());

        if (functionTypeSymbol == null) {
            functionSymbol.setType(new ClassSymbol(null, type.getText()));
        } else {
            functionSymbol.setType((ClassSymbol) functionTypeSymbol);
        }

        for (var formal: func.paramList) {
            formal.accept(this);
        }

        func.body.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public ClassSymbol visit(Formal form) {
        var id = form.id;
        var type = form.type;
        var parentClass = (ClassSymbol) currentScope.getParent();
        var currentScope = (FunctionSymbol) this.currentScope;

        if (id.getToken().getText().compareTo("self") == 0) {
            SymbolTable.error(form.ctx, id.getToken(), "Method " +
                    currentScope.getName() + " of class " +  parentClass.getName() +
                    " has formal parameter with illegal name self");
            return null;
        }

        if (currentScope.lookupCurrent(id.textInScope) != null) {
            SymbolTable.error(form.ctx, id.getToken(), "Method " +
                    currentScope.getName() + " of class " +  parentClass.getName() +
                    " redefines formal parameter " + id.getToken().getText());
            return null;
        }

        if (type.getText().compareTo("SELF_TYPE") == 0) {
            SymbolTable.error(form.ctx, type, "Method " +
                    currentScope.getName() + " of class " +  parentClass.getName() +
                    " has formal parameter " + id.getToken().getText() +
                    " with illegal type SELF_TYPE");
            return null;
        }

        var formalSymbol = new IdSymbol(id.getToken().getText());
        var typeSymbol = currentScope.lookup(type.getText());

        if (typeSymbol == null) {
            formalSymbol.setType(new ClassSymbol(null, type.getText()));
        } else {
            formalSymbol.setType((ClassSymbol) typeSymbol);
        }

        id.setScope(currentScope);
        id.setSymbol(formalSymbol);
        currentScope.add(formalSymbol);

        return null;
    }

    @Override
    public ClassSymbol visit(Let lt) {
        var newScope = new LetScope(currentScope);
        currentScope = newScope;
        lt.setScope(currentScope);

        for (LetDecl ldc : lt.vars) {
            ldc.accept(this);
        }

        lt.body.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public ClassSymbol visit(LetDecl ltdcl) {
        var id = ltdcl.id;
        var type = ltdcl.type;
        var value = ltdcl.value;

        if (id.getToken().getText().compareTo("self") == 0) {
            SymbolTable.error(ltdcl.ctx, ltdcl.id.getToken(), "Let variable has illegal name self");
            return null;
        }

        var newSymbol = new IdSymbol(ltdcl.id.getToken().getText());
        var typeSymbol = currentScope.lookup(type.getText());

        if (typeSymbol == null) {
            newSymbol.setType(new ClassSymbol(null, type.getText()));
        } else {
            newSymbol.setType((ClassSymbol) typeSymbol);
        }

        id.setScope(currentScope);
        id.setSymbol(newSymbol);
        if (value != null)
            value.accept(this);
        currentScope.add(newSymbol);


        return null;
    }

    @Override
    public ClassSymbol visit(Case cas) {
        var newScope = new DefaultScope(currentScope);
        currentScope = newScope;

        cas.case_value.accept(this);

        for (CaseRule ldc : cas.branch_cases) {
            ldc.accept(this);
        }

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public ClassSymbol visit(CaseRule cr) {
        var id = cr.id;
        var type = cr.type;

        if (id.getToken().getText().compareTo("self") == 0) {
            SymbolTable.error(cr.ctx, id.getToken(), "Case variable has illegal name self");
            return null;
        }

        if (type.getText().compareTo("SELF_TYPE") == 0) {
            SymbolTable.error(cr.ctx, type, "Case variable " + id.getToken().getText() +
                    " has illegal type SELF_TYPE");
            return null;
        }

        var newSymbol = new IdSymbol(id.getToken().getText());
        var typeSymbol = currentScope.lookup(type.getText());

        if (typeSymbol == null) {
            newSymbol.setType(new ClassSymbol(null, type.getText()));
        } else {
            newSymbol.setType((ClassSymbol) typeSymbol);
        }

        id.setScope(currentScope);
        id.setSymbol(newSymbol);
        currentScope.add(newSymbol);

        cr.block.accept(this);

        return null;
    }

    @Override
    public ClassSymbol visit(Id id) {

        if (currentScope instanceof LetScope && ((LetScope) currentScope).f_call == false) {
            if (currentScope.lookup(id.textInScope) == null) {

                //look into inherited
                Scope getClass = currentScope.getParent();

                while (!(getClass instanceof ClassSymbol) && getClass != null) {
                    getClass = getClass.getParent();
                }

                if (getClass != null) {
                    ClassSymbol parentClass = (ClassSymbol) getClass;
                    while (parentClass != ClassSymbol.NULL_CLASS) {
                        if (parentClass.lookup(id.textInScope) != null) {
                            id.setScope(parentClass);
                            id.setSymbol(parentClass.lookup(id.textInScope));
                            return null;
                        }
                        parentClass = parentClass.getParentClass();
                    }
                }

                SymbolTable.error(id.ctx, id.getToken(), "Undefined identifier " + id.textInScope);
                return null;
            }
        }

        if (currentScope.lookup(id.textInScope) == null) {
            //look into inherited
            Scope getClass = currentScope.getParent();

            while (!(getClass instanceof ClassSymbol) && getClass != null) {
                getClass = getClass.getParent();
            }

            if (getClass != null) {
                ClassSymbol parentClass = (ClassSymbol) getClass;
                while (parentClass != ClassSymbol.NULL_CLASS) {
                    if (parentClass.lookup(id.textInScope) != null) {
                        id.setScope(currentScope);
                        id.setSymbol(parentClass.lookup(id.textInScope));
                        return null;
                    }
                    parentClass = parentClass.getParentClass();
                }
            }
        }

        id.setScope(currentScope);
        id.setSymbol(currentScope.lookup(id.textInScope));

        return null;
    }

    @Override
    public ClassSymbol visit(AddSub id) {
        id.left.accept(this);
        id.right.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(MulDiv id) {
        id.left.accept(this);
        id.right.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(Comparasion id) {
        id.left.accept(this);
        id.right.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(Neg id) {
        id.value.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(Assignment assign) {
        assign.id.accept(this);
        assign.value.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(Not id) {
        id.value.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(IsVoid id) {
        id.id.setScope(currentScope);
        id.void_expr.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(Instantiate id) {
        id.id.setScope(currentScope);
        return null;
    }

    @Override
    public ClassSymbol visit(While id) {
        id.cond.accept(this);
        id.block.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(If id) {
        id.cond.accept(this);
        id.thenBranch.accept(this);
        id.elseBranch.accept(this);
        return null;
    }

    @Override
    public ClassSymbol visit(Block id) {
        for (Expression e : id.exprList) {
            e.accept(this);
        }
        return null;
    }

    @Override
    public ClassSymbol visit(FCall id) {
        if (currentScope instanceof LetScope) {
            ((LetScope) currentScope).f_call = true;
            id.id.accept(this);
            ((LetScope) currentScope).f_call = false;
        } else {
            id.id.accept(this);
        }
        for (Expression f : id.arglst) {
            f.accept(this);
        }
        return null;
    }

    @Override
    public ClassSymbol visit(ComplexFCall id) {
        id.caller.accept(this);
        if (currentScope instanceof LetScope) {
            ((LetScope) currentScope).f_call = true;
            id.id.accept(this);
            ((LetScope) currentScope).f_call = false;
        } else {
            id.id.accept(this);
        }
        for (Expression f : id.arglst) {
            f.accept(this);
        }
        return null;
    }

    @Override
    public ClassSymbol visit(Int id) {
        return null;
    }

    @Override
    public ClassSymbol visit(Bool id) {
        return null;
    }

    @Override
    public ClassSymbol visit(StringNode id) {
        return null;
    }
}