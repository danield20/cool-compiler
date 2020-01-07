package cool.compiler;

import cool.nodes.*;
import cool.structures.ClassSymbol;
import cool.structures.FunctionSymbol;
import cool.structures.IdSymbol;
import cool.structures.LetScope;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.util.*;
import java.util.regex.Pattern;

public class CodeGenVisitor implements ASTVisitor<ST> {
    static STGroupFile templates = new STGroupFile("cool/compiler/cgen.stg");
    String filename;
    int dispatchNumber = -1;
    int ifnumber = -1;
    int isvoidnumber = -1;
    int notnumber = -1;
    int equalsNumber = -1;
    int compareNumber = -1;
    int whileNumber = -1;
    ST dataSection;
    ST textSection;
    ST stringConstSection;
    ST intConstSection;
    ST classSection;
    ST prototypeObjSection;
    ST dispatchTableSection;
    ST initSection;
    ST funcDeclSection;
    int intTag = 2;
    int stringTag = 3;
    int boolTag = 4;
    ResolutionPassVisitor res = new ResolutionPassVisitor();
    HashMap<String, Integer> string_consts = new LinkedHashMap<>();
    HashMap<Integer, Integer> int_consts = new LinkedHashMap<>();
    HashMap<String, Integer> class_map = new LinkedHashMap<>();
    ClassNode currentClass;
    Program prg;
    HashMap<String, LinkedHashMap<String,VarFeature>> class_vars = new LinkedHashMap<>();
    HashMap<String,LinkedHashMap<String,FuncFeature>> class_funcs = new LinkedHashMap<>();
    List<String> basicClasses = new ArrayList<>(Arrays.asList("Object", "IO", "String", "Int","Bool"));


    public CodeGenVisitor (String file) {
        filename = file;
    }

    public String globalData() {
        String returnS =    "    .align  2\n" +
                            "    .globl  class_nameTab\n" +
                            "    .globl  Int_protObj\n" +
                            "    .globl  String_protObj\n" +
                            "    .globl  bool_const0\n" +
                            "    .globl  bool_const1\n" +
                            "    .globl  Main_protObj\n" +
                            "    .globl  _int_tag\n" +
                            "    .globl  _string_tag\n" +
                            "    .globl  _bool_tag\n" +
                            "_int_tag:\n" +
                            "    .word   2\n" +
                            "_string_tag:\n" +
                            "    .word   3\n" +
                            "_bool_tag:\n" +
                            "    .word   4";
        return returnS;
    }

    public String escapeString(String k) {
        return k.replace("\n","\\n");
    }

    public void addString(String currentString) {
        ST stringConstTemplate = templates.getInstanceOf("stringconst");
        ST intConstTemplate = templates.getInstanceOf("intconst");
        int currentStringIndex = string_consts.containsKey(currentString) ? string_consts.get(currentString) : string_consts.size();
        int currentIntIndex = int_consts.containsKey(currentString.length()) ? int_consts.get(currentString.length()) : int_consts.size();

        if (!string_consts.containsKey(currentString)) {
            string_consts.put(currentString, string_consts.size());
            stringConstSection.add("e", stringConstTemplate.add("tag", stringTag).add("string", currentString)
                    .add("dimension", 4 + (int)Math.ceil((float)(1 + currentString.length()) / 4)).add("int_const", currentIntIndex).add("index", currentStringIndex));
        }

        if (!int_consts.containsKey(currentString.length())) {
            int_consts.put(currentString.length(), int_consts.size());
            intConstSection.add("e", intConstTemplate.add("index", currentIntIndex).add("tag", intTag)
                    .add("val", currentString.length()));
        }
    }

    public void addInt(String currentInt) {
        Integer cur_int = Integer.parseInt(currentInt);
        ST intConstTemplate = templates.getInstanceOf("intconst");
        int currentIntIndex = int_consts.containsKey(cur_int) ? int_consts.get(cur_int) : int_consts.size();
        if (!int_consts.containsKey(cur_int)) {
            int_consts.put(cur_int, int_consts.size());
            intConstSection.add("e", intConstTemplate.add("index", currentIntIndex).add("tag", intTag)
                    .add("val", cur_int));
        }
    }

