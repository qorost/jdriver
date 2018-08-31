package cn.edu.nudt.secant.jdriver.knowledgebase;

import org.objectweb.asm.Type;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by huang on 7/18/18.
 */
public class StandardLibraryClasses {

    public HashMap<String, HelperMethod> helperMethodItemHashMap = new HashMap<>();

    public static String STRING_HELPER_BUILDER = "return arg0;";

    public static String FILE_HELPER_BUILDER = "return new File(arg0);";
    public static String INPUTSTREAM_HELPER_BUILDER = "return (InputStream) (new FileInputStream(arg0));";
    public static String FILEINPUTSTREAM_HELPER_BUILDER = "return new FileInputStream(arg0);";


    public static String OUTPUTSTREAM_HELPER_BUILDER = "return (OutputStream) (new FileOutputStream(arg0));";
    public static String FILEOUTPUTSTREAM_HELPER_BUILDER = "return new FileOutputStream(arg0);";

    //java.io.PrintWriter
    public static String PRINTWRITER_HELPER_BUILDER = "return new PrintWriter(new OutputStreamWriter(System.out, Charset.defaultCharset()));";

    //java.awt.image.BufferedImage
    public static String BUFFEREDIMAGE_HELPER_BUILDER = "return new BufferedImage(arg0, arg1, arg2);";

    //java.awt.color.ColorSpace
    public static String COLORSPACE_HELPER_BUILDER = "BufferedImage src = new BufferedImage(arg0, arg1, arg2);\n" +
            "\t\treturn src.getColorModel().getColorSpace();";

    //java.awt.image.ColorModel

    public static String DIRECTCOLORMODEL_HELPER_BUILDER = "return new DirectColorModel(32, 0x00ff0000, 0x0000ff00,0x000000ff, 0xff000000);";
    public static String COLORMODEL_HELPER_BUILDER = "return (ColorModel)(new DirectColorModel(32, 0x00ff0000, 0x0000ff00,0x000000ff, 0xff000000));";


    //java.awt.color.ICC_Profile
    public static String ICC_Profile_HELPER_BUILDER = "return ICC_Profile.getInstance(arg0);";


    //java.awt.Rectangle
    //fixme, arg2, arg3, > 0
    public static String RECTANGLE_HELPER_BUILDER = "return new Rectangle(arg0, arg1,arg2, arg3);";


    //java.io.RandomAccessFile
    public static String RANDOMACCESSFILE_HELPER_BUILDER = "return new RandomAccessFile(new File(arg0), \"r\");";

    //java.io.ByteArrayInputStream
    public static String BYTEARRAYINPUTSTREAM_HELPER_BUILDER = "byte[] src = Helper.readBytes(arg0);\n" +
            "\t\treturn new ByteArrayInputStream(src);";



    //java.nio.ByteOrder
    public static String BYTEORDER_HELPER_BUILDER = "return ByteOrder.BIG_ENDIAN;";
    public static String BYTEORDERLITTLE_HELPER_BUILDER = "return ByteOrder.LITTLE_ENDIAN;";

    //java.util.Date
    public static String DATE_HELPER_BUILDER= "return new Date();";

    //java.util.List fixme, update for variaous data type
    public static String LIST_HELPER_BUILDER = "return new ArrayList<>();";
    //java.util.Calendar
    public static String CALENDAR_HELPER_BUILDER = "return Calendar.getInstance();";
    //java.util.Map
    public static String MAP_HELPER_BUILDER = "return new HashMap<String, Object>();";
    //java.lang.Object
    public static String OBJECT_HELPER_BUILDER = "return null;";
    //java.lang.StringBuilder
    public static String STRINGBUILDER_HELPER_BUILDER = "return new StringBuilder();";

    //java.lang.Throwable





