package org.puredata.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IoUtils {

    private interface FileProcessor {
        void processFile(File file);
    }

    /* renamed from: org.puredata.core.utils.IoUtils$1 */
    class C00931 implements FileProcessor {
        private final /* synthetic */ List val$hits;
        private final /* synthetic */ Pattern val$p;

        C00931(Pattern pattern, List list) {
            this.val$p = pattern;
            this.val$hits = list;
        }

        public void processFile(File file) {
            if (this.val$p.matcher(file.getName()).matches()) {
                this.val$hits.add(file);
            }
        }
    }

    public static File extractResource(InputStream in, String filename, File directory) throws IOException {
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();
        File file = new File(directory, filename);
        FileOutputStream out = new FileOutputStream(file);
        out.write(buffer);
        out.close();
        return file;
    }

    public static List<File> extractZipResource(InputStream in, File directory) throws IOException {
        return extractZipResource(in, directory, false);
    }

    public static List<File> extractZipResource(InputStream in, File directory, boolean overwrite) throws IOException {
        byte[] buffer = new byte[2048];
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in, 2048));
        List<File> files = new ArrayList();
        directory.mkdirs();
        while (true) {
            ZipEntry entry = zin.getNextEntry();
            if (entry == null) {
                zin.close();
                return files;
            }
            File file = new File(directory, entry.getName());
            files.add(file);
            if (overwrite || !file.exists()) {
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), 2048);
                    while (true) {
                        int nRead = zin.read(buffer, 0, 2048);
                        if (nRead <= 0) {
                            break;
                        }
                        bos.write(buffer, 0, nRead);
                    }
                    bos.flush();
                    bos.close();
                }
            }
        }
    }

    public static List<File> find(File dir, String pattern) {
        List<File> hits = new ArrayList();
        traverseTree(dir, new C00931(Pattern.compile(pattern), hits));
        return hits;
    }

    private static void traverseTree(File file, FileProcessor fp) {
        fp.processFile(file);
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                traverseTree(child, fp);
            }
        }
    }
}