    public void stringAndIntBasicConsts() {

        addString("");
        addString("Object");
        class_map.put("Object", class_map.size());
        addString("IO");
        class_map.put("IO", class_map.size());
        addString("Int");
        class_map.put("Int", class_map.size());
        addString("String");
        class_map.put("String", class_map.size());
        addString("Bool");
        class_map.put("Bool", class_map.size());

        prototypeObjSection.add("e", "\nObject_protObj:\n" +
                                                    "    .word   0\n" +
                                                    "    .word   3\n" +
                                                    "    .word   Object_dispTab\n" +
                                                    "IO_protObj:\n" +
                                                    "    .word   1\n" +
                                                    "    .word   3\n" +
                                                    "    .word   IO_dispTab\n" +
                                                    "Int_protObj:\n" +
                                                    "    .word   2\n" +
                                                    "    .word   4\n" +
                                                    "    .word   Int_dispTab\n" +
                                                    "    .word   0\n" +
                                                    "String_protObj:\n" +
                                                    "    .word   3\n" +
                                                    "    .word   5\n" +
                                                    "    .word   String_dispTab\n" +
                                                    "    .word   int_const0\n" +
                                                    "    .asciiz \"\"\n" +
                                                    "    .align  2\n" +
                                                    "Bool_protObj:\n" +
                                                    "    .word   4\n" +
                                                    "    .word   4\n" +
                                                    "    .word   Bool_dispTab\n" +
                                                    "    .word   0");

        dispatchTableSection.add("e", "\nObject_dispTab:\n" +
                                                    "    .word   Object.abort\n" +
                                                    "    .word   Object.type_name\n" +
                                                    "    .word   Object.copy\n" +
                                                    "IO_dispTab:\n" +
                                                    "    .word   Object.abort\n" +
                                                    "    .word   Object.type_name\n" +
                                                    "    .word   Object.copy\n" +
                                                    "    .word   IO.out_string\n" +
                                                    "    .word   IO.out_int\n" +
                                                    "    .word   IO.in_string\n" +
                                                    "    .word   IO.in_int\n" +
                                                    "Int_dispTab:\n" +
                                                    "    .word   Object.abort\n" +
                                                    "    .word   Object.type_name\n" +
                                                    "    .word   Object.copy\n" +
                                                    "String_dispTab:\n" +
                                                    "    .word   Object.abort\n" +
                                                    "    .word   Object.type_name\n" +
                                                    "    .word   Object.copy\n" +
                                                    "    .word   String.length\n" +
                                                    "    .word   String.concat\n" +
                                                    "    .word   String.substr\n" +
                                                    "Bool_dispTab:\n" +
                                                    "    .word   Object.abort\n" +
                                                    "    .word   Object.type_name\n" +
                                                    "    .word   Object.copy");

        initSection.add("e",    "    .globl  Int_init\n" +
                                            "    .globl  String_init\n" +
                                            "    .globl  Bool_init\n" +
                                            "    .globl  Main_init\n" +
                                            "    .globl  Main.main\n" +
                                            "Object_init:\n" +
                                            "    addiu   $sp $sp -12\n" +
                                            "    sw      $fp 12($sp)\n" +
                                            "    sw      $s0 8($sp)\n" +
                                            "    sw      $ra 4($sp)\n" +
                                            "    addiu   $fp $sp 4\n" +
                                            "    move    $s0 $a0\n" +
                                            "    move    $a0 $s0\n" +
                                            "    lw      $fp 12($sp)\n" +
                                            "    lw      $s0 8($sp)\n" +
                                            "    lw      $ra 4($sp)\n" +
                                            "    addiu   $sp $sp 12\n" +
                                            "    jr      $ra\n" +
                                            "IO_init:\n" +
                                            "    addiu   $sp $sp -12\n" +
                                            "    sw      $fp 12($sp)\n" +
                                            "    sw      $s0 8($sp)\n" +
                                            "    sw      $ra 4($sp)\n" +
                                            "    addiu   $fp $sp 4\n" +
                                            "    move    $s0 $a0\n" +
                                            "    jal     Object_init\n" +
                                            "    move    $a0 $s0\n" +
                                            "    lw      $fp 12($sp)\n" +
                                            "    lw      $s0 8($sp)\n" +
                                            "    lw      $ra 4($sp)\n" +
                                            "    addiu   $sp $sp 12\n" +
                                            "    jr      $ra\n" +
                                            "Int_init:\n" +
                                            "    addiu   $sp $sp -12\n" +
                                            "    sw      $fp 12($sp)\n" +
                                            "    sw      $s0 8($sp)\n" +
                                            "    sw      $ra 4($sp)\n" +
                                            "    addiu   $fp $sp 4\n" +
                                            "    move    $s0 $a0\n" +
                                            "    jal     Object_init\n" +
                                            "    move    $a0 $s0\n" +
                                            "    lw      $fp 12($sp)\n" +
                                            "    lw      $s0 8($sp)\n" +
                                            "    lw      $ra 4($sp)\n" +
                                            "    addiu   $sp $sp 12\n" +
                                            "    jr      $ra\n" +
                                            "String_init:\n" +
                                            "    addiu   $sp $sp -12\n" +
                                            "    sw      $fp 12($sp)\n" +
                                            "    sw      $s0 8($sp)\n" +
                                            "    sw      $ra 4($sp)\n" +
                                            "    addiu   $fp $sp 4\n" +
                                            "    move    $s0 $a0\n" +
                                            "    jal     Object_init\n" +
                                            "    move    $a0 $s0\n" +
                                            "    lw      $fp 12($sp)\n" +
                                            "    lw      $s0 8($sp)\n" +
                                            "    lw      $ra 4($sp)\n" +
                                            "    addiu   $sp $sp 12\n" +
                                            "    jr      $ra\n" +
                                            "Bool_init:\n" +
                                            "    addiu   $sp $sp -12\n" +
                                            "    sw      $fp 12($sp)\n" +
                                            "    sw      $s0 8($sp)\n" +
                                            "    sw      $ra 4($sp)\n" +
                                            "    addiu   $fp $sp 4\n" +
                                            "    move    $s0 $a0\n" +
                                            "    jal     Object_init\n" +
                                            "    move    $a0 $s0\n" +
                                            "    lw      $fp 12($sp)\n" +
                                            "    lw      $s0 8($sp)\n" +
                                            "    lw      $ra 4($sp)\n" +
                                            "    addiu   $sp $sp 12\n" +
                                            "    jr      $ra");

        LinkedHashMap<String, FuncFeature> objVals = new LinkedHashMap<>();
        objVals.put("Object.abort", null);
        objVals.put("Object.type_name", null);
        objVals.put("Object.copy", null);
        LinkedHashMap<String, FuncFeature> stringVals = new LinkedHashMap<>();
        stringVals.put("Object.abort", null);
        stringVals.put("Object.type_name", null);
        stringVals.put("Object.copy", null);
        stringVals.put("String.length", null);
        stringVals.put("String.concat", null);
        stringVals.put("String.substr", null);
        LinkedHashMap<String, FuncFeature> ioVals = new LinkedHashMap<>();
        ioVals.put("Object.abort", null);
        ioVals.put("Object.type_name", null);
        ioVals.put("Object.copy", null);
        ioVals.put("IO.out_string", null);
        ioVals.put("IO.out_int", null);
        ioVals.put("IO.in_string", null);
        ioVals.put("IO.in_int", null);


        class_funcs.put("Object", objVals);
        class_funcs.put("String", stringVals);
        class_funcs.put("IO", ioVals);
        class_funcs.put("Bool", objVals);
        class_funcs.put("Int", objVals);
    }

