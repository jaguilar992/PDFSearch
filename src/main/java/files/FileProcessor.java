package files;

import java.io.File;
import java.io.IOException;

public class FileProcessor extends Thread{
  private final Database db = new Database();
  PDFFile pdf;
  long fileSize;
  String path;
  int numPages;
  long id;

  public FileProcessor(File f, String dir){
    this.pdf = new PDFFile(f);
    this.fileSize = f.length();
    this.path = f.toString();
    this.numPages = pdf.getPageCount();
    this.id = db.createFileEntry(path, fileSize,numPages,dir);
  }

  @Override
  public void run() {
    try {
      this.processFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      this.pdf.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.db.close();
  }

  private void processFile() throws IOException {
    String content = this.pdf.ReadPDFContent();
    this.db.updateFileContent(this.path, content);
  }
}