    public static HelperMethod.StandardHelperMethod[] methodItems= {

            /**
             * java.lang
            */
            new HelperMethod.StandardHelperMethod("java.lang.String",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    STRING_HELPER_BUILDER),

            /**
             * java.io
            */
            new HelperMethod.StandardHelperMethod("java.io.File",
                    "(Ljava/lang/String;)Ljava/io/File;",
                    true,
                    FILE_HELPER_BUILDER),
            new HelperMethod.StandardHelperMethod("java.io.InputStream",
                    "(Ljava/lang/String;)Ljava/io/InputStream;",
                    true, INPUTSTREAM_HELPER_BUILDER),

            new HelperMethod.StandardHelperMethod("java.io.FileInputStream",
                    "(Ljava/lang/String;)Ljava/io/FileInputStream;",
                    true, FILEINPUTSTREAM_HELPER_BUILDER),

            new HelperMethod.StandardHelperMethod("java.io.OutputStream",
                    "(Ljava/lang/String;)Ljava/io/OutputStream;",
                    true, OUTPUTSTREAM_HELPER_BUILDER),

            new HelperMethod.StandardHelperMethod("java.io.FileOutputStream",
                    "(Ljava/lang/String;)Ljava/io/FileOutputStream;",
                    true, FILEOUTPUTSTREAM_HELPER_BUILDER),

            new HelperMethod.StandardHelperMethod("java.io.PrintWriter",
                    "()Ljava/io/PrintWriter;",
                    false,
                    PRINTWRITER_HELPER_BUILDER),


            //java.awt.image.BufferedImage
            new HelperMethod.StandardHelperMethod("java.awt.image.BufferedImage",
                    "(III)Ljava/awt/image/BufferedImage;",
                    false,
                    BUFFEREDIMAGE_HELPER_BUILDER),


            //java.awt.color.ColorSpace
            new HelperMethod.StandardHelperMethod("java.io.ColorSpace",
                    "(III)Ljava/awt/color/ColorSpace;",
                    false,
                    COLORSPACE_HELPER_BUILDER),

            //java.awt.image.ColorModel
            new HelperMethod.StandardHelperMethod("java.awt.image.ColorModel",
                    "()Ljava/awt/image/ColorModel;",
                    false,
                    COLORMODEL_HELPER_BUILDER),

            //fixme, update for input rebuld
            //java.awt.image.DirectColorModel
            new HelperMethod.StandardHelperMethod("java.awt.image.DirectColorModel",
                    "()Ljava/awt/image/DirectColorModel;",
                    false,
                    DIRECTCOLORMODEL_HELPER_BUILDER),

            //java.awt.color.ICC_Profile
            new HelperMethod.StandardHelperMethod("java.awt.color.ICC_Profile",
                    "(I)Ljava/awt/color/ICC_Profile;",
                    false,
                    ICC_Profile_HELPER_BUILDER),

            //java.awt.Rectangle
            new HelperMethod.StandardHelperMethod("java.awt.Rectangle",
                    "(IIII)Ljava/awt/Rectangle;",
                    false,
                    RECTANGLE_HELPER_BUILDER),



            //java.io.RandomAccessFile
            new HelperMethod.StandardHelperMethod("java.io.RandomAccessFile",
                    "(Ljava/lang/String;)Ljava/io/RandomAccessFile;",
                    true,
                    RANDOMACCESSFILE_HELPER_BUILDER),
//            java.io.ByteArrayInputStream

            new HelperMethod.StandardHelperMethod("java.io.ByteArrayInputStream",
                    "(Ljava/lang/String;)Ljava/io/ByteArrayInputStream;",
                    true,
                    BYTEARRAYINPUTSTREAM_HELPER_BUILDER),
//            java.nio.ByteOrder
            new HelperMethod.StandardHelperMethod("java.nio.ByteOrder",
                    "()Ljava/nio/ByteOrder;",
                    false,
                    BYTEORDER_HELPER_BUILDER),



//            java.util.Date
            new HelperMethod.StandardHelperMethod("java.util.Date",
                    "()Ljava/util/Date;",
                    false,
                    DATE_HELPER_BUILDER),
            //java.util.List
            new HelperMethod.StandardHelperMethod("java.util.List",
                    "()Ljava/util/List;",
                    false,
                    LIST_HELPER_BUILDER),

            //java.util.Calendar
            new HelperMethod.StandardHelperMethod("java.util.Calendar",
                    "()Ljava/util/Calendar;",
                    false,
                    CALENDAR_HELPER_BUILDER),

            //java.util.Map
            new HelperMethod.StandardHelperMethod("java.util.Map",
                    "()Ljava/util/Map;",
                    false,
                    MAP_HELPER_BUILDER),
//            java.lang.Object
            new HelperMethod.StandardHelperMethod("java.lang.Object",
                    "()Ljava/lang/Object;",
                    false,
                    OBJECT_HELPER_BUILDER),

//            java.lang.StringBuilder
            new HelperMethod.StandardHelperMethod("java.lang.StringBuilder",
                    "()Ljava/lang/StringBuilder;",
                    false,
                    STRINGBUILDER_HELPER_BUILDER),

//            java.lang.Throwable



    };

    public static HelperMethod.StandardHelperMethod getHelper(Type t) {
        //t.getClassName returns like this: "java.lang.String;"
        for(HelperMethod.StandardHelperMethod m: methodItems) {
            String inputTypeName = t.getClassName();
            if(m.classname.contains(inputTypeName)) {
                //fixme, allow duplicate
                return m;
            }
        }
        return null;
    }


}