    public void makeClassSection() {
        ST stringseq = templates.getInstanceOf("sequence");
        ST classnametag = templates.getInstanceOf("class_template");

        for (String key : class_map.keySet()) {
            stringseq.add("e" , ".word   str_const" + string_consts.get(key));
        }

        classnametag.add("seq", stringseq).add("title", "nameTab");

        classSection.add("e", classnametag);

        ST classObjTab = templates.getInstanceOf("class_template");
        stringseq = templates.getInstanceOf("sequence");

        for (String key : class_map.keySet()) {
            stringseq.add("e" , ".word   " + key + "_protObj");
            stringseq.add("e" , ".word   " + key + "_init");
        }

        classObjTab.add("seq", stringseq).add("title", "objTab");

        classSection.add("e", classObjTab);
    }

    public ClassNode getParentNodeClass(ClassNode cls) {

        if (cls.inheritClass == null) {
            return null;
        }

        for (ClassNode currentcls : prg.classes) {
            if (currentcls.id.getToken().getText().compareTo(cls.inheritClass.getText()) == 0) {
                return currentcls;
            }
        }

        return null;
    }

    public LinkedHashMap<String, VarFeature> getVarList(ClassNode cls) {
        ClassNode parentClass = getParentNodeClass(cls);

        ArrayList<VarFeature> currentVars = new ArrayList<>();
        LinkedHashMap<String, VarFeature> inheritedVars = null;
        ArrayList<VarFeature> finalVars = new ArrayList<>();

        if (parentClass != null) {
            inheritedVars = getVarList(parentClass);
        }

        for (Feature f: cls.features) {
            if (f instanceof VarFeature) {
                currentVars.add((VarFeature)f);

            }
        }

        if (inheritedVars != null) {
            for (String v1 : inheritedVars.keySet()) {
                boolean exists = false;
                for (VarFeature v2 : currentVars) {
                    if (v1.compareTo(v2.id.getToken().getText()) == 0) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    finalVars.add(inheritedVars.get(v1));
                }
            }
        }

        finalVars.addAll(currentVars);

        LinkedHashMap<String, VarFeature> last_vars = new LinkedHashMap<>();

        for (VarFeature v : finalVars) {
            last_vars.put(v.id.getToken().getText(), v);
        }

        return last_vars;
    }

