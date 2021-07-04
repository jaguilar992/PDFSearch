package gui;

import files.Directory;
import files.Database;
import files.FileProcessor;
import files.PDFFile;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private Button selectDirButton;
    @FXML private Button zipButton;
    @FXML private Button clearButton;
    @FXML private Button searchButton;
    @FXML private TextField txtSelectDir;
    @FXML private Label txtStatus;
    @FXML private TextField txtQuery;
    @FXML private ListView<String> resultList;
    @FXML private ProgressBar loadingBar;
    private ArrayList<File> initialResults;
    private final Database db = new Database();
    private ArrayList<File> results = new ArrayList<>();
    private String query = "";
    private String selected;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.zipButton.setDisable(true);
        this.clearButton.setDisable(true);
        this.searchButton.setDisable(true);
        this.txtQuery.setDisable(true);
        this.loadingBar.setVisible(false);
    }

    @FXML
    public void selectDir(){
        this.txtQuery.setText("");
        this.query = "";
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta");
        Stage stage = (Stage)selectDirButton.getScene().getWindow();
        File selectedDir= chooser.showDialog(stage);
        if (selectedDir != null) {
            this.selected = selectedDir.toString();
            txtSelectDir.setText(selectedDir.toString());
            try{
                var that = this;
                Task<Void> task = new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        that.processDir();
                        return null;
                    }
                };
                new Thread(task).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void clearQuery(){
        this.query = "";
        this.txtQuery.setText("");
        this.results = this.initialResults;
        this.printResults();
        this.zipButton.setDisable(true);
    }

    private void processDir() throws IOException {
        this.loadingBar.setVisible(true);
        this.txtSelectDir.setDisable(true);
        this.selectDirButton.setDisable(true);
        ArrayList<File> files = PDFFile.SearchFiles(this.selected);
        HashMap<String, Long> storedFiles = db.getFiles(this.selected);
        this.initialResults = files;
        this.results = files;
        final var total = files.size();
        // Analize
        File file;
        for(int i=0; i < files.size(); i++){
            file = files.get(i);
            String path = file.toString();
            if (!storedFiles.containsKey(path) ||
                storedFiles.get(path) != file.length()
            ) {
                System.out.println(file);
                final var pdf = new PDFFile(file);
                long fileSize = file.length();
                int numPages = pdf.getPageCount();
                this.db.createFileEntry(path, fileSize,numPages,this.selected);
                final String content = pdf.ReadPDFContent();
                this.db.updateFileContent(path, content);
            }
            final var progress = ((float)(i+1) / (total));
            this.loadingBar.setProgress(progress);
        }


        /// Last steps
        this.clearButton.setDisable(false);
        this.searchButton.setDisable(false);
        this.txtQuery.setDisable(false);
        this.txtSelectDir.setDisable(false);
        this.selectDirButton.setDisable(false);
        this.loadingBar.setVisible(false);
        this.printResults();
    }

    private void printResults() {
//        this.txtStatus.setText("Listo");
        this.resultList.getItems().clear();
        for(var result : this.results){
            this.resultList.getItems().add(result.toString());
        }
    }

    @FXML
    public void search() {
        this.query = this.txtQuery.getText();
        this.results = db.search("%"+this.query+"%", this.selected);
        this.printResults();
        if (!query.equals("")){
            this.zipButton.setDisable(false);
//            this.txtStatus.setText("Búsqueda completada");
        } else {
            this.zipButton.setDisable(true);
        }
    }

    @FXML
    public void saveZipFile() throws IOException {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zip files (*.zip)", "*.zip");
        chooser.getExtensionFilters().add(extFilter);
        Stage stage = (Stage)zipButton.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            Directory.createZipFile(file.toString(), this.results);
//            this.txtStatus.setText("Archivo guardado");
        }
    }
}
