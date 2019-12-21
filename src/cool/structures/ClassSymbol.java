package cool.structures;

import org.antlr.v4.runtime.*;

import java.util.*;

public class ClassSymbol extends Symbol implements Scope{
    public Map<String, Symbol> symbols = new LinkedHashMap<>();

    protected Scope parent;

    protected ClassSymbol parentClass = ClassSymbol.OBJECT;

    public ClassSymbol(Scope parent, String name) {
        super(name);
        this.parent = parent;
    }

    public ClassSymbol(Scope parent, String name, ClassSymbol parentClass) {
        super(name);
        this.parent = parent;
        this.parentClass = parentClass;
    }

    public boolean add(Symbol sym) {
        // Ne asigurăm că simbolul nu există deja în domeniul de vizibilitate
        // curent.
        if (symbols.containsKey(sym.getName()))
            return false;

        symbols.put(sym.getName(), sym);

        return true;
    }

    public Symbol lookup(String s) {
        var sym = symbols.get(s);

        if (sym != null)
            return sym;

        // Dacă nu găsim simbolul în domeniul de vizibilitate curent, îl căutăm
        // în domeniul de deasupra.
        if (parent != null)
            return parent.lookup(s);

        return null;
    }

    public Symbol lookupCurrent(String s) {
        var sym = symbols.get(s);

        if (sym != null)
            return sym;

        return null;
    }

    public boolean isParentOrEqual(ClassSymbol type2) {
        var currentParrent = type2;
        while (currentParrent.getName().compareTo("NULL_CLASS") != 0) {
            if (currentParrent.getName().compareTo(this.getName()) == 0) {
                return true;
            }
            currentParrent = currentParrent.getParentClass();
        }
        return false;
    }

    public void setParentClass(ClassSymbol parent) {
        parentClass = parent;
    }

    public ClassSymbol getParentClass() {
        return parentClass;
    }

    public Scope getParent() {
        return parent;
    }

    public void addFunction(String name, ClassSymbol type, ArrayList<IdSymbol> args) {
        if (args.size() == 0) {
            var newFunc = new FunctionSymbol(this, name);
            newFunc.setType(type);
            this.add(newFunc);
        } else {
            var newFunc = new FunctionSymbol(this, name);
            newFunc.setType(type);
            for (IdSymbol k : args) {
                newFunc.add(k);
            }
            this.add(newFunc);
        }
    }

    public Map<String, Symbol> getFormals() {
        return symbols;
    }

    // Symboluri aferente tipurilor, definite global
    public static  ClassSymbol NULL_CLASS = new ClassSymbol(null, "NULL_CLASS", null);
    public static  ClassSymbol OBJECT    = new ClassSymbol(null, "Object",  ClassSymbol.NULL_CLASS);
    public static  ClassSymbol INT       = new ClassSymbol(null, "Int",  ClassSymbol.OBJECT);
    public static  ClassSymbol STRING    = new ClassSymbol(null, "String",  ClassSymbol.OBJECT);
    public static  ClassSymbol BOOL      = new ClassSymbol(null, "Bool",  ClassSymbol.OBJECT);
    public static  ClassSymbol IO        = new ClassSymbol(null, "IO",  ClassSymbol.OBJECT);
    public static  ClassSymbol SELF_TYPE = new ClassSymbol(null, "SELF_TYPE");
    public static  List invalidParents   = new ArrayList(Arrays.asList("Int", "String", "Bool", "SELF_TYPE"));
}
