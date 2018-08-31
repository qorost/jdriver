package cn.edu.nudt.secant.jdriver.knowledgebase;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * depreciated, generated automatically, here for test, ignore this
 * Created by huang on 7/19/18.
 */
public class StandardLibraryHelper {

    public static String get_String(String input) {
        return input;
    }


    public static byte[] readBytes(String filename) {
        Path path = Paths.get(filename);
        try {
            byte[] data = Files.readAllBytes(path);
            return data;
            //main(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] get_Byte_Array_frome_file(String input) {
        return readBytes(input);
    }

    //java.util.Map

    public static InputStream get_InputStream(String arg) {
        return null;
    }



    public static OutputStream get_OutputStream(String input) {
        return null;
    }

    public static ByteOrder get_ByteOrder() {
        return null;
    }

    public static File get_File(String input) {
        return new File(input);
    }


//    org.apache.commons.imaging.common.ImageMetadata$ImageMetadataItem : 1
//    org.apache.commons.imaging.icc.IccTag[] : 1
//    org.apache.commons.imaging.formats.ico.IcoImageParser$IconData[] : 1
//    java.io.ByteArrayInputStream : 1
//    java.util.Calendar : 1
//    java.io.RandomAccessFile : 1
//    long[] : 1
//    org.apache.commons.imaging.common.mylzw.MyLzwDecompressor$Listener : 1
//    org.apache.commons.imaging.common.itu_t4.HuffmanTree$1 : 1
//    java.util.Date : 1
//    org.apache.commons.imaging.palette.MedianCut : 1
//    org.apache.commons.imaging.common.itu_t4.T4_T6_Tables$Entry[] : 1
//    org.apache.commons.imaging.formats.xbm.XbmImageParser$1 : 1
//    org.apache.commons.imaging.formats.jpeg.JpegUtils$Visitor : 1
//    org.apache.commons.imaging.common.bytesource.ByteSourceInputStream$1 : 1
//    org.apache.commons.imaging.common.mylzw.MyLzwCompressor$Listener : 1
//    org.apache.commons.imaging.formats.jpeg.xmp.JpegRewriter$SegmentFilter : 2
//    org.apache.commons.imaging.formats.pnm.PamFileInfo$1 : 2
//    org.apache.commons.imaging.formats.icns.IcnsImageParser$IcnsElement[] : 2
//    char[] : 2
//    org.apache.commons.imaging.formats.xpm.XpmImageParser$1 : 2
//    java.awt.image.ColorModel : 2
//    java.awt.Rectangle : 3
//    java.lang.StringBuilder : 3
//    org.apache.commons.imaging.formats.jpeg.decoder.Block[] : 3
//    org.apache.commons.imaging.formats.tiff.TiffElement$DataElement[] : 3
//    org.apache.commons.imaging.formats.png.ChunkType[] : 3
//    boolean[] : 3
//    org.apache.commons.imaging.formats.tiff.TiffReader$Listener : 4
//    double[] : 4
//    java.lang.String[] : 4
//    java.awt.color.ICC_Profile : 7
//    int[][][] : 9
//    java.lang.Throwable : 10
//    org.apache.commons.imaging.common.RationalNumber[] : 11
//    java.awt.color.ColorSpace : 12
//    float[] : 12
//    short[] : 14
//    java.io.PrintWriter : 42
//    java.io.File : 45
//    java.lang.Object : 52
//    java.util.List : 62
//    java.awt.image.BufferedImage : 68
//    	int[] : 82
    //java.awt.image.BufferedImage



}