    public LinkedHashMap<String, FuncFeature> getFuncList(ClassNode cls) {

        ArrayList<FuncFeature> currentVars = new ArrayList<>();
        LinkedHashMap<String, FuncFeature> inheritedVals = new LinkedHashMap<>();
        inheritedVals.put("Object.abort", null);
        inheritedVals.put("Object.type_name", null);
        inheritedVals.put("Object.copy", null);


        if (cls.inheritClass != null)
            if (basicClasses.contains(cls.inheritClass.getText())) {
                if (cls.inheritClass.getText().compareTo("IO") == 0) {
                    inheritedVals.put("IO.out_string", null);
                    inheritedVals.put("IO.out_int", null);
                    inheritedVals.put("IO.in_string", null);
                    inheritedVals.put("IO.in_int", null);
                }
                if (cls.inheritClass.getText().compareTo("String") == 0) {
                    inheritedVals.put("String.length", null);
                    inheritedVals.put("String.concat", null);
                    inheritedVals.put("String.substr", null);
                }
            } else {
                var parentClass = getParentNodeClass(cls);
                if (parentClass != null) {
                    inheritedVals = getFuncList(parentClass);
                }
            }


        for (Feature f: cls.features) {
            if (f instanceof FuncFeature) {
                currentVars.add((FuncFeature)f);
            }
        }


        LinkedHashMap<String, FuncFeature> finals = new LinkedHashMap<>();

        if (inheritedVals != null) {
            for (String v1 : inheritedVals.keySet()) {
                boolean exists = false;
                for (FuncFeature v2 : currentVars) {
                    var f1name = v1.split(Pattern.quote(".")).length == 2 ? v1.split(Pattern.quote("."))[1] : v1;
                    if (f1name.compareTo(v2.id.getToken().getText()) == 0) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    if (inheritedVals.get(v1) == null) {
                        finals.put(v1, null);
                    } else {
                        finals.put(v1,inheritedVals.get(v1));
                    }
                }
            }
        }

        for (FuncFeature f : currentVars) {
            finals.put(cls.id.getToken().getText() + "." + f.id.getToken().getText(), f);
        }

        return finals;
    }

    public String getDefaultType(String v) {
        if (v.compareTo("Int") == 0)
            return "int_const0";
        if (v.compareTo("String") == 0)
            return "str_const0";
        if (v.compareTo("Bool") == 0)
            return "bool_const0";

        return "0";
    }

    public void addPrototypeAndDispTab(int index, LinkedHashMap<String, VarFeature> varList, LinkedHashMap<String, FuncFeature> funcList, ClassNode cls) {
        // make object prototype
        ST varSeq = templates.getInstanceOf("sequence");
        ST protobj = templates.getInstanceOf("class_protobj");
        int currentSize = 3;

        for (String v : varList.keySet()) {
            varSeq.add("e", "\t.word   " + getDefaultType(((IdSymbol)(varList.get(v).id.getSymbol())).getType().getName()));
            currentSize += 1;
        }

        if (varSeq.render().length() == 0) {
            prototypeObjSection.add("e", protobj.add("index", index).add("name", cls.id.getToken().getText())
                    .add("size", currentSize));
        } else {
            prototypeObjSection.add("e", protobj.add("index", index).add("name", cls.id.getToken().getText())
                    .add("size", currentSize).add("seq", varSeq));
        }

        // make function dispatch table
        ST funcSeq = templates.getInstanceOf("sequence");
        ST dispTab = templates.getInstanceOf("class_disptab");

        for (String f : funcList.keySet()) {
            funcSeq.add("e", "\t.word   " + f);
        }

        if (funcSeq.render().length() == 0) {
            dispatchTableSection.add("e", dispTab.add("name", cls.id.getToken().getText()));
        } else {
            dispatchTableSection.add("e", dispTab.add("name", cls.id.getToken().getText())
                    .add("seq", funcSeq));
        }

    }

