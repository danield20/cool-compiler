package cool.compiler;

import cool.structures.*;
import cool.nodes.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ResolutionPassVisitor implements ASTVisitor<ClassSymbol> {

    @Override
    public ClassSymbol visit(Program id) {
        for (ClassNode my_class : id.classes) {
            my_class.accept(this);
        }

        return null;
    }

    @Override
    public ClassSymbol visit(ClassNode cls) {

        if (cls.id.getSymbol() == null) {
            return null;
        }


        ClassSymbol clsSymbol = (ClassSymbol)cls.id.getSymbol();
        Scope currentScope = cls.id.getScope();

        if (clsSymbol.getParentClass().getName().compareTo("Object") != 0) {
            String parentName = clsSymbol.getParentClass().getName();

            if (ClassSymbol.invalidParents.contains(parentName)) {
                SymbolTable.error(cls.ctx, cls.inheritClass, "Class " + cls.id.token.getText() + " has illegal parent " + parentName);
                return null;
            }

            if (currentScope.lookup(parentName) == null) {
                SymbolTable.error(cls.ctx, cls.inheritClass, "Class " + cls.id.token.getText() + " has undefined parent " + parentName);
                return null;
            }

            clsSymbol.setParentClass((ClassSymbol)currentScope.lookup(parentName));

            //check for inheritance cycle
            ClassSymbol currentParent = clsSymbol.getParentClass();
            Scope secCurrScope = currentScope;

            while(currentParent.getName().compareTo("Object") != 0) {
                if (currentParent.getName().compareTo(cls.id.getToken().getText()) == 0) {
                    SymbolTable.error(cls.ctx, cls.id.getToken(), "Inheritance cycle for class " + cls.id.getToken().getText());
                    return null;
                }
                String nextClass = currentParent.getParentClass().getName();
                currentParent.setParentClass((ClassSymbol)secCurrScope.lookup(nextClass));
                currentParent = currentParent.getParentClass();
            }
        }

        for (Feature f : cls.features) {
            f.accept(this);
        }

        return null;
    }

    @Override
    public ClassSymbol visit(VarFeature var) {
        var id = var.id;
        var type = var.type;

        if (id.getSymbol() == null) {
            return null;
        }

        var currentSc = (ClassSymbol) var.id.getScope();
        var parentSc = ((ClassSymbol)var.id.getScope()).getParentClass();

        if (parentSc.lookup(id.textInScope) != null) {
            SymbolTable.error(var.ctx, id.getToken(), "Class " + currentSc.getName() + " redefines inherited attribute " + id.getToken().getText());
            return null;
        }

        if (currentSc.lookup(var.type.getText()) == null) {
            SymbolTable.error(var.ctx, type, "Class " +
                    currentSc.getName() + " has attribute " + id.getToken().getText() +
                    " with undefined type " + type.getText());
            return null;
        } else {
            IdSymbol symbol = (IdSymbol) var.id.getSymbol();
            if (type.getText().compareTo("SELF_TYPE") != 0)
                symbol.setType((ClassSymbol)currentSc.lookup(var.type.getText()));
        }

        if (var.value != null) {
            var varType = var.value.accept(this);
            if (varType != null) {
                var typeType = ((IdSymbol) id.getSymbol()).getType();
                if (typeType == null) {
                    return null;
                }
                if (!typeType.isParentOrEqual(varType)) {
                    SymbolTable.error(var.ctx, var.value.getToken(), "Type " + varType.getName() +
                            " of initialization expression of attribute " + id.getToken().getText() +
                            " is incompatible with declared type " + typeType.getName());
                    return null;
                }
            }
        }

        return null;
    }

    @Override
    public ClassSymbol visit(FuncFeature func) {
        var id = func.id;
        var type = func.returnType;

        if (id.getSymbol() == null) {
            return null;
        }

        var currentScope = (FunctionSymbol) id.getScope();
        var parentScope = (ClassSymbol) currentScope.getParent();
        var currentFuncSymbol = (FunctionSymbol) id.getSymbol();

        if (currentScope.lookup(type.getText()) == null) {
            SymbolTable.error(func.ctx, id.getToken(), "Class " +
                    parentScope.getName() + " has method " +  id.getToken().getText() +
                    " with undefined return type " + type.getText());
            return null;
        } else {
            if (type.getText().compareTo("SELF_TYPE") != 0)
                currentFuncSymbol.setType((ClassSymbol)currentScope.lookup(type.getText()));
        }

        ClassSymbol inheritedScope = null;

        if (parentScope.getParentClass().lookup(id.textInScope) != null) {

            inheritedScope = parentScope.getParentClass();
            var inheritedFunc = (FunctionSymbol)inheritedScope.lookup(id.textInScope);

            if (inheritedFunc.getType().getName().compareTo(currentFuncSymbol.getType().getName()) != 0) {
                SymbolTable.error(func.ctx, func.returnType, "Class " +
                        parentScope.getName() + " overrides method " +  id.getToken().getText() +
                        " but changes return type from " + inheritedFunc.getType().getName() +
                        " to " + currentFuncSymbol.getType().getName());
                return null;
            }

            List<Symbol> current = new ArrayList<>(currentScope.symbols.values());
            List<Symbol> inherited = new ArrayList<>(inheritedFunc.symbols.values());

            if (current.size() != inherited.size()) {
                SymbolTable.error(func.ctx, id.getToken(), "Class " +
                        parentScope.getName() + " overrides method " +  id.getToken().getText() +
                        " with different number of formal parameters");
                return null;
            }

            for(int i = 0; i < current.size(); i++) {
                IdSymbol s1 = (IdSymbol) current.get(i);
                IdSymbol s2 = (IdSymbol) inherited.get(i);

                if (s1.getType().getName().compareTo(s2.getType().getName()) != 0) {
                    SymbolTable.error(func.ctx, func.paramList.get(i).type, "Class " +
                            parentScope.getName() + " overrides method " +  id.getToken().getText() +
                            " but changes type of formal parameter " + s1.getName() + " from " +
                            s2.getType().getName() + " to " + s1.getType().getName());
                    return null;
                }
            }
        }

        for (var formal: func.paramList) {
            formal.accept(this);
        }

        var funcBodyType = func.body.accept(this);
        var realType = ((IdSymbol) id.getSymbol()).getType();

        if (funcBodyType != null && realType != null) {

            if (funcBodyType.getName().compareTo("SELF_TYPE") == 0) {
                funcBodyType = parentScope;
            }

            if (realType.getName().compareTo("SELF_TYPE") == 0) {
                realType = parentScope;
            }

            if (!realType.isParentOrEqual(funcBodyType)) {
                SymbolTable.error(func.ctx, func.body.getToken(), "Type " + funcBodyType.getName() +
                        " of the body of method " + id.getToken().getText() +
                        " is incompatible with declared return type " + type.getText());
                return null;
            }
        }

        return null;
    }

    @Override
    public ClassSymbol visit(Formal form) {
        var id = form.id;
        var type = form.type;

        if (id.getSymbol() == null) {
            return null;
        }

        var currentScope = (FunctionSymbol) id.getScope();
        var parentClass = (ClassSymbol) currentScope.getParent();

        if (currentScope.lookup(type.getText()) == null) {
            SymbolTable.error(form.ctx, type, "Method " +
                    currentScope.getName() + " of class " +  parentClass.getName() +
                    " has formal parameter " + id.getToken().getText() +
                    " with undefined type " + type.getText());
            return null;
        } else {
            var symbol = (IdSymbol) id.getSymbol();
            symbol.setType((ClassSymbol) currentScope.lookup(type.getText()));
        }

        return null;
    }

    @Override
    public ClassSymbol visit(Let lt) {

        for (LetDecl ldc : lt.vars) {
            ldc.accept(this);
        }

        return lt.body.accept(this);
    }

    @Override
    public ClassSymbol visit(LetDecl ltdcl) {
        var id = ltdcl.id;
        var type = ltdcl.type;
        var exp = ltdcl.value;

        if (id.getSymbol() == null) {
            return null;
        }

        var currentScope = (LetScope) id.getScope();

        if (currentScope.lookup(type.getText()) == null) {
            SymbolTable.error(ltdcl.ctx, type, "Let variable " + id.getToken().getText() +
                    " has undefined type " + type.getText());
            return null;
        }

        if (exp != null) {
            var exprType = exp.accept(this);
            var currentType = ((IdSymbol)id.getSymbol()).getType();

            if (currentType == null || exprType == null) {
                return null;
            }

            if (!currentType.isParentOrEqual(exprType)) {
                SymbolTable.error(ltdcl.ctx, exp.getToken(), "Type " + exprType.getName() +
                        " of initialization expression of identifier " + id.getToken().getText() +
                        " is incompatible with declared type " + type.getText());
                return null;
            }
        }

        return null;
    }

    @Override
    public ClassSymbol visit(Case id) {

        id.case_value.accept(this);
        ClassSymbol mostCompleteAncestor = id.branch_cases.get(0).accept(this);


        for (CaseRule crl : id.branch_cases) {
            var caseRuleType = crl.accept(this);

            if (caseRuleType == null) {
                continue;
            }

            if (caseRuleType.isParentOrEqual(mostCompleteAncestor)) {
                mostCompleteAncestor = caseRuleType;
            } else if (!mostCompleteAncestor.isParentOrEqual(caseRuleType)) {
                mostCompleteAncestor = ClassSymbol.OBJECT;
            }
        }

        return mostCompleteAncestor;
    }

    @Override
    public ClassSymbol visit(CaseRule crl) {
        var id = crl.id;
        var type = crl.type;
        var blockType = crl.block.accept(this);

        if (id.getSymbol() == null) {
            return null;
        }

        var currentScope = (DefaultScope) id.getScope();

        if (currentScope.lookup(type.getText()) == null) {
            SymbolTable.error(crl.ctx, type, "Case variable " + id.getToken().getText() +
                    " has undefined type " + type.getText());
            return null;
        }

        if (blockType != null) {
            return blockType;
        }

        return ClassSymbol.OBJECT;
    }

    @Override
    public ClassSymbol visit(Id id) {

        if (id.getSymbol() == null) {
            return null;
        }

        var currentScope = id.getScope();

        if (id.getToken().getText().compareTo("self") == 0) {
            return ((IdSymbol)currentScope.lookup("self")).getType();
        }

//        if (currentScope.lookup(id.textInScope) == null) {
//            SymbolTable.error(id.ctx, id.getToken(), "Undefined identifier " + id.textInScope);
//            return null;
//        }

        return ((IdSymbol)id.getSymbol()).getType();
    }

    @Override
    public ClassSymbol visit(AddSub id) {
        var leftType = (ClassSymbol) id.left.accept(this);
        var rightType = (ClassSymbol) id.right.accept(this);

        if (leftType != ClassSymbol.INT && leftType != null) {
            SymbolTable.error(id.ctx, id.left.getToken(), "Operand of " + id.op.getText() +
                    " has type " + leftType.getName() + " instead of Int");
            return null;
        }

        if (rightType != ClassSymbol.INT && rightType != null) {
            SymbolTable.error(id.ctx, id.right.getToken(), "Operand of " + id.op.getText() +
                    " has type " + rightType.getName() + " instead of Int");
            return null;
        }

        if (leftType == null || rightType == null) {
            return null;
        }

        return ClassSymbol.INT;
    }

    @Override
    public ClassSymbol visit(MulDiv id) {
        var leftType = (ClassSymbol) id.left.accept(this);
        var rightType = (ClassSymbol) id.right.accept(this);

        if (leftType != ClassSymbol.INT && leftType != null) {
            SymbolTable.error(id.ctx, id.left.getToken(), "Operand of " + id.op.getText() +
                    " has type " + leftType.getName() + " instead of Int");
            return null;
        }

        if (rightType != ClassSymbol.INT && rightType != null) {
            SymbolTable.error(id.ctx, id.right.getToken(), "Operand of " + id.op.getText() +
                    " has type " + rightType.getName() + " instead of Int");
            return null;
        }

        if (leftType == null || rightType == null) {
            return null;
        }

        return ClassSymbol.INT;
    }

    @Override
    public ClassSymbol visit(Comparasion id) {
        var leftType = (ClassSymbol) id.left.accept(this);
        var rightType = (ClassSymbol) id.right.accept(this);
        List basicTypes = new ArrayList<>(Arrays.asList(ClassSymbol.INT, ClassSymbol.BOOL, ClassSymbol.STRING));

        if (id.op.getText().compareTo("=") == 0) {
            if (basicTypes.contains(leftType) && basicTypes.contains(rightType)) {
                if (leftType == rightType) {
                    return ClassSymbol.BOOL;
                } else {
                    SymbolTable.error(id.ctx, id.op, "Cannot compare " + leftType.getName() +
                            " with " + rightType.getName());
                    return null;
                }
            } else {
                if (basicTypes.contains(leftType)) {
                    SymbolTable.error(id.ctx, id.op, "Cannot compare " + leftType.getName() +
                            " with " + rightType.getName());
                    return null;
                } else if (basicTypes.contains(rightType)) {
                    SymbolTable.error(id.ctx, id.op, "Cannot compare " + leftType.getName() +
                            " with " + rightType.getName());
                    return null;
                } else{
                    return ClassSymbol.BOOL;
                }
            }
        }

        if (leftType != ClassSymbol.INT && leftType != null) {
            SymbolTable.error(id.ctx, id.left.getToken(), "Operand of " + id.op.getText() +
                    " has type " + leftType.getName() + " instead of Int");
            return null;
        }

        if (rightType != ClassSymbol.INT && rightType != null) {
            SymbolTable.error(id.ctx, id.right.getToken(), "Operand of " + id.op.getText() +
                    " has type " + rightType.getName() + " instead of Int");
            return null;
        }

        if (leftType == null || rightType == null) {
            return null;
        }

        return ClassSymbol.BOOL;
    }

    @Override
    public ClassSymbol visit(Neg id) {
        var type = (ClassSymbol) id.value.accept(this);

        if (type != ClassSymbol.INT && type != null) {
            SymbolTable.error(id.ctx, id.value.getToken(), "Operand of " + id.op.getText() +
                    " has type " + type.getName() + " instead of Int");
            return null;
        }

        return ClassSymbol.INT;
    }

    @Override
    public ClassSymbol visit(Not id) {
        var type = (ClassSymbol) id.value.accept(this);
        if (type != ClassSymbol.BOOL && type != null) {
            SymbolTable.error(id.ctx, id.value.getToken(), "Operand of " + id.op.getText() +
                    " has type " + type.getName() + " instead of Bool");
            return null;
        }

        return ClassSymbol.BOOL;
    }

    @Override
    public ClassSymbol visit(Assignment assign) {
        var id = assign.id;
        var idType = assign.id.accept(this);
        var exprType = assign.value.accept(this);

        if (assign.id.getToken().getText().compareTo("self") == 0) {
            SymbolTable.error(id.ctx, id.getToken(), "Cannot assign to self");
            return null;
        }

        if (idType == null || exprType == null) {
            return null;
        }

        if (!idType.isParentOrEqual(exprType)) {
            SymbolTable.error(assign.ctx, assign.value.getToken(), "Type " + exprType.getName() +
                    " of assigned expression is incompatible with declared type " + idType.getName() + " of" +
                    " identifier " + id.getToken().getText());
            return null;
        }

        return idType;
    }

    @Override
    public ClassSymbol visit(IsVoid id) {
        return ClassSymbol.BOOL;
    }

    @Override
    public ClassSymbol visit(Instantiate id) {
        var currentScope = id.id.getScope();

        if (currentScope.lookup(id.type.getText()) == null) {
            SymbolTable.error(id.ctx, id.type, "new is used with undefined type " + id.type.getText());
            return null;
        }
        return (ClassSymbol)currentScope.lookup(id.type.getText());
    }

    @Override
    public ClassSymbol visit(While id) {
        var condType = id.cond.accept(this);

        if (condType == null) {
            return null;
        }

        if (condType != ClassSymbol.BOOL) {
            SymbolTable.error(id.ctx, id.cond.getToken(), "While condition has type " + condType.getName()
            + " instead of Bool");
            return ClassSymbol.OBJECT;
        }

        return id.block.accept(this);
    }

    @Override
    public ClassSymbol visit(If id) {
        var condType = id.cond.accept(this);
        var thanType = id.thenBranch.accept(this);
        var elseType = id.elseBranch.accept(this);

        if (condType == null) {
            return null;
        }

        if (condType != ClassSymbol.BOOL) {
            SymbolTable.error(id.ctx, id.cond.getToken(), "If condition has type " + condType.getName()
                    + " instead of Bool");
            return ClassSymbol.OBJECT;
        }

        if (thanType.isParentOrEqual(elseType)) {
            return thanType;
        } else if (elseType.isParentOrEqual(thanType)) {
            return elseType;
        }

        return ClassSymbol.OBJECT;
    }

    @Override
    public ClassSymbol visit(Block id) {

        ClassSymbol last = null;

        for (Expression e : id.exprList) {
            last = e.accept(this);
        }

        return last;
    }

    @Override
    public ClassSymbol visit(FCall fc) {
        var id = fc.id;
        FunctionSymbol functionScope = null;
        ClassSymbol defaultFunctionTypes = id.getScope() instanceof ClassSymbol ? (ClassSymbol)id.getScope() : null;

        // get parent class of the call
        Scope getClass = id.getScope() instanceof ClassSymbol ? id.getScope() : id.getScope().getParent();

        while (!(getClass instanceof ClassSymbol) && getClass != null) {
            getClass = getClass.getParent();
        }

        // get parent class of function, and get function scope
        var parentScope = (ClassSymbol) getClass;
        var auxScope = parentScope;

        if (parentScope.lookup(id.textInScope) == null) {

            while (auxScope != ClassSymbol.NULL_CLASS) {
                if (auxScope.lookup(id.textInScope) != null) {
                    functionScope = (FunctionSymbol) auxScope.lookup(id.textInScope);
                }
                auxScope = auxScope.getParentClass();
            }

            if (parentScope == ClassSymbol.NULL_CLASS) {
                SymbolTable.error(id.ctx, id.getToken(), "Undefined method " + id.getToken().getText() +
                        " in class " + parentScope.getName());
                return null;
            }
        } else {
            functionScope = (FunctionSymbol) parentScope.lookup(id.textInScope);
        }

        // check for arguments
        List<Symbol> current = new ArrayList<>(functionScope.symbols.values());

        if (current.size() != fc.arglst.size()) {
            SymbolTable.error(id.ctx, id.getToken(), "Method " + id.getToken().getText() +
                    " of class " + parentScope.getName() + " is applied to wrong number of arguments");
            if (defaultFunctionTypes != null && (functionScope).getType().getName().compareTo("SELF_TYPE") == 0) {
                return defaultFunctionTypes;
            }
            return functionScope.getType();
        }

        for (int i = 0; i < current.size(); i++) {
            var initialType = ((IdSymbol) current.get(i)).getType();
            var callType = fc.arglst.get(i).accept(this);
            if (!initialType.isParentOrEqual(callType)) {
                SymbolTable.error(id.ctx, fc.arglst.get(i).getToken(), "In call to method " +
                        id.getToken().getText() + " of class " + parentScope.getName() +
                        ", actual type " + callType.getName() + " of formal parameter " +
                        current.get(i).getName() + " is incompatible with declared type " +
                        initialType.getName());
                if (defaultFunctionTypes != null && (functionScope).getType().getName().compareTo("SELF_TYPE") == 0) {
                    return defaultFunctionTypes;
                }
                return functionScope.getType();
            }
        }

        if (defaultFunctionTypes != null && (functionScope).getType().getName().compareTo("SELF_TYPE") == 0) {
            return defaultFunctionTypes;
        }

        return functionScope.getType();
    }

    @Override
    public ClassSymbol visit(ComplexFCall cfcall) {
        var id = cfcall.id;
        var type = cfcall.type;
        var caller = cfcall.caller;
        var callerType = caller.accept(this);
        var arglst = cfcall.arglst;

        // get parent class of the call or the static dispatch type
        ClassSymbol parentScope;

        if (type != null) {

            if (type.getText().compareTo("SELF_TYPE") == 0) {
                SymbolTable.error(id.ctx, type, "Type of static dispatch cannot" +
                        " be SELF_TYPE");
                return null;
            }

            parentScope = (ClassSymbol) id.getScope().lookup(type.getText());

            if (parentScope == null) {
                SymbolTable.error(id.ctx, type, "Type " + type.getText() + " of static dispatch "
                        + "is undefined");
                return null;
            }

            if (callerType.getName().compareTo("SELF_TYPE") == 0) {
                // get parent class of the call
                Scope getClass = id.getScope() instanceof ClassSymbol ? id.getScope() : id.getScope().getParent();

                while (!(getClass instanceof ClassSymbol) && getClass != null) {
                    getClass = getClass.getParent();
                }

                callerType = (ClassSymbol)getClass;
            }

            if (!parentScope.isParentOrEqual(callerType)) {
                SymbolTable.error(id.ctx, type, "Type " + type.getText() + " of static dispatch "
                        + "is not a superclass of type " + callerType.getName());
                return null;
            }

        } else {

            Scope getClass = id.getScope() instanceof ClassSymbol ? id.getScope() : id.getScope().getParent();

            while (!(getClass instanceof ClassSymbol) && getClass != null) {
                getClass = getClass.getParent();
            }

            if (callerType.getName().compareTo("SELF_TYPE") == 0) {
                parentScope = (ClassSymbol) getClass;
            } else {
                parentScope = caller.accept(this);
            }
        }

        // search for the function scope
        FunctionSymbol functionScope = (FunctionSymbol) parentScope.lookup(id.textInScope);

        var auxScope = parentScope;

        if (functionScope == null) {

            while (auxScope != ClassSymbol.NULL_CLASS) {
                if (auxScope.lookup(id.textInScope) != null) {
                    functionScope = (FunctionSymbol) auxScope.lookup(id.textInScope);
                    break;
                }
                auxScope = auxScope.getParentClass();
            }

        }

        if (functionScope == null) {
            SymbolTable.error(id.ctx, id.getToken(), "Undefined method " + id.getToken().getText() +
                    " in class " + parentScope.getName());
            return null;
        }



        // search in args
        List<Symbol> current = new ArrayList<>(functionScope.symbols.values());

        if (current.size() != arglst.size()) {
            SymbolTable.error(id.ctx, id.getToken(), "Method " + id.getToken().getText() +
                    " of class " + parentScope.getName() + " is applied to wrong number of arguments");
            if (functionScope.getType().getName().compareTo("SELF_TYPE") == 0) {
                return caller.accept(this);
            } else {
                return functionScope.getType();
            }
        }

        for (int i = 0; i < current.size(); i++) {
            var initialType = ((IdSymbol) current.get(i)).getType();
            var callType = arglst.get(i).accept(this);
            if (!initialType.isParentOrEqual(callType)) {
                SymbolTable.error(id.ctx, arglst.get(i).getToken(), "In call to method " +
                        id.getToken().getText() + " of class " + parentScope.getName() +
                        ", actual type " + callType.getName() + " of formal parameter " +
                        current.get(i).getName() + " is incompatible with declared type " +
                        initialType.getName());
                if (functionScope.getType().getName().compareTo("SELF_TYPE") == 0) {
                    return caller.accept(this);
                } else {
                    return functionScope.getType();
                }
            }
        }

        if (functionScope.getType().getName().compareTo("SELF_TYPE") == 0) {
            return caller.accept(this);
        } else {
            return functionScope.getType();
        }
    }

    @Override
    public ClassSymbol visit(Int id) {
        return ClassSymbol.INT;
    }

    @Override
    public ClassSymbol visit(Bool id) {
        return ClassSymbol.BOOL;
    }

    @Override
    public ClassSymbol visit(StringNode id) {
        return ClassSymbol.STRING;
    }
}