package app;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.ResourceBundle;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import javafx.application.Platform;

public class Controller implements Initializable {
    ObservableList<ImageProcessingJob> jobs;
    File directory;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    @FXML private Button selectButton;
    @FXML private Button selectDirectory;
    @FXML private Label directoryLabel;
    @FXML private Label timeLabel;
    @FXML private Label sumLabel;
    @FXML private ChoiceBox<String> choiceBox;
    @FXML TableView<ImageProcessingJob> tableView;
    @FXML TableColumn<ImageProcessingJob, String> imageCol;
    @FXML TableColumn<ImageProcessingJob, Double> progressCol;
    @FXML TableColumn<ImageProcessingJob, String> statusCol;
    @FXML TableColumn<ImageProcessingJob, String> timeCol;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choiceBox.getItems().addAll("sequential", "2 threads", "4 threads", "8 threads", "16 threads");
        choiceBox.setValue("sequential");
        imageCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getFile().getName()));
        statusCol.setCellValueFactory(p -> p.getValue().messageProperty());
        progressCol.setCellFactory(ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
        progressCol.setCellValueFactory(p -> p.getValue().progressProperty().asObject());
        timeCol.setCellValueFactory(p -> p.getValue().getTime());
        jobs = FXCollections.observableList(new ArrayList<>());
        tableView.setItems(jobs);
    }


    public void selectFiles(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        jobs.clear();
        selectedFiles.stream().forEach(i->jobs.add(new ImageProcessingJob(i)));
    }

    public void selectDirectory(){
        DirectoryChooser dirChooser = new DirectoryChooser();
        directory = dirChooser.showDialog(null);
        directoryLabel.setText(directory.getAbsolutePath());
    }

    @FXML
    void processFiles() {
        Thread t = new Thread(this::backgroundJob);
        t.start();
    }


    private void backgroundJob(){
        reloadExecutor();
        long start = System.currentTimeMillis();
        jobs.stream().forEach(job -> {
            job.directory = directory;
            executor.submit(job);
        });
        executor.shutdown();
        while(!executor.isTerminated());
        long end = System.currentTimeMillis();
        long duration = end-start;
        Platform.runLater(() -> timeLabel.setText("execution time: "+duration+"ms"));
        long total = 0;
        for(ImageProcessingJob x: jobs){
            total+=x.time_tot;
        }
        long tx = total;
        Platform.runLater(() -> sumLabel.setText("summary time: "+tx+"ms"));

    }

    private void reloadExecutor(){
        String value = choiceBox.getValue();
        if(value != "sequential") {
            switch (value) {
                case "16 threads":
                    executor = new ForkJoinPool(16);
                    break;
                case "2 threads":
                    executor = new ForkJoinPool(2);
                    break;
                case "4 threads":
                    executor = new ForkJoinPool(4);
                    break;
                case "8 threads":
                    executor = new ForkJoinPool(8);
                    break;
            }
        }
    }
}