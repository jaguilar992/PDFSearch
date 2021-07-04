module pdfsearch {
  requires javafx.controls;
  requires javafx.fxml;
  requires org.apache.pdfbox;
  requires java.sql;
  opens gui to javafx.fxml;
  exports gui;
}