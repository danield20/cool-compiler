package cool.compiler;

import cool.nodes.*;
import cool.structures.SymbolTable;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import cool.lexer.*;
import cool.parser.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Compiler {
    // Annotates class nodes with the names of files where they are defined.
    public static ParseTreeProperty<String> fileNames = new ParseTreeProperty<>();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No file(s) given");
            return;
        }

        CoolLexer lexer = null;
        CommonTokenStream tokenStream = null;
        CoolParser parser = null;
        ParserRuleContext globalTree = null;

        // True if any lexical or syntax errors occur.
        boolean lexicalSyntaxErrors = false;

        // Parse each input file and build one big parse tree out of
        // individual parse trees.
        for (var fileName : args) {
            var input = CharStreams.fromFileName(fileName);

            // Lexer
            if (lexer == null)
                lexer = new CoolLexer(input);
            else
                lexer.setInputStream(input);

            // Token stream
            if (tokenStream == null)
                tokenStream = new CommonTokenStream(lexer);
            else
                tokenStream.setTokenSource(lexer);


             //Test lexer only.
//            tokenStream.fill();
//            List<Token> tokens = tokenStream.getTokens();
//            tokens.stream().forEach(token -> {
//                var text = token.getText();
//                var name = CoolLexer.VOCABULARY.getSymbolicName(token.getType());
//
//                System.out.println(text + " : " + name);
//                //System.out.println(token);
//            });


            // Parser
            if (parser == null)
                parser = new CoolParser(tokenStream);
            else
                parser.setTokenStream(tokenStream);

            // Customized error listener, for including file names in error
            // messages.
            var errorListener = new BaseErrorListener() {
                public boolean errors = false;

                @Override
                public void syntaxError(Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg,
                                        RecognitionException e) {

                    String newMsg = "\"" + new File(fileName).getName() + "\", line " +
                            line + ":" + charPositionInLine + ", ";

                    Token token = (Token) offendingSymbol;
                    if (token.getType() == CoolLexer.ERROR)
                        newMsg += "Lexical error: " + token.getText();
                    else
                        newMsg += "Syntax error: " + msg;

                    System.err.println(newMsg);
                    errors = true;
                }
            };

            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Actual parsing
            var tree = parser.program();
            if (globalTree == null)
                globalTree = tree;
            else
                // Add the current parse tree's children to the global tree.
                for (int i = 0; i < tree.getChildCount(); i++)
                    globalTree.addAnyChild(tree.getChild(i));

            // Annotate class nodes with file names, to be used later
            // in semantic error messages.
            for (int i = 0; i < tree.getChildCount(); i++) {
                var child = tree.getChild(i);
                // The only ParserRuleContext children of the program node
                // are class nodes.
                if (child instanceof ParserRuleContext)
                    fileNames.put(child, fileName);
            }

            // Record any lexical or syntax errors.
            lexicalSyntaxErrors |= errorListener.errors;
        }

        // Stop before semantic analysis phase, in case errors occurred.
        if (lexicalSyntaxErrors) {
            System.err.println("Compilation halted");
            return;
        }

        var astConstructionVisitor = new CoolParserBaseVisitor<ASTNode>() {
            @Override
            public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
                ArrayList<ClassNode> classlist = new ArrayList<>();

                for (ParseTree currentClass : ctx.classG()) {
                    classlist.add((ClassNode) visit(currentClass));
                }

                return new Program(classlist, ctx.start, ctx);
            }

            @Override
            public ASTNode visitClassG(CoolParser.ClassGContext ctx) {
                ArrayList<Feature> featureList = new ArrayList<>();

                for (ParseTree currentFeature : ctx.feature()) {
                    featureList.add((Feature) visit(currentFeature));
                }

                return new ClassNode(ctx.className, ctx.inheritType, featureList, ctx.start, ctx);
            }

            @Override
            public ASTNode visitFuncFeature(CoolParser.FuncFeatureContext ctx) {
                ArrayList<Formal> formalList = new ArrayList<>();

                for (ParseTree currentFormal : ctx.formal()) {
                    formalList.add((Formal) visit(currentFormal));
                }

                return new FuncFeature(
                        ctx.ID().getSymbol(),
                        formalList,
                        ctx.TYPE().getSymbol(),
                        (Expression) visit(ctx.body),
                        ctx.start,
                        ctx);

            }

            @Override
            public ASTNode visitVarFeature(CoolParser.VarFeatureContext ctx) {
                return new VarFeature(
                        ctx.ID().getSymbol(),
                        ctx.TYPE().getSymbol(),
                        ctx.value == null ? null : (Expression) visit(ctx.value),
                        ctx.start,
                        ctx);
            }

            @Override
            public ASTNode visitFormal(CoolParser.FormalContext ctx) {
                return new Formal(ctx.ID().getSymbol(), ctx.TYPE().getSymbol(), ctx.start, ctx);
            }

            private TerminalNode nvl(TerminalNode first, TerminalNode second, TerminalNode third) {
                if (first == null) {
                    if (second == null)
                        return third;
                    return second;
                }
                return first;
            }

            @Override
            public ASTNode visitComparison(CoolParser.ComparisonContext ctx) {
                return new Comparasion((Expression) visit(ctx.left),
                        nvl(ctx.LT(), ctx.LE(), ctx.EQUAL()).getSymbol(),
                        (Expression) visit(ctx.right),
                        ctx.start,
                        ctx);
            }

            @Override
            public ASTNode visitString(CoolParser.StringContext ctx) {
                return new StringNode(ctx.STRING().getSymbol(), ctx);
            }

            @Override
            public ASTNode visitBool(CoolParser.BoolContext ctx) {
                return new Bool(ctx.BOOL().getSymbol(), ctx);
            }

            @Override
            public ASTNode visitAssignment(CoolParser.AssignmentContext ctx) {
                return new Assignment(ctx.ID().getSymbol(), (Expression) visit(ctx.value), ctx.start, ctx);
            }

            @Override
            public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
                return new IsVoid((Expression) visit(ctx.value), ctx.ISVOID().getSymbol(), ctx.start, ctx);
            }

            @Override
            public ASTNode visitAddSub(CoolParser.AddSubContext ctx) {
                return new AddSub((Expression) visit(ctx.left),
                        ctx.PLUS() == null ? ctx.MINUS().getSymbol() : ctx.PLUS().getSymbol(),
                        (Expression) visit(ctx.right),
                        ctx.start,
                        ctx);
            }

            @Override
            public ASTNode visitWhile(CoolParser.WhileContext ctx) {
                return new While(ctx.start, ctx, (Expression) visit(ctx.cond), (Expression) visit(ctx.block));
            }

            @Override
            public ASTNode visitInstantiate(CoolParser.InstantiateContext ctx) {
                return new Instantiate(ctx.start, ctx, ctx.TYPE().getSymbol());
            }

            @Override
            public ASTNode visitInt(CoolParser.IntContext ctx) {
                return new Int(ctx.INT().getSymbol(), ctx);
            }

            @Override
            public ASTNode visitMulDiv(CoolParser.MulDivContext ctx) {
                return new MulDiv((Expression) visit(ctx.left),
                        (ctx.MULT() == null ? ctx.DIV() : ctx.MULT()).getSymbol(),
                        (Expression) visit(ctx.right),
                        ctx.start,
                        ctx);
            }

            @Override
            public ASTNode visitCall(CoolParser.CallContext ctx) {
                ArrayList<Expression> exprlst = new ArrayList<>();

                for (ParseTree expr : ctx.expr()) {
                    exprlst.add((Expression) visit(expr));
                }

                return new FCall(ctx.start, ctx, ctx.ID().getSymbol(), exprlst);
            }

            @Override
            public ASTNode visitNeg(CoolParser.NegContext ctx) {
                return new Neg(ctx.NEG().getSymbol(), (Expression) visit(ctx.value), ctx.start, ctx);
            }

            @Override
            public ASTNode visitNot(CoolParser.NotContext ctx) {
                return new Not(ctx.NOT().getSymbol(), (Expression) visit(ctx.value), ctx.start, ctx);
            }

            @Override
            public ASTNode visitParenthesized(CoolParser.ParenthesizedContext ctx) {
                return visit(ctx.expr());
            }

            @Override
            public ASTNode visitBlock(CoolParser.BlockContext ctx) {
                ArrayList<Expression> exprlst = new ArrayList<>();

                for (ParseTree expr : ctx.expr()) {
                    exprlst.add((Expression) visit(expr));
                }

                return new Block(ctx.start, ctx, exprlst);
            }

            @Override
            public ASTNode visitLet(CoolParser.LetContext ctx) {
                ArrayList<LetDecl> vars = new ArrayList<>();

                for (ParseTree dcl : ctx.let_decl()) {
                    vars.add((LetDecl) visit(dcl));
                }
                return new Let(ctx.start, ctx, vars, (Expression) visit(ctx.expr()));
            }

            @Override
            public ASTNode visitComplexcall(CoolParser.ComplexcallContext ctx) {
                ArrayList<Expression> exprlst = new ArrayList<>();

                for (CoolParser.ExprContext expr : ctx.expr()) {
                    if (ctx.expr().indexOf(expr) == 0)
                        continue;
                    exprlst.add((Expression) visit(expr));
                }

                return new ComplexFCall(
                        ctx.start,
                        ctx,
                        ctx.ID().getSymbol(),
                        ctx.TYPE() != null ? ctx.TYPE().getSymbol() : null,
                        (Expression) visit(ctx.caller),
                        exprlst);
            }

            @Override
            public ASTNode visitId(CoolParser.IdContext ctx) {
                return new Id(ctx.ID().getSymbol(), ctx);
            }

            @Override
            public ASTNode visitIf(CoolParser.IfContext ctx) {
                return new If(
                        (Expression) visit(ctx.cond),
                        (Expression) visit(ctx.thenBranch),
                        (Expression) visit(ctx.elseBranch),
                        ctx.start,
                        ctx);
            }

            @Override
            public ASTNode visitCase(CoolParser.CaseContext ctx) {
                ArrayList<CaseRule> checkCases = new ArrayList<>();

                for (CoolParser.Case_ruleContext currentRule : ctx.case_rule()) {
                    checkCases.add((CaseRule) visit(currentRule));
                }

                return new Case(ctx.start, ctx, (Expression) visit(ctx.value_case), checkCases);
            }

            @Override
            public ASTNode visitCase_rule(CoolParser.Case_ruleContext ctx) {
                return new CaseRule(ctx.ID().getSymbol(),
                        ctx.TYPE().getSymbol(),
                        (Expression) visit(ctx.expr()),
                        ctx.start,
                        ctx);
            }

            @Override
            public ASTNode visitLet_decl(CoolParser.Let_declContext ctx) {
                return new LetDecl(
                        ctx.ID().getSymbol(),
                        ctx.TYPE().getSymbol(),
                        ctx.expr() == null ? null : (Expression) visit(ctx.expr()),
                        ctx.start,
                        ctx);


            }
        };

        var ast = astConstructionVisitor.visit(globalTree);

