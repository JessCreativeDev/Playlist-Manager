/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 *
 * @author Thema
 */
public final class VideoInterface extends BorderPane{
    
    // The icons used for UI elements
    private ImageView playIcon;
    private ImageView pauseIcon;
    private ImageView rewindIcon;
    private ImageView fastForwardIcon;
    private ImageView muteIcon;
    private ImageView lowVolumeIcon;
    private ImageView mediumVolumeIcon;
    private ImageView highVolumeIcon;
    
    // The UI elements
    private BorderPane videoLayout;
    private MediaPlayer currentVideo;
    private MediaView videoPlayer;
    private Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Slider volumeSlider;
    private Button playButton;
    private HBox mediaBar;
    
    public VideoInterface(){
        
        // Loads the icons in the resource folder into the Image objects
        playIcon = new ImageView(new Image(getClass().getResourceAsStream("/playIcon.png"), 23, 23, false, false));
        pauseIcon = new ImageView(new Image(getClass().getResourceAsStream("/pauseIcon.png"), 23, 23, false, false));
        rewindIcon = new ImageView(new Image(getClass().getResourceAsStream("/rewindIcon.png"), 23, 23, false, false));
        fastForwardIcon = new ImageView(new Image(getClass().getResourceAsStream("/fastForwardIcon.png"), 23, 23, false, false));
        muteIcon = new ImageView(new Image(getClass().getResourceAsStream("/muteIcon.png"), 23, 23, false, false));
        lowVolumeIcon = new ImageView(new Image(getClass().getResourceAsStream("/lowVolumeIcon.png"), 23, 23, false, false));
        mediumVolumeIcon = new ImageView(new Image(getClass().getResourceAsStream("/mediumVolumeIcon.png"), 23, 23, false, false));
        highVolumeIcon = new ImageView(new Image(getClass().getResourceAsStream("/highVolumeIcon.png"), 23, 23, false, false));

        // Initializes the video layout
        videoLayout = new BorderPane();
 
        // Initializes the play button and it's properties
        playButton = new Button();
        playButton.setGraphic(pauseIcon);
        playButton.setId("interface");
        playButton.setMinSize(23, 23);
        playButton.setPrefSize(23, 23);
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                updateVideoState();
            }
        });

        // Initializes the time slider and it's properties
        timeSlider = new Slider();
        HBox.setHgrow(timeSlider,Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        timeSlider.setOnMouseReleased(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                timeSlider.setValueChanging(true);
                timeSlider.setValue((event.getX()/timeSlider.getWidth())*timeSlider.getMax());
                timeSlider.setValueChanging(false);
            }
            
        });
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
               if (timeSlider.isValueChanging()) {
                    // Multiplies the duration by percentage calculated by slider position
                    currentVideo.seek(duration.multiply(timeSlider.getValue() / 100.0));
               }
            }
        });

        // Initializes the label that holds thecurrent time and duration of the video
        playTime = new Label();
        playTime.setMinWidth(50);
        playTime.setMaxWidth(100);

        // Initializes the button that shows the volume and it's properties
        Button volumeButton = new Button();
        volumeButton.setGraphic(highVolumeIcon);
        volumeButton.setId("interface");
        volumeButton.setMinSize(23, 23);
        volumeButton.setPrefSize(23, 23);
        // If the button is pressed, then mute the video.
        // Otherwise, unmute the video.
        volumeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if(!volumeButton.getGraphic().equals(muteIcon)){
                    currentVideo.setMute(true);
                    volumeButton.setGraphic(muteIcon);
                } else {
                    currentVideo.setMute(false);
                    if(currentVideo.getVolume() > .75){
                       volumeButton.setGraphic(highVolumeIcon);
                    } else if (currentVideo.getVolume() > .25){
                        volumeButton.setGraphic(mediumVolumeIcon);
                    } else if (currentVideo.getVolume() > 0){
                        volumeButton.setGraphic(lowVolumeIcon);
                    } else {
                        volumeButton.setGraphic(muteIcon);
                    }
                }
                
            }
        });
        
        // Initializes the volume slider and it's properties
        volumeSlider = new Slider();
        volumeSlider.setMinWidth(30);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setPrefWidth(70);
        volumeSlider.setOnMouseReleased(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                volumeSlider.setValueChanging(true);
                volumeSlider.setValue((event.getX()/volumeSlider.getWidth())*volumeSlider.getMax());
                volumeSlider.setValueChanging(false);
            }
        });
        // Updates the volume button's graphic depending on the volume of the video
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable ov) {
               if (volumeSlider.isValueChanging()) {
                   currentVideo.setMute(false);
                   currentVideo.setVolume(volumeSlider.getValue() / 100.0);
                   if(currentVideo.getVolume() > .75){
                       volumeButton.setGraphic(highVolumeIcon);
                   } else if (currentVideo.getVolume() > .25){
                       volumeButton.setGraphic(mediumVolumeIcon);
                   } else if (currentVideo.getVolume() > 0){
                       volumeButton.setGraphic(lowVolumeIcon);
                   } else {
                       volumeButton.setGraphic(muteIcon);
                   }
               }
            }
        });
        
        // Groups the video manipulation elements into a row, and sets the row's properties
        mediaBar = new HBox(playButton, volumeButton, volumeSlider, playTime, timeSlider);
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setSpacing(3);
        mediaBar.setPadding(new Insets(5));
        mediaBar.setId("interface");
        videoLayout.setBottom(mediaBar);
        
        // Initializes the video player and it's properties
        videoPlayer = new MediaView();
        videoPlayer.fitWidthProperty().bind(videoLayout.widthProperty());
        videoPlayer.fitHeightProperty().bind(videoLayout.heightProperty().subtract(mediaBar.heightProperty()));
        videoPlayer.setPreserveRatio(false);
        videoPlayer.setManaged(false);
        videoPlayer.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton().equals(MouseButton.PRIMARY)){
                    updateVideoState();
                }
            }
        });
        
        // Currently does not work, have to figure it out later.
        videoPlayer.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case RIGHT:
                        currentVideo.seek(currentVideo.getCurrentTime().add(Duration.seconds(5)));
                        break;
                    case LEFT:
                        currentVideo.seek(currentVideo.getCurrentTime().subtract(Duration.seconds(5)));
                        break;
                    case SPACE:
                        playButton.fire();
                        break;
                    default:
                        break;
                }
            }
        });
        videoPlayer.setDisable(false);
        videoPlayer.getStyleClass().add("middle");
        videoLayout.setCenter(videoPlayer);
    }
    
    /**
     * Updates the current state of the video depending on the state
     */
    public void updateVideoState() {
        if(currentVideo != null){
            MediaPlayer.Status currentStatus = currentVideo.getStatus();
            switch (currentStatus) {
                case PLAYING:
                    currentVideo.pause();
                    playButton.setGraphic(playIcon);
                    break;
                case PAUSED:
                    currentVideo.play();
                    playButton.setGraphic(pauseIcon);
                default:
                    break;
            }
        }
    }
    
    /**
     * Updates the current time or volume of the video depending on the value of the time or volume slider.
     */
    protected void updateValues() {
        if (playTime != null && timeSlider != null && volumeSlider != null && currentVideo != null) {
           Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Duration currentTime = currentVideo.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                    }
                    if (!volumeSlider.isValueChanging()) {
                      volumeSlider.setValue((int)Math.round(currentVideo.getVolume() * 100));
                    }
                }
            });
        }
    }
    
    // Formats the time of the video for the time label
    private String formatTime(Duration elapsed, Duration duration) {
        int elapsedHours = (int) elapsed.toHours();
        int elapsedMinutes = (int) elapsed.toMinutes() % 60;
        int elapsedSeconds = (int) elapsed.toSeconds() % 60;
        
        int durationHours = (int) duration.toHours();
        int durationMinutes = (int) duration.toMinutes() % 60;
        int durationSeconds = (int) duration.toSeconds() % 60;
        
        if(currentVideo != null && durationHours > 0){
            return String.format("%2d:%02d:%02d",
                elapsedHours,
                elapsedMinutes,
                elapsedSeconds) + "/" + String.format("%2d:%02d:%02d",
                durationHours,
                durationMinutes,
                durationSeconds);
        } else {
            return String.format("%02d:%02d",
                elapsedMinutes,
                elapsedSeconds) + "/" + String.format("%02d:%02d",
                durationMinutes,
                durationSeconds);
        }
        
    }
    
    public MediaPlayer getVideo() {
        return currentVideo;
    }
    
    public MediaView getPlayer(){
        return videoPlayer;
    }
    
    public BorderPane getLayout() {
        return videoLayout;
    }

    public HBox getMediaBar(){
        return mediaBar;
    }
    public void setDuration(Duration d) {
        duration = d;
    }
    
    public void setVideo(MediaPlayer v) {
        if(currentVideo != null){
            currentVideo.dispose();
        }
        
        currentVideo = v;
        videoPlayer.setMediaPlayer(currentVideo);
        playButton.setGraphic(pauseIcon);
    }
}
