package files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Directory {

  public static ArrayList<File> indexDirectory(String dir) throws IOException {
    var files = PDFFile.SearchFiles(dir);
    var db = new Database();
    var storedFiles = db.getFiles(dir);
    db.close();
    for (File file : files) {
      String path = file.toString();
      FileProcessor p;
      if (!storedFiles.containsKey(path) ||
          storedFiles.get(path) != file.length()
      ) {
        p = new FileProcessor(file, dir);
        p.start();
      }
//      System.out.println(file);
    }
    return files;
  }

  public static void createZipFile(String zipFilePath, ArrayList<File> files) throws IOException {
//    String zipFileFullName = zipFilePath.concat(zipFileName + ".zip");
    FileOutputStream fos = new FileOutputStream(zipFilePath);
    ZipOutputStream zos = new ZipOutputStream(fos);
    for (File file :
        files) {
      zos.putNextEntry(new ZipEntry(Math.random()+file.getName()));
      byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
      zos.write(bytes, 0, bytes.length);
      zos.closeEntry();
    }
    zos.close();
  }
}