    public void addInitSection(ClassNode cls, ST seq) {
        ClassSymbol parent = ((ClassSymbol) cls.id.getSymbol()).getParentClass();
        if (seq.render().length() != 0) {
            initSection.add("e", templates.getInstanceOf("init")
                    .add("name", cls.id.getToken().getText())
                    .add("parent", parent.getName())
                    .add("seq", seq));
        } else {
            initSection.add("e", templates.getInstanceOf("init")
                    .add("name", cls.id.getToken().getText())
                    .add("parent", parent.getName()));
        }
    }

    public int getIndexOfVar(String var, LinkedHashMap<String, VarFeature> vlist) {
        int i = 0;

        for (String f : vlist.keySet()) {
            if (var.compareTo(f) == 0) {
                return i;
            }
            i++;
        }

        return -1;
    }

    public int getIndexOfFunc(String f, LinkedHashMap<String, FuncFeature> flist) {
        int i = 0;

        for (String k : flist.keySet()) {
            var lst = k.split(Pattern.quote("."));

            if (f.compareTo(lst[1]) == 0) {
                return i;
            }
            i++;
        }

        return -1;
    }

    @Override
    public ST visit(Program id) {
        prg = id;

        // init sections
        dataSection = templates.getInstanceOf("sequence");
        textSection = templates.getInstanceOf("sequence");
        initSection = templates.getInstanceOf("sequence");
        stringConstSection = templates.getInstanceOf("sequence");
        intConstSection = templates.getInstanceOf("sequence");
        classSection = templates.getInstanceOf("sequence");
        prototypeObjSection = templates.getInstanceOf("sequence");
        dispatchTableSection = templates.getInstanceOf("sequence");
        funcDeclSection = templates.getInstanceOf("sequence");

        // start data section
        dataSection.add("e", globalData());
        // add basic string and ints (same for all programs)
        stringAndIntBasicConsts();


        for (ClassNode cls : id.classes) {
            // add class name to data
            addString(cls.id.getToken().getText());
            if (!class_map.containsKey(cls.id.getToken().getText()))
                class_map.put(cls.id.getToken().getText(), class_map.size());

            // get list of all vars and functions (including inherited ones)
            LinkedHashMap<String, VarFeature> varList = getVarList(cls);
            LinkedHashMap<String, FuncFeature> funcList = getFuncList(cls);
            class_vars.put(cls.id.getToken().getText(), varList);
            class_funcs.put(cls.id.getToken().getText(), funcList);
            addPrototypeAndDispTab(class_map.get(cls.id.getToken().getText()), varList, funcList, cls);
        }

        for (ClassNode my_class : id.classes) {
            my_class.accept(this);
        }

        var programST = templates.getInstanceOf("program");

        // construct program section

        // add data const sections
        dataSection.add("e", stringConstSection);
        dataSection.add("e", intConstSection);
        dataSection.add("e", templates.getInstanceOf("boolconst"));

        // add data class section
        makeClassSection();
        dataSection.add("e", classSection);

        // data obj section and dispatchTableSection
        dataSection.add("e", prototypeObjSection);
        dataSection.add("e", dispatchTableSection);
        dataSection.add("e", "    .globl  heap_start\n" +
                "heap_start:\n" +
                "    .word   0");
        programST.add("data", dataSection);

        textSection.add("e", initSection);
        textSection.add("e", funcDeclSection);
        programST.add("textFuncs", textSection);

        return programST;
    }

    @Override
    public ST visit(ClassNode cls) {

        LinkedHashMap<String, VarFeature> varList = class_vars.get(cls.id.getToken().getText());
        LinkedHashMap<String, FuncFeature> funcList = class_funcs.get(cls.id.getToken().getText());

        currentClass = cls;
        for (Feature f : cls.features) {
            if (f instanceof FuncFeature) {
                funcDeclSection.add("e", f.accept(this));
            }
        }

        ST initAssign = templates.getInstanceOf("sequence");
        for (Feature f : cls.features) {
            if (f instanceof VarFeature) {
                ST curVar = f.accept(this);
                if (curVar != null) {
                    var index = new ArrayList<>(varList.keySet()).indexOf(((VarFeature) f).id.getToken().getText())
                            + 3;
                    initAssign.add("e", f.accept(this));
                    initAssign.add("e", templates.getInstanceOf("store_class_off")
                                            .add("offset", index * 4));
                }
            }
        }
        addInitSection(cls, initAssign);

        return null;
    }

    @Override
    public ST visit(FuncFeature id) {
        ST funcDef = templates.getInstanceOf("funcDef");
        String functionName = ((ClassSymbol)((FunctionSymbol)(id.id.getSymbol())).getParent()).getName() + "."
                + id.getToken().getText();
        if (id.paramList.size() == 0)
            return funcDef.add("name", functionName).add("expression", id.body.accept(this));
        return funcDef.add("name", functionName).add("expression", id.body.accept(this)).
                add("pfree",id.paramList.size() * 4);
    }

