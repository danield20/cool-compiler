package cool.structures;

public class LetScope extends FunctionSymbol {
    public boolean f_call = false;
    public LetScope(Scope parent) {super(parent, "let");}
}
