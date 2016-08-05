
package com.pl.webplib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class IOUtil {
    public IOUtil() {
    }

    public static void delete(File file) {
        file.delete();
    }

    public static void delete(String file) {
        (new File(file)).delete();
    }

    public static void mv(String source, String target) {
        try {
            Runtime.getRuntime().exec(String.format("mv %s %s", new Object[]{source, target}));
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public static String readString(String file) throws IOException {
        return readString(new File(file));
    }

    public static String readString(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        String str = readString((InputStream)in);
        in.close();
        return str;
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean c = false;

        int c1;
        while((c1 = in.read(buf)) > 0) {
            out.write(buf, 0, c1);
        }

        byte[] bytes = out.toByteArray();
        out.close();
        return bytes;
    }

    public static byte[] readBytes(String path) throws IOException {
        FileInputStream in = new FileInputStream(path);
        byte[] bytes = readBytes((InputStream)in);
        in.close();
        return bytes;
    }

    public static String readString(InputStream in) throws IOException {
        byte[] bytes = readBytes(in);
        return new String(bytes, "UTF-8");
    }

    public static void writeString(OutputStream out, String str) throws IOException {
        out.write(str.getBytes());
    }

    public static void appendString(OutputStream out, String str) throws IOException {
        out.write(str.getBytes());
    }

    public static void appendString(File file, String str) throws IOException {
        FileOutputStream out = new FileOutputStream(file, true);
        out.write(str.getBytes());
        out.close();
    }

    public static void appendString(String file, String str) throws IOException {
        appendString(new File(file), str);
    }

    public static void writeString(File file, String str) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        out.write(str.getBytes());
        out.close();
    }

    public static void writeUTF8String(File file, String str) throws IOException {
        OutputStreamWriter outw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        outw.write(str);
        outw.close();
    }

    public static void writeUTF8String(String file, String str) throws IOException {
        writeUTF8String(new File(file), str);
    }

    public static void writeString(String file, String str) throws IOException {
        writeString(new File(file), str);
    }

    public static boolean copy(InputStream in, String target) throws IOException {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(new File(target));
            byte[] buf = new byte[10240];
            boolean c = false;

            int c1;
            while((c1 = in.read(buf)) > 0) {
                out.write(buf, 0, c1);
            }
        } finally {
            if(out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException var10) {
                    var10.printStackTrace();
                }
            }

        }

        return true;
    }

    public static boolean copy(String source, String target) {
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(new File(source));
            out = new FileOutputStream(new File(target));
            byte[] e = new byte[1024];
            boolean c = false;

            int c1;
            while((c1 = in.read(e)) > 0) {
                out.write(e, 0, c1);
            }

            return true;
        } catch (FileNotFoundException var21) {
            var21.printStackTrace();
            return false;
        } catch (IOException var22) {
            var22.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException var20) {
                    var20.printStackTrace();
                }
            }

            if(out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException var19) {
                    var19.printStackTrace();
                }
            }

        }

        return false;
    }

    public static boolean copyWithFileLock(String source, String target) {
        FileInputStream in = null;
        FileOutputStream out = null;
        FileChannel fileChannel = null;
        File targetFile=new File(target);
        FileLock fileLock = null;
        boolean hasBeenLocked = false;

        try {
            in = new FileInputStream(new File(source));
            out = new FileOutputStream(targetFile,true);
            //用源文件做锁，不然目标文件会被置空
            fileChannel=out.getChannel();
            fileLock=fileChannel.tryLock();
            if (fileLock==null){
                hasBeenLocked=true;
                fileLock=fileChannel.lock();
            }

            //由于是复制，所以复制一次就够了，等其他地方复制完毕，就返回
            if (hasBeenLocked){
                return true;
            }

            out = new FileOutputStream(targetFile);
            byte[] e = new byte[1024];
            boolean c = false;

            int c1;
            while((c1 = in.read(e)) > 0) {
                out.write(e, 0, c1);
            }

            out.flush();
            return true;
        } catch (FileNotFoundException var21) {
            var21.printStackTrace();
            return false;
        } catch (IOException var22) {
            var22.printStackTrace();
        } finally {
            if (fileLock!=null){
                try {
                    fileLock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(in != null) {
                try {
                    in.close();
                } catch (IOException var20) {
                    var20.printStackTrace();
                }
            }
            if(out != null) {
                try {
                    out.close();
                } catch (IOException var19) {
                    var19.printStackTrace();
                }
            }


        }
        return false;
    }
    public static void serialize(Serializable obj, String file) throws FileNotFoundException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));

        try {
            oos.writeObject(obj);
        } catch (IOException var11) {
            var11.printStackTrace();
            throw var11;
        } finally {
            try {
                oos.close();
            } catch (IOException var10) {
                var10.printStackTrace();
            }

        }

    }

    public static Object unserialize(String file) throws Exception {
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            Object var4 = ois.readObject();
            return var4;
        } catch (Exception var12) {
            ;
        } finally {
            try {
                if(ois != null) {
                    ois.close();
                }
            } catch (IOException var11) {
                var11.printStackTrace();
            }

        }

        return null;
    }
}