    @Override
    public ST visit(VarFeature id) {
        if (id.value != null)
            return id.value.accept(this);
        return null;
    }

    @Override
    public ST visit(FCall id) {
        addString(filename);
        dispatchNumber++;
        int savedDispatchNumber =dispatchNumber;

        // construct args
        ArrayList<ST> paramList = new ArrayList<>();
        for (Expression p : id.arglst) {
            paramList.add(p.accept(this));
        }
        Collections.reverse(paramList);
        ST paramSeq = templates.getInstanceOf("push_args").add("args", paramList);

        // construct call
        var line = id.id.getToken().getLine();
        var offset = getIndexOfFunc(id.id.getToken().getText(),
                        class_funcs.get(currentClass.id.getToken().getText()))* 4;
        ST call = templates.getInstanceOf("dispatch_on_self").add("index", savedDispatchNumber)
                .add("offset", offset).add("line_number", line).
                        add("file_string", string_consts.get(filename));

        if (paramList.size() == 0) {
            return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", call);
        } else {
            return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", "\tmove    $a0 $s0")
                    .add("e", call);
        }
    }

    @Override
    public ST visit(ComplexFCall id) {
        addString(filename);
        dispatchNumber++;
        int savedDispatchNumber = dispatchNumber;

        if ((id.caller.getToken().getText().compareTo("self") == 0) && (id.caller instanceof Id)) {
            // construct args
            ArrayList<ST> paramList = new ArrayList<>();
            for (Expression p : id.arglst) {
                paramList.add(p.accept(this));
            }
            Collections.reverse(paramList);
            ST paramSeq = templates.getInstanceOf("push_args").add("args", paramList);

            // construct call
            ST call = null;
            if (id.type == null) {
                var line = id.id.getToken().getLine();
                var offset = getIndexOfFunc(id.id.getToken().getText(),
                        class_funcs.get(currentClass.id.getToken().getText())) * 4;
                call = templates.getInstanceOf("dispatch_on_self").add("index", savedDispatchNumber)
                        .add("offset", offset).add("line_number", line).
                                add("file_string", string_consts.get(filename));
            } else {
                var line = id.id.getToken().getLine();
                var offset = getIndexOfFunc(id.id.getToken().getText(),
                        class_funcs.get(id.type.getText())) * 4;
                call = templates.getInstanceOf("dispatch_on_static").add("index", savedDispatchNumber)
                        .add("offset", offset).add("line_number", line)
                        .add("file_string", string_consts.get(filename))
                        .add("name", id.type.getText());
            }

            if (paramList.size() == 0) {
                return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", call);
            } else {
                return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", "\tmove    $a0 $s0")
                        .add("e", call);
            }
        }

        // construct args
        ArrayList<ST> paramList = new ArrayList<>();
        for (Expression p : id.arglst) {
            paramList.add(p.accept(this));
        }
        Collections.reverse(paramList);
        ST paramSeq = templates.getInstanceOf("push_args").add("args", paramList);

        var line = id.id.getToken().getLine();
        var callerType = id.caller.accept(res);
        var offset = -1;
        ST call = null;

        if (id.type == null) {
            if (callerType.getName().compareTo("SELF_TYPE") == 0) {
                offset = getIndexOfFunc(id.id.getToken().getText(),
                        class_funcs.get(currentClass.id.getToken().getText())) * 4;
            } else {
                offset = getIndexOfFunc(id.id.getToken().getText(),
                        class_funcs.get(callerType.getName())) * 4;
            }
            call = templates.getInstanceOf("dispatch_on_self").add("index", savedDispatchNumber)
                    .add("offset", offset).add("line_number", line)
                    .add("file_string", string_consts.get(filename));
        } else {
            offset = getIndexOfFunc(id.id.getToken().getText(),
                    class_funcs.get(id.type.getText())) * 4;
            call = templates.getInstanceOf("dispatch_on_static").add("index", savedDispatchNumber)
                    .add("offset", offset).add("line_number", line)
                    .add("file_string", string_consts.get(filename))
                    .add("name", id.type.getText());
        }


        if (id.caller instanceof Id) {

            ST loadObj;

            var currentScope = id.id.getScope();

            if (currentScope instanceof LetScope) {
                var localindex = (new ArrayList<>(((LetScope) currentScope).symbols.keySet()).indexOf
                        (id.caller.getToken().getText()) + 1) * -4;
                if (localindex != 0) {
                    loadObj = templates.getInstanceOf("load_var_of_let").add("offset", localindex);
                    return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", loadObj).add("e", call);

                }
            }

             if (currentScope instanceof FunctionSymbol) {
                var localindex = new ArrayList<>(((FunctionSymbol) currentScope).symbols.keySet()).
                        indexOf(id.caller.getToken().getText()) * 4 + 12;
                if (localindex != 8) {
                    loadObj = templates.getInstanceOf("load_local_var").add("offset", localindex);
                    return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", loadObj).add("e", call);
                }
            }

            var index = getIndexOfVar(id.caller.getToken().getText(), class_vars.get(currentClass.id.getToken().getText())) + 3;

            loadObj = templates.getInstanceOf("load_var_of_class").add("offset", index * 4);

            return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", loadObj).add("e", call);

        } else if ((id.caller instanceof ComplexFCall) || (id.caller instanceof FCall)) {

            return templates.getInstanceOf("sequence").add("e", id.caller.accept(this)).
                    add("e", "\tmove\t$s1 $a0").add("e", paramSeq).add("e","\tmove\t$a0 $s1").
                    add("e", call);

        } else if ((id.caller instanceof Bool) || (id.caller instanceof Int) || (id.caller instanceof StringNode)) {

            return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", id.caller.accept(this))
                    .add("e", call);

        } else {

            return templates.getInstanceOf("sequence").add("e", paramSeq).add("e", id.caller.accept(this))
                    .add("e", call);

        }

    }


