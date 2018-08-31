package cn.edu.nudt.secant.jdriver.assembler;

import cn.edu.nudt.secant.jdriver.tools.Options;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static cn.edu.nudt.secant.jdriver.assembler.statement.StatementBuilder.BYTEBUFFER_METHODS;
import static cn.edu.nudt.secant.jdriver.assembler.statement.StatementBuilder.PRIMITIVE_TYPE_NAME;
import static cn.edu.nudt.secant.jdriver.assembler.value.ValueBuilder.PRMITIVE_BYTE_SIZE;

/**
 * This is defined to write a Helper Class
 * Created by huang on 6/27/18.
 */
public class HelperCode {


    //ZBCIJFDS
    public static final String[] DESC = {
            "V",
            "Z",
            "B",
            "C",
            "S",
            "I",
            "J",
            "F",
            "D"
    };


    public static String makeDesc(Type t) {
        int sort = t.getSort();
        if(sort <= Type.DOUBLE) {
            return DESC[sort];
        } else if(sort == Type.ARRAY) {
            Type et = t.getElementType();
            return "[" + makeDesc(et);
        } else if(sort == Type.OBJECT) {
            return "L" + t.getClassName().replace(".", "/") + ";";
        }
        return null;
    }

    public static final String[] IMPORTS = {
            "import java.util.Arrays;",
            "import java.nio.ByteBuffer;",
            "import java.nio.file.Files;",
            "import java.nio.file.Paths;",
            "import java.nio.file.Path;",
            "import java.lang.reflect.Method;"
    };

    public static String REFLCTIONHELPER = "";

    public static String DATAHELPER = "    public static byte[] readBytes(String filename) {\n" +
            "        Path path = Paths.get(filename);\n" +
            "        try {\n" +
            "            byte[] data = Files.readAllBytes(path);\n" +
            "            return data;\n" +
            "            //main(data);\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "            return null;\n" +
            "        }\n" +
            "    }\n\n";

    public static String ARGHELPER = "    public static void processargs(String[] args, Method[] methods) {\n" +
            "        if(args.length > 2) {\n" +
            "            int i = Integer.valueOf(args[0]);\n" +
            "            if(i < 0)\n" +
            "                return;\n" +
            "\n" +
            "            Path path = Paths.get(args[1]);\n" +
            "            try {\n" +
            "                byte[] data = Files.readAllBytes(path);\n" +
            "                if(i < methods.length) {\n" +
            "                    Method m = methods[i];\n" +
            "                    m.invoke(data);\n" +
            "                }\n" +
            "            } catch (Exception e) {\n" +
            "                e.printStackTrace();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "\n";

    /**
     * Make methods to split two array
     */
    public static String BYTEARRAYHELPER =
                    "    public static byte[] splitFields(byte[] data, int end) {\n" +
                    "        if(end < data.length)\n" +
                    "            return Arrays.copyOfRange(data, 0, end);\n" +
                    "        else\n" +
                    "            return null;\n" +
                    "    }\n" +
                    "\n" +
                    "    public static byte[] splitInput(byte[] data, int start, int end) {\n" +
                    "        if(start < data.length && end <= data.length && end > start)\n" +
                    "            return Arrays.copyOfRange(data, start, end);\n" +
                    "        else\n" +
                    "            return null;\n" +
                    "    }\n";


    public static String HELPERMETHODS = ARGHELPER + "\n" + BYTEARRAYHELPER;


    public static String GETVALUEHELPER = "";
    /**
     * make such kinds of methods:
     * public static int getInt(byte[] data, int start)
     */
    public static void makeGetMethods() {

        for(int i=1; i< Type.ARRAY; i++) {
            String typeName = PRIMITIVE_TYPE_NAME[i];
            int typeSize = PRMITIVE_BYTE_SIZE[i];
            String typeMethod = BYTEBUFFER_METHODS[i];
            String tmp = String.format("    public static %s get_%s(byte[] data, int start) {\n" +
                    "        int size = %d;\n" +
                    "        byte[] tmp = Arrays.copyOfRange(data, start, start + size);\n" +
                    "        return %s;\n" +
                    "    }", typeName, typeName, typeSize, typeMethod);
            GETVALUEHELPER += tmp;
            GETVALUEHELPER += "\n\n";
        }
    }

//    public static int getInt(byte[] data, int start) {
//        int size = 1;
//        byte[] tmp = Arrays.copyOfRange(data, start, start + size);
//        if(tmp != null) return 1;
//    }

    public static boolean build() {
        try {
            makeGetMethods();

            String output = Options.v().getOutput();
            if(!output.endsWith("/"))
                output += "/";
            String filename = output + "Helper" + ".java";

            if (Files.exists(Paths.get(filename))) {
                System.out.println("\tMaking Helper.java: Helper file already existed, skip building Helper.");
            } else {
                System.out.println("\tMaking Helper.java: Making helper class to " + filename);

                File helperfile = new File(filename);
                helperfile.getParentFile().mkdirs();

                PrintWriter helperwriter = new PrintWriter(helperfile, "UTF-8");

                for (String i : IMPORTS)
                    helperwriter.write(i + "\n");
                helperwriter.write("\npublic class Helper{\n\n");

                helperwriter.write(DATAHELPER);//readbytes
                helperwriter.write(HELPERMETHODS);
                helperwriter.write(GETVALUEHELPER);

                helperwriter.write("\n}");
                helperwriter.close();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }



}
