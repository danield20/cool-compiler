package cool.structures;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.antlr.v4.runtime.*;

import cool.compiler.Compiler;
import cool.parser.CoolParser;

public class SymbolTable {
    public static Scope globals;
    
    private static boolean semanticErrors;
    
    public static void defineBasicClasses() {
        globals = new DefaultScope(null);
        semanticErrors = false;

        // add basic functions to classes

        // Object basic func
        ClassSymbol.OBJECT.addFunction("abort", ClassSymbol.OBJECT, new ArrayList<>());
        ClassSymbol.OBJECT.addFunction("type_name", ClassSymbol.STRING, new ArrayList<>());
        ClassSymbol.OBJECT.addFunction("copy", ClassSymbol.SELF_TYPE, new ArrayList<>());


        // IO basic func
        var argSymbol = new IdSymbol("x");
        argSymbol.setType(ClassSymbol.STRING);

        ClassSymbol.IO.addFunction("out_string", ClassSymbol.SELF_TYPE, new ArrayList<>(
                Arrays.asList(argSymbol))
        );

        var arg2Symbol = new IdSymbol("x");
        arg2Symbol.setType(ClassSymbol.INT);

        ClassSymbol.IO.addFunction("out_int", ClassSymbol.SELF_TYPE, new ArrayList<>(
                Arrays.asList(arg2Symbol))
        );

        ClassSymbol.IO.addFunction("in_string", ClassSymbol.STRING, new ArrayList<>());
        ClassSymbol.IO.addFunction("in_int", ClassSymbol.INT, new ArrayList<>());


        // String basic func
        ClassSymbol.STRING.addFunction("length", ClassSymbol.INT, new ArrayList<>());

        var arg3Symbol = new IdSymbol("s");
        arg3Symbol.setType(ClassSymbol.STRING);

        ClassSymbol.STRING.addFunction("concat", ClassSymbol.STRING, new ArrayList<>(
                Arrays.asList(arg3Symbol))
        );

        var arg4Symbol = new IdSymbol("i");
        arg4Symbol.setType(ClassSymbol.INT);

        var arg5Symbol = new IdSymbol("l");
        arg5Symbol.setType(ClassSymbol.INT);

        ClassSymbol.STRING.addFunction("substr", ClassSymbol.STRING, new ArrayList<>(
                Arrays.asList(arg4Symbol, arg5Symbol))
        );


        globals.add(ClassSymbol.BOOL);
        globals.add(ClassSymbol.INT);
        globals.add(ClassSymbol.IO);
        globals.add(ClassSymbol.STRING);
        globals.add(ClassSymbol.OBJECT);
        globals.add(ClassSymbol.SELF_TYPE);
    }
    
    /**
     * Displays a semantic error message.
     * 
     * @param ctx Used to determine the enclosing class context of this error,
     *            which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str) {
        while (! (ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();
        
        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static void error(String str) {
        String message = "Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}