    @Override
    public ST visit(Id id) {
        var currentScope = id.getScope();

        if (id.getToken().getText().compareTo("self") == 0) {
            return templates.getInstanceOf("sequence")
                    .add("e","\tmove\t$a0 $s0");
        }

        if (currentScope instanceof LetScope) {
            var localindex = (new ArrayList<>(((LetScope) currentScope).symbols.keySet()).indexOf
                    (id.getToken().getText()) + 1) * -4;
            if (localindex != 0) {
                return templates.getInstanceOf("load_var_of_let").add("offset", localindex);
            }
        }
        if (currentScope instanceof FunctionSymbol) {
            var localindex = new ArrayList<>(((FunctionSymbol) id.getScope()).symbols.keySet()).
                    indexOf(id.getToken().getText()) * 4 + 12;
            if (localindex != 8)
                return templates.getInstanceOf("load_local_var").add("offset", localindex);
        }
        var varsList = new ArrayList<>(class_vars.get(currentClass.id.getToken().getText()).keySet());
        var index = varsList.indexOf(id.getToken().getText()) + 3;
        if (index == 2) {
            if (id.getScope() instanceof FunctionSymbol) {
                var localindex = new ArrayList<>(((FunctionSymbol) id.getScope()).symbols.keySet()).
                        indexOf(id.getToken().getText()) * 4 + 12;
                return templates.getInstanceOf("load_local_var").add("offset", localindex);
            }
        }

        return templates.getInstanceOf("load_var_of_class").add("offset", index*4);
    }

    @Override
    public ST visit(Assignment id) {
        var currentScope = id.id.getScope();

        if (currentScope instanceof  LetScope) {
            var localindex = (new ArrayList<>(((LetScope) currentScope).symbols.keySet()).indexOf
                    (id.getToken().getText()) + 1) * -4;
            if (localindex != 0) {
                ST assSeq = templates.getInstanceOf("sequence").add("e", id.value.accept(this)).
                        add("e", templates.getInstanceOf("store_let_var_off").add("offset", localindex));
                return assSeq;
            }
        }

        if (currentScope instanceof FunctionSymbol) {
            var localindex = new ArrayList<>(((FunctionSymbol) id.id.getScope()).symbols.keySet()).
                    indexOf(id.id.getToken().getText()) * 4 + 12;

            if (localindex != 8) {
                ST assSeq = templates.getInstanceOf("sequence").add("e", id.value.accept(this)).
                        add("e", templates.getInstanceOf("store_local_off").add("offset", localindex));

                return assSeq;
            }
        }

        var classListVars = (new ArrayList<>(class_vars.get(currentClass.id.getToken().getText()).keySet()));
        var index = classListVars.indexOf(id.id.getToken().getText());

        var varOff = (index + 3) * 4;
        ST assSeq = templates.getInstanceOf("sequence").add("e", id.value.accept(this)).
                add("e", templates.getInstanceOf("store_class_off").add("offset", varOff));
        return assSeq;
    }

    @Override
    public ST visit(Int id) {
        addInt(id.getToken().getText());
        return templates.getInstanceOf("const").add("name", "int_const").add("index",
                int_consts.get(Integer.parseInt(id.getToken().getText())));
    }

    @Override
    public ST visit(StringNode id) {
        addString(escapeString(id.getToken().getText()));
        return templates.getInstanceOf("const").add("name", "str_const").add("index",
                string_consts.get(escapeString(id.getToken().getText())));

    }

    @Override
    public ST visit(Bool id) {
        if (id.getToken().getText().compareTo("true") == 0)
            return templates.getInstanceOf("const").add("name", "bool_const").add("index", 1);
        else
            return templates.getInstanceOf("const").add("name", "bool_const").add("index", 0);
    }

    @Override
    public ST visit(If id) {
        ifnumber++;
        return templates.getInstanceOf("ifRule")
                .add("index", ifnumber)
                .add("condition", id.cond.accept(this))
                .add("then", id.thenBranch.accept(this))
                .add("els", id.elseBranch.accept(this));
    }

    @Override
    public ST visit(Case id) {
        return null;
    }

    @Override
    public ST visit(Let id) {
        ST finalRetSeq = templates.getInstanceOf("sequence");
        int index = 1;

        for (LetDecl d : id.vars) {
            finalRetSeq.add("e", templates.getInstanceOf("store_let_var")
                    .add("seq", d.accept(this))
                    .add("offset", index * -4 ));
            index++;
        }

        return templates.getInstanceOf("let").add("decl", finalRetSeq)
                .add("body", id.body.accept(this)).add("free", id.vars.size() * 4);
    }

    @Override
    public ST visit(LetDecl id) {
        if (id.value == null) {
            return templates.getInstanceOf("sequence").add("e",
                    "\tla      $a0 " + getDefaultType(id.type.getText()));
        } else {
            return id.value.accept(this);
        }
    }

    @Override
    public ST visit(Instantiate id) {
        if (id.type.getText().compareTo("SELF_TYPE") == 0) {
            return templates.getInstanceOf("instantiate_self_type");
        }
        return templates.getInstanceOf("instantiate").add("name",id.type.getText());
    }

    @Override
    public ST visit(AddSub id) {
        if (id.op.getText().compareTo("+") == 0) {
            return templates.getInstanceOf("plus").add("e1", id.left.accept(this))
                    .add("e2", id.right.accept(this));
        }
        return templates.getInstanceOf("minus").add("e1", id.left.accept(this))
                .add("e2", id.right.accept(this));
    }

    @Override
    public ST visit(MulDiv id) {
        if (id.op.getText().compareTo("*") == 0) {
            return templates.getInstanceOf("mult").add("e1", id.left.accept(this))
                    .add("e2", id.right.accept(this));
        }
        return templates.getInstanceOf("div").add("e1", id.left.accept(this))
                .add("e2", id.right.accept(this));
    }

    @Override
    public ST visit(Neg id) {
        return templates.getInstanceOf("neg").add("e1", id.value.accept(this));
    }


    @Override
    public ST visit(Comparasion id) {
        if (id.op.getText().compareTo("=") == 0) {
            equalsNumber++;
            return templates.getInstanceOf("equals")
                    .add("e1", id.left.accept(this))
                    .add("e2", id.right.accept(this))
                    .add("index", equalsNumber);
        } else if (id.op.getText().compareTo("<") == 0) {
            compareNumber++;
            return templates.getInstanceOf("less")
                    .add("e1", id.left.accept(this))
                    .add("e2", id.right.accept(this))
                    .add("index", compareNumber);
        }
        compareNumber++;
        return templates.getInstanceOf("lesseq")
                .add("e1", id.left.accept(this))
                .add("e2", id.right.accept(this))
                .add("index", compareNumber);
    }

    @Override
    public ST visit(IsVoid id) {
        isvoidnumber++;
        return templates.getInstanceOf("isvoid")
                .add("val", id.void_expr.accept(this))
                .add("index", isvoidnumber);
    }

    @Override
    public ST visit(Not id) {
        notnumber++;
        return templates.getInstanceOf("not")
                .add("index", notnumber)
                .add("seq", id.value.accept(this));
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
        ST blockSequence = templates.getInstanceOf("sequence");

        for (Expression e : id.exprList) {
            blockSequence.add("e", e.accept(this));
        }

        return blockSequence;
    }

    @Override
    public ST visit(While id) {
        whileNumber++;
        return templates.getInstanceOf("while")
                .add("c", id.cond.accept(this))
                .add("b", id.block.accept(this))
                .add("index", whileNumber);
    }

}