//        var printVisitor = new ASTVisitor<Void>() {
//            int indent = 0;
//
//            void printIndent(String str) {
//                for (int i = 0; i < indent; i++)
//                    System.out.print("  ");
//                System.out.println(str);
//            }
//
//            @Override
//            public Void visit(Id id) {
//                printIndent(id.token.getText());
//                return null;
//            }
//
//            @Override
//            public Void visit(Int id) {
//                printIndent(id.token.getText());
//                return null;
//            }
//
//            @Override
//            public Void visit(StringNode id) {
//                printIndent(id.token.getText());
//                return null;
//            }
//
//            @Override
//            public Void visit(Bool id) {
//                printIndent(id.token.getText());
//                return null;
//            }
//
//            @Override
//            public Void visit(If id) {
//                printIndent("if");
//                indent++;
//                id.cond.accept(this);
//                id.thenBranch.accept(this);
//                id.elseBranch.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Assignment id) {
//                printIndent("<-");
//                indent++;
//                printIndent(id.getToken().getText());
//                id.value.accept(this);
//                indent--;
//                return null;
//            }
//
//            private Void printBinarOp(Token op, Expression left, Expression right) {
//                printIndent(op.getText());
//                indent++;
//                left.accept(this);
//                right.accept(this);
//                indent--;
//                return null;
//            }
//
//
//            @Override
//            public Void visit(AddSub id) {
//                return printBinarOp(id.op, id.left, id.right);
//            }
//
//            @Override
//            public Void visit(MulDiv id) {
//                return printBinarOp(id.op, id.left, id.right);
//            }
//
//            @Override
//            public Void visit(Comparasion id) {
//                return printBinarOp(id.op, id.left, id.right);
//            }
//
//            @Override
//            public Void visit(IsVoid id) {
//                printIndent(id.op.getText());
//                indent++;
//                id.void_expr.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Neg id) {
//                printIndent(id.op.getText());
//                indent++;
//                id.value.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Not id) {
//                printIndent(id.op.getText());
//                indent++;
//                id.value.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Program id) {
//                printIndent("program");
//                indent++;
//                for (ClassNode cClass : id.classes) {
//                    cClass.accept(this);
//                }
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(ClassNode id) {
//                printIndent("class");
//                indent++;
//                printIndent(id.id.getToken().getText());
//                if (id.inheritClass != null) {
//                    printIndent(id.inheritClass.getText());
//                }
//                for (Feature feature : id.features) {
//                    feature.accept(this);
//                }
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(FuncFeature id) {
//                printIndent("method");
//                indent++;
//                printIndent(id.id.getToken().getText());
//                for (Formal f : id.paramList) {
//                    f.accept(this);
//                }
//                printIndent(id.returnType.getText());
//                id.body.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(VarFeature id) {
//                printIndent("attribute");
//                indent++;
//                printIndent(id.id.getToken().getText());
//                printIndent(id.type.getText());
//                if (id.value != null) {
//                    id.value.accept(this);
//                }
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Formal id) {
//                printIndent("formal");
//                indent++;
//                printIndent(id.id.getToken().getText());
//                printIndent(id.type.getText());
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Case id) {
//                printIndent("case");
//                indent++;
//                id.case_value.accept(this);
//                for (CaseRule c : id.branch_cases) {
//                    c.accept(this);
//                }
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(CaseRule id) {
//                printIndent("case branch");
//                indent++;
//                printIndent(id.id.getToken().getText());
//                printIndent(id.type.getText());
//                id.block.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Block id) {
//                printIndent("block");
//                indent++;
//                for (Expression expr : id.exprList) {
//                    expr.accept(this);
//                }
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Instantiate id) {
//                printIndent("new");
//                indent++;
//                printIndent(id.type.getText());
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(While id) {
//                printIndent("while");
//                indent++;
//                id.cond.accept(this);
//                id.block.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(FCall id) {
//                printIndent("implicit dispatch");
//                indent++;
//                printIndent(id.id.getToken().getText());
//                for (Expression arg : id.arglst) {
//                    arg.accept(this);
//                }
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(ComplexFCall id) {
//                printIndent(".");
//                indent++;
//                id.caller.accept(this);
//                if (id.type != null) {
//                    printIndent(id.type.getText());
//                }
//                printIndent(id.id.getToken().getText());
//                for (Expression arg : id.arglst) {
//                    arg.accept(this);
//                }
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(Let id) {
//                printIndent("let");
//                indent++;
//                for (LetDecl l : id.vars) {
//                    l.accept(this);
//                }
//                id.body.accept(this);
//                indent--;
//                return null;
//            }
//
//            @Override
//            public Void visit(LetDecl id) {
//                printIndent("local");
//                indent++;
//                printIndent(id.id.getToken().getText());
//                printIndent(id.type.getText());
//                if (id.value != null) {
//                    id.value.accept(this);
//                }
//                indent--;
//                return null;
//            }
//        };
//
//        ast.accept(printVisitor);

        SymbolTable.defineBasicClasses();

        var definitionPassVisitor = new DefinitionPassVisitor();

        ast.accept(definitionPassVisitor);

        var resolutionPassVisitor = new ResolutionPassVisitor();

        ast.accept(resolutionPassVisitor);

        if (SymbolTable.hasSemanticErrors()) {
            System.err.println("Compilation halted");
        }

        var parseList = args[0].split("/");
        var clName = parseList[parseList.length - 1];
        var codeGen = new CodeGenVisitor(clName);
        var t = ast.accept(codeGen);
        System.out.println(t.render());
    }
}
