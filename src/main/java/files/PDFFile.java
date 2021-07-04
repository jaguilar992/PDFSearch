package files;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class PDFFile {
  private static final Pattern pattern = Pattern.compile(".*\\.pdf");
  private PDDocument doc;
  private boolean ready = false;
  PDFTextStripper reader;

  public static ArrayList<File> SearchFiles(String dir) throws IOException {
    var filePaths = new ArrayList<File>();
    Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        var filename = file.toString();
        var isPDF = pattern.matcher(filename).find();
        if (isPDF) {
          filePaths.add(file.toFile());
        }
        return FileVisitResult.CONTINUE;
      }
    });
    return filePaths;
  }

  public PDFFile(File pdf) {
    try {
      this.doc = PDDocument.load(pdf);
      this.ready = true;

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String ReadPDFContent() throws IOException {
    if (this.ready) {
      reader = new PDFTextStripper();
      String content = reader.getText(this.doc)
          .replaceAll("\\s+", " ")
          .trim();
      this.doc.close();
      return content;
    }
    this.doc.close();
    return "";
  }

  public int getPageCount(){
    return this.doc.getNumberOfPages();
  }

  public void close () throws IOException {
    this.doc.close();
  }
}