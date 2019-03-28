package app;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageProcessingJob extends Task<Void> {
    File file;
    File directory;
    SimpleStringProperty status;
    DoubleProperty progress;
    SimpleStringProperty time;
    double time_tot=0;

    public File getFile() {
        return file;
    }
    public SimpleStringProperty getTime(){
        return time;
    }

    @Override protected Void call(){
        long start = System.currentTimeMillis();
        try {
            updateMessage("converting ...");
            BufferedImage original = ImageIO.read(file);
            BufferedImage grayscale = new BufferedImage(
                    original.getWidth(), original.getHeight(), original.getType());
            for (int i = 0; i < original.getWidth(); i++) {
                for (int j = 0; j < original.getHeight(); j++) {
                    int red = new Color(original.getRGB(i, j)).getRed();
                    int green = new Color(original.getRGB(i, j)).getGreen();
                    int blue = new Color(original.getRGB(i, j)).getBlue();
                    int luminosity = (int) (0.21*red + 0.71*green + 0.07*blue);
                    int newPixel =
                            new Color(luminosity, luminosity, luminosity).getRGB();
                    grayscale.setRGB(i, j, newPixel);
                }
                double progress = (1.0 + i) / original.getWidth();
                Platform.runLater(() -> updateProgress(progress,1));
            }
            Path outputPath =
                    Paths.get(directory.getAbsolutePath(), file.getName());

            ImageIO.write(grayscale, "jpg", outputPath.toFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        long end = System.currentTimeMillis();
        long duration = end-start;
        updateMessage("finished!");
        this.time.set(duration+" ms");
        time_tot = duration;
        return null;
    };

    public ImageProcessingJob(File file){
        this.file = file;
        this.directory = null;
        updateMessage("waiting ...");
        this.time = new SimpleStringProperty();
        this.time.set("...");
    }
}