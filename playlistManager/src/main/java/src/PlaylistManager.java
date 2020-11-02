/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 * 
 *
 * @author Jacob Jones
 * @version alpha
 * @date 1/23/20
 *
 * This program will receive a list of videos from YouTube, and allows the user
 * to play the videos
 */
public class PlaylistManager extends Application {

    // The Vriables
    // The Minimum Width and Height of the window, for proper scaling
    private int minWidth = 720;
    private int minHeight = 540;
    
    // The context menu used for Videos
    private ContextMenu videoOptions;
    
    // The Services
    private Service<Void> loadFromURLService; // Retrieves the playlist through URL
    private Service<Void> loadFromFileService; // Retreieves the playlist through the saved files

    // The path for the folder that holds all of the playlist
    private final String playlistPath = "playlists";
    
    // The UI Icons
    private ImageView trashCanIcon;
    private ImageView refreshPageIcon;
    private ImageView playlistSettingsIcon;
    private ImageView videoSettingsIcon;
    private ImageView previousVideoIcon;
    private ImageView nextVideoIcon;
    
    // The Files
    private File playlistsFolder; // The directory of the playlist folders

    // The Ints
    private final int pageSizeLimit = 50; // The limit of items on the current page
    private int pageNumber; // The current page in the playlist
    private int maxPageNumber; // The highest page the playlist can be

    // The Layout Variables
    private BorderPane layout;
    private Scene scene;

    // The Top Variables
    // The Playlist Search Row Elements
    private Label playlistSearchLabel;
    private TextField playlistSearchField;
    private Button playlistSearchButton;
    private ComboBox savedPlaylistBox;
    private Button playlistSettingsButton;
    private Button deletePlaylistButton;
    private Button refreshPageButton;
    private HBox playlistSearchRow;
     // The Video Search Row Elements
    private Label videoSearchLabel;
    private ContextMenu searchSuggestions;
    private TextField videoSearchField;
    private Button videoSearchButton;
    private Button searchResetButton;
    private CheckBox isTagSearch;
    private HBox videoSearchRow;
    
    // Groups the two search rows together
    private VBox searchCol;

    // The Center Variables
    // The Video Player Elements
    private Button videoSettingsButton;
    private Button previousVideoButton;
    private Button nextVideoButton;
    private VideoInterface videoPlayer;
    
    // The Progress Bar Elements
    private String loadedVideoTitle;
    private Label videoProgressLabel;
    private int currentPosition; // The position of the last loaded video
    private int maxListSize; // The size of the list
    private int responseListSize; // THe size of the list on youtube
    private Timeline updateProgress;
    private ProgressBar videoProgress;
    private VBox videoProgressCol;

    // The Bottom Variables
    private Label tagLabel;
    private TextField tagField;
    private Button setTagsButton;
    private HBox tagRow;

    // The Right Variables
    private CheckBox isRandom; // Randomizes the list
    private CheckBox isBiased; // Gives bias to the randomization
    private ArrayList<PlaylistVideo> randomizedPlaylist;
    private ArrayList<PlaylistVideo> searchResults;
    private ArrayList<PlaylistVideo> reorderedPlaylist; // The custom order of the list in the view
    private ArrayList<PlaylistVideo> playlistPage; // The videos in the playlist that is currently displayed
    private ListView<PlaylistVideo> playlistView; // Displays the videos in the list
    private Label pageNumberLabel;
    private Button firstPageButton;
    private Button previousPageButton;
    private Button nextPageButton;
    private Button lastPageButton;
    private HBox pageRow;
    private VBox playlistInterface;

    // The Videos and Playlists
    private PlaylistVideo currentVideo;
    private VideoPlaylist currentPlaylist;
    private ArrayList<PlaylistVideo> currentPageList; // The videos shown in the view

    // The Object that downloads the video files
    private YoutubeDownloader downloader;

    /**
     * @param args the command line arguments
     * @throws java.security.GeneralSecurityException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.net.URISyntaxException
     */
    public static void main(String[] args) throws GeneralSecurityException, IOException, ClassNotFoundException, URISyntaxException {
        
        // Loads the API Key from the file. Make sure this file is *not* uploaded to Github.
        try (Scanner keyScanner = new Scanner(new File(PlaylistManager.class.getClassLoader().getResource("API_KEY.txt").toURI()))) {
            YoutubeAPI.getService(keyScanner.nextLine());
        }
        
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Initializes the downloader
        downloader = new YoutubeDownloader();
        
        // Initializes the folder path
        playlistsFolder = new File(playlistPath);
        // If the folder does not exist, make it.
        if (!playlistsFolder.exists()) {
            playlistsFolder.mkdirs();
        }
        
        // Initializes the icons used in the GUI
        // The Bigger Icons
        trashCanIcon = new ImageView(new Image(getClass().getResourceAsStream("/trashCanIcon.png"), 25, 25, false, false));
        refreshPageIcon = new ImageView(new Image(getClass().getResourceAsStream("/refreshPageIcon.png"), 25, 25, false, false));
        playlistSettingsIcon = new ImageView(new Image(getClass().getResourceAsStream("/settingsIcon.png"), 25, 25, false, false));
        // The Smaller Icons
        videoSettingsIcon = new ImageView(new Image(getClass().getResourceAsStream("/settingsIcon.png"), 23, 23, false, false));
        nextVideoIcon = new ImageView(new Image(getClass().getResourceAsStream("/nextVideoIcon.png"), 23, 23, false, false));
        previousVideoIcon = new ImageView(new Image(getClass().getResourceAsStream("/previousVideoIcon.png"), 23, 23, false, false));
        
        // Initializes the video options
        videoOptions = new ContextMenu();
        // Plays the video if the video is paused, and vice versa
        MenuItem playOption = new MenuItem("Play");
        playOption.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // If the video is currently playing, pause/continue playing it
                if (currentVideo.equals(playlistView.getSelectionModel().getSelectedItem())){
                    if (videoPlayer.getVideo().getStatus().equals(MediaPlayer.Status.PAUSED)){
                        videoPlayer.getVideo().play();
                    } else {
                        videoPlayer.getVideo().pause();
                    }
                    videoPlayer.updateVideoState();
                } 
                // Otherwise, load up the selected video
                else {
                    try {
                        currentVideo = playlistView.getSelectionModel().getSelectedItem();
                        updateVideoPlayer("=");
                    } catch (YoutubeException | IOException ex) {
                        showException(ex);
                    }
                }
            }
        });

        // Initializes the properties option
        MenuItem propertiesOption = new MenuItem("Properties");
        propertiesOption.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showVideoProperties(playlistView.getSelectionModel().getSelectedItem());
            }
        });

        // Loads the options into the context menu
        videoOptions.getItems().addAll(playOption, propertiesOption);
        
        // Intializes the URL service
        loadFromURLService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                Task<Void> loadFromURLTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Retrievs the text from the search field
                        String playlistURL = playlistSearchField.getText();
                        
                        // Checks if the given URL is one that contains a playlist
                        if (playlistURL.length() > 0 && playlistURL.contains("list=")) {
                            // Obtains the playlist information from the Youtube API
                            Playlist receivedPlaylist = YoutubeAPI.requestPlaylist(playlistURL.substring(playlistURL.indexOf("list=") + 5)).getItems().get(0);
                            // Loads the playlist's name into a variable
                            String playlistName = receivedPlaylist.getSnippet().getTitle();
                            try {
                                // Puts all of the videos obtained from the playlist into a List
                                List<PlaylistItem> responseList = retrievePlaylist(playlistURL.substring(playlistURL.indexOf("list=") + 5));
                                
                                // Filters out any characters that would prevent the creation of a folder
                                String filteredPlaylistName = playlistName.replace(":", "_").replace("?", "_").replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>]|[.]{2,}$|[.]$", "").trim();
                                
                                // Loads the filepath for the folder into a File
                                File playlistFolder = new File(playlistPath, filteredPlaylistName);
                                // If the playlist folder doesn't exist, make it
                                if (!playlistFolder.exists()) {
                                    playlistFolder.mkdirs();
                                }
                                
                                // Checks if the retrieved playlist is the same as the currently playing playlist
                                if (!playlistName.equals(currentPlaylist.getPlaylistName())) {
                                    // Checks if the retrieved playlist has been loaded before
                                    if (!savedPlaylistBox.getItems().contains(playlistName)) {
                                        // Loads the properties of the playlist into the needed variables
                                        String playlistID = receivedPlaylist.getId();
                                        String playlistDescription = receivedPlaylist.getSnippet().getDescription();
                                        String playlistChannel = receivedPlaylist.getSnippet().getChannelTitle();
                                        String playlistThumbnailURL = receivedPlaylist.getSnippet().getThumbnails().getMedium().getUrl();
                                        int playlistThumbnailWidth = receivedPlaylist.getSnippet().getThumbnails().getMedium().getWidth().intValue();
                                        int playlistThumbnailHeight = receivedPlaylist.getSnippet().getThumbnails().getMedium().getHeight().intValue();
                                        
                                        // Loads the retrieved playlist into a VideoPlaylist object
                                        currentPlaylist = new VideoPlaylist(playlistFolder.getCanonicalPath(), playlistID, playlistName, playlistDescription, playlistChannel, playlistThumbnailURL, playlistThumbnailWidth, playlistThumbnailHeight);
                                        
                                        // Retrieves the size of the list and puts it into a variable
                                        int listSize = responseList.size();
                                        maxListSize = listSize;
                                        responseListSize = listSize;
                                        currentPosition = 0;
                                        
                                        // Retrieves the videos in the playlists and coverts them into PlaylistVideo objects
                                        for (int i = 0; i < maxListSize; i++) {
                                            // If the task is cancelled, stop the loop
                                            if (isCancelled()) {
                                                break;
                                            }
                                            
                                            // Retrieves the video from the response list
                                            PlaylistItem item = responseList.get(i);
                                            // Retrieves the name of the video and stores it ina  variable
                                            String videoName = item.getSnippet().getTitle();
                                            // Filters the video name for any characters that might prevent folder creation
                                            String filteredVideoName = videoName.replace(":", "_").replace("?", "_").replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>]|[.]{2,}$|[.]$", "").trim();
                                            
                                            // Checks if the video can have it's data retrieved. Otherwise, skip it.
                                            if (!videoName.equals("Deleted Video") && !videoName.equals("Deleted video") && !videoName.isEmpty() && !videoName.equals("")) {
                                                loadVideoFromURL(item, playlistFolder.getCanonicalPath(), videoName, filteredVideoName);
                                                // Updates the variables needed for the progress bar
                                                loadedVideoTitle = videoName;
                                                currentPosition = i;
                                                maxListSize = listSize;
                                                videoProgress.setProgress(((double) currentPosition) / maxListSize);
                                            }
                                        }
                                        
                                    } 
                                    // Otherwise, load it from the files instead
                                    else {
                                        currentPlaylist = new VideoPlaylist(playlistPath + "/" + filteredPlaylistName, playlistName);
                                        loadVideoFromFile(playlistName, receivedPlaylist);
                                    }
                                } 
                                // Otherwise, tell the user that it is already playing
                                else {
                                    Platform.runLater(() -> {
                                        Alert playlistAlert = new Alert(AlertType.INFORMATION);
                                        playlistAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                                        playlistAlert.setTitle("Playlist \"" + playlistName + "\" Already Playing");
                                        playlistAlert.setHeaderText(null);
                                        playlistAlert.setContentText("That playlist is currently playing.");
                                        playlistAlert.showAndWait();
                                        playlistSearchField.setText("");
                                    });
                                }
                            } 
                            // If the playlist is unable to be retrieved, load the version of it from the files
                            catch (GeneralSecurityException | IOException | ClassNotFoundException ex) {
                                showException(ex);
                                loadVideoFromFile(playlistName, null);
                            }
                        } 
                        // Otherwise, tell the user to enter the video in the proper format
                        else {
                            Platform.runLater(() -> {
                                Alert invalidLinkAlert = new Alert(AlertType.INFORMATION);
                                invalidLinkAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                                invalidLinkAlert.setTitle("Invalid Link");
                                invalidLinkAlert.setHeaderText(null);
                                invalidLinkAlert.setContentText("Make sure the link is a valid playlist url!");
                                invalidLinkAlert.showAndWait();
                                playlistSearchField.setText("");
                            });
                        }
                        return null;
                    }
                };

                loadFromURLTask.setOnScheduled((schedueledEvent) -> {
                    primaryStage.setTitle("Playlist Manager");
                    disableGUI();
                });

                loadFromURLTask.setOnCancelled(cancelledEvent -> {
                    // Resets the GUI, and shows the error
                    layout.setCenter(videoPlayer.getLayout());
                    savedPlaylistBox.setValue(null);
                    savedPlaylistBox.setDisable(false);
                    showException(cancelledEvent.getSource().getException());
                });
                
                loadFromURLTask.setOnFailed(failedEvent -> {
                    // Resets the GUI, and shows the error
                    layout.setCenter(videoPlayer.getLayout());
                    savedPlaylistBox.setValue(null);
                    savedPlaylistBox.setDisable(false);
                    showException(failedEvent.getSource().getException());
                });
                
                loadFromURLTask.setOnSucceeded((successEvent) -> {
                    try {
                        // Sets the size of the playlist
                        currentPlaylist.setSize(currentPlaylist.getPlaylist().size());
                        // If the playlist loaded successfully, reset the GUI
                        if (currentPlaylist.getSize() >= 1) {
                            resetGUI();
                        }
                    } catch (FileNotFoundException | MalformedURLException ex) {
                        showException(ex);
                    } catch (YoutubeException | IOException ex) {
                        showException(ex);
                    }
                });
                return loadFromURLTask;
            }
        };

        // Initializes the File service
        loadFromFileService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                Task<Void> loadFromFileTask = new Task<Void>() {
                    @Override
                    protected Void call() throws IOException, StackOverflowError, GeneralSecurityException, GeneralSecurityException, GoogleJsonResponseException, GoogleJsonResponseException, ClassNotFoundException, ClassNotFoundException, ClassNotFoundException, ClassNotFoundException {
                        
                        // Retrieves the playlist name from the box, and filters the name
                        String playlistName = savedPlaylistBox.getValue().toString().replace(":", "_").replace("?", "_").replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>]|[.]{2,}$|[.]$", "").trim();
                        try {
                            // Creates a new VideoPlaylist based on the loaded name
                            currentPlaylist = new VideoPlaylist(playlistPath + "/" + playlistName, playlistName);
                            
                            // Puts all of the videos obtained from the playlist into a List
                            Playlist receivedPlaylist = YoutubeAPI.requestPlaylist(currentPlaylist.getPlaylistID()).getItems().get(0);
                            
                            // If the program was able to retrieve the playlist, load the file and give the playlist to the method
                            if (receivedPlaylist != null){
                                loadVideoFromFile(playlistName, receivedPlaylist);
                            } 
                            // Otherwise, just give the name
                            else {
                                loadVideoFromFile(playlistName, null);
                            }

                        } catch (IOException | StackOverflowError | ClassNotFoundException ex) {
                            showException(ex);
                            loadVideoFromFile(playlistName, null);
                        }

                        return null;
                    }
                };

                loadFromFileTask.setOnScheduled((succeesesEvent) -> {
                    primaryStage.setTitle("Playlist Manager");
                    disableGUI();
                });

                loadFromFileTask.setOnCancelled(cancelledEvent -> {
                    // Resets the GUI, and shows the error
                    layout.setCenter(videoPlayer.getLayout());
                    savedPlaylistBox.setValue(null);
                    savedPlaylistBox.setDisable(false);
                    showException(cancelledEvent.getSource().getException());
                });
                
                loadFromFileTask.setOnFailed(failedEvent -> {
                    // Resets the GUI, and shows the error
                    layout.setCenter(videoPlayer.getLayout());
                    savedPlaylistBox.setValue(null);
                    savedPlaylistBox.setDisable(false);
                    showException(failedEvent.getSource().getException());
                });
                
                loadFromFileTask.setOnSucceeded((succeededEvent) -> {
                    try {
                        currentPlaylist.setSize(currentPlaylist.getPlaylist().size());
                        resetGUI();
                    } catch (MalformedURLException | YoutubeException | FileNotFoundException ex) {
                        showException(ex);
                    } catch (IOException ex) {
                        showException(ex);
                        
                    }
                });
                return loadFromFileTask;
            }
        };

        // Initializes the ArrayLists
        currentPlaylist = new VideoPlaylist();
        randomizedPlaylist = new ArrayList<>();
        searchResults = new ArrayList<>();
        reorderedPlaylist = new ArrayList<>();
        playlistPage = new ArrayList<>();

        // Initializes the current video
        currentVideo = new PlaylistVideo();

        // Initializes the layout and scene
        layout = new BorderPane();
        
        // Initializes the scene and it's properties
        scene = new Scene(layout, minWidth, minHeight);
        scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
        
        // Sets up the window's properties
        primaryStage.setTitle("Playlist Manager");
        primaryStage.setMinWidth(minWidth);
        primaryStage.setWidth(minWidth);
        primaryStage.setMinHeight(minHeight);
        primaryStage.setHeight(minHeight);
        primaryStage.setScene(scene);
        primaryStage.show();

        // If the window is closed, stop all tasks
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                // If the playlist overrides the video's settings, use the playlist's properties instead
                if (currentPlaylist.getOverrideSettings()){
                    if (currentVideo.getVideoPath() != null && !currentPlaylist.getIsSaved()){
                        videoPlayer.setVideo(null);
                        currentVideo.deleteVideo();
                    }
                }
                // Otherwise, use the video's
                else {
                    if (currentVideo.getVideoPath() != null && !currentVideo.getIsSaved()){
                        videoPlayer.setVideo(null);
                        currentVideo.deleteVideo();
                    }
                }
                Platform.exit();
                System.exit(0);
            }
        });

        // Sets up the playlist search variables
        playlistSearchLabel = new Label("Enter Playlist URL here: ");
        playlistSearchField = new TextField();
        playlistSearchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                // If the user presses enter, start searching for the playlist
                if (event.getCode().equals(KeyCode.ENTER)){
                    playlistSearchButton.fire();
                }
            }
        });
        
        // Initializes the playlist search button and it's properties
        playlistSearchButton = new Button("Search For Playlist");
        playlistSearchButton.setPadding(new Insets(5));
        playlistSearchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadFromURLService.restart();
            }
        });

        // Initialize the Combo Box that holds the playlist names and it's properties
        savedPlaylistBox = new ComboBox(getPlaylistNames(playlistsFolder.list()));
        savedPlaylistBox.setMinWidth(100);
        savedPlaylistBox.setDisable(savedPlaylistBox.getItems().isEmpty());
        savedPlaylistBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (savedPlaylistBox.getSelectionModel().getSelectedItem() != null && !savedPlaylistBox.getSelectionModel().getSelectedItem().equals(currentPlaylist.getPlaylistName())) {
                    loadFromFileService.restart();
                }
            }
        });

        // Initializes the playlist settings button and it's properties
        playlistSettingsButton = new Button();
        playlistSettingsButton.setGraphic(playlistSettingsIcon);
        playlistSettingsButton.setPrefSize(25, 25);
        playlistSettingsButton.setPadding(new Insets(2));
        playlistSettingsButton.setDisable(true);
        playlistSettingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Initializes the properties alert and it's properties
                Alert playlistPropertiesAlert = new Alert(AlertType.INFORMATION);
                playlistPropertiesAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                playlistPropertiesAlert.setTitle("Playlist Properties");
                playlistPropertiesAlert.setHeaderText(null);
                playlistPropertiesAlert.setWidth(600);
                playlistPropertiesAlert.setHeight(400);

                // Retrieves the needed properties from the playlist
                String playlistName = currentPlaylist.getPlaylistName();
                String playlistID = currentPlaylist.getPlaylistID();
                String playlistChannel = currentPlaylist.getChannel();
                String playlistDescription = currentPlaylist.getDescription();
                int playlistSize = currentPlaylist.getSize();
                String playlistTags = currentPlaylist.getTags();
                int playlistLoopCount = currentPlaylist.getLoopCount();
                double playlistSpeed = 0;
                
                // Loads some of the properties into a label
                Label topText = new Label("Name: " + playlistName + "\nChannel: " + playlistChannel);
                Label urlLabel = new Label("Playlist URL: ");
                
                // Creates a link to the playlist
                Hyperlink playlistURL = new Hyperlink("https://www.youtube.com/playlist?list=" + playlistID);
                playlistURL.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        // If the user has a browser and if the click was from the primary mouse button, open the link
                        if (event.getButton().equals(MouseButton.PRIMARY) && Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(new URI(playlistURL.getText()));
                            } catch (URISyntaxException | IOException ex) {
                                showException(ex);

                            }
                            playlistURL.setVisited(false);
                        }
                    }
                });
                
                // Creates an option to copy the URL
                MenuItem copyOption = new MenuItem("Cooy");
                copyOption.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();

                        content.putString(playlistURL.getText());
                        clipboard.setContent(content);
                    }
                });
                
                // Loads the copy option into a context menu, which will open when the URL is right-clicked
                ContextMenu copyMenu = new ContextMenu(copyOption);
                playlistURL.setContextMenu(copyMenu);
                
                HBox urlRow = new HBox(urlLabel, playlistURL);
                urlRow.setSpacing(5);
                urlRow.setAlignment(Pos.CENTER_LEFT);
                
                // Loads up some of the properties into a label
                Label middleText = new Label("Size: " + playlistSize
                        + "\nTags: " + playlistTags + "\nDescription: ");
                middleText.setPrefWidth(playlistPropertiesAlert.getWidth());
                middleText.setWrapText(true);
                
                // Initializes the check box that changes if the playlist's properties will override the video's and it's properties
                CheckBox  overrideSettings = new CheckBox("Override Video Settings?");
                overrideSettings.setSelected(currentPlaylist.getOverrideSettings());
                overrideSettings.setTooltip(new Tooltip("Forces all videos to follow the following settings, unless they already have them modified."));
                overrideSettings.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            currentPlaylist.setOverrideSettings(overrideSettings.isSelected());
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }
                });
                
                // Loads the playlist's loop information into a lebel
                TextField loopField = new TextField(playlistLoopCount + "");

                // Intiializes the check box that changes if the videos will loop
                CheckBox isLooping = new CheckBox("Loop Video?");
                isLooping.setSelected(currentPlaylist.getIsLooping());
                isLooping.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            currentPlaylist.setIsLooping(isLooping.isSelected());
                            // If the check box gets deselected, reset the properties
                            if (!isLooping.isSelected()){
                                currentPlaylist.setLoopCount(1);
                                videoPlayer.getVideo().setCycleCount(1);
                                loopField.setText(currentPlaylist.getLoopCount() + "");
                            }
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }
                });
                isLooping.setTooltip(new Tooltip("Any value above -1 works, with -1 acting as Infinity."));

                // Initializes the button that sets the playlists' loop count
                Button setLoopButton = new Button("Set Playlist Video Loop Count");
                setLoopButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent buttonEvent) {
                        try {
                            // Retrieves the value from the field
                            int loopValue = Integer.parseInt(loopField.getText());
                            // If it is below zero, just set it to infinite
                            if (loopValue < 0){
                                currentPlaylist.setLoopCount(-1);
                                videoPlayer.getVideo().setCycleCount(MediaPlayer.INDEFINITE);
                            } 
                            // Otherwise, set the value to the playlist and video player
                            else {
                                currentPlaylist.setLoopCount(Integer.parseInt(loopField.getText()));
                                videoPlayer.getVideo().setCycleCount(currentPlaylist.getLoopCount());
                            }
                            loopField.setText(currentPlaylist.getLoopCount() + "");
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }
                });
                loopField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (event.getCode().equals(KeyCode.ENTER)){
                            setLoopButton.fire();
                        }
                    }
                });
                
                // Initializzes the buttont hat resets the loop count
                Button loopCountResetButton = new Button("Reset");
                loopCountResetButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent buttonEvent) {
                        try {
                            currentPlaylist.setLoopCount(1);
                            videoPlayer.getVideo().setCycleCount(1);
                            loopField.setText("1");
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }
                });
                
                // Groups the loop elements into a row and initializes the row's properties
                HBox loopRow = new HBox(loopField, setLoopButton, loopCountResetButton);
                loopRow.setSpacing(5);
                loopRow.disableProperty().bind(isLooping.selectedProperty().not());

                // Loads the playlist's speed into a label
                TextField speedField = new TextField(String.format("%.0f", currentPlaylist.getSpeed() * 100) + "%");
                
                // Initializes the button that sets the speed
                Button setSpeedButton = new Button("Set Speed");
                setSpeedButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent buttonEvent) {
                        try {
                            double speedValue = Double.parseDouble(speedField.getText().replace("%", "")) / 100;
                            currentPlaylist.setSpeed(speedValue);
                            videoPlayer.getVideo().setRate(currentPlaylist.getSpeed());
                            speedField.setText(String.format("%.0f", currentPlaylist.getSpeed() * 100) + "%");
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }
                });
                speedField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (event.getCode().equals(KeyCode.ENTER)){
                            setSpeedButton.fire();
                        }
                    }
                });

                // Initializes the checkbox that enables changes to the playlist's speed
                CheckBox isSpeedChanged = new CheckBox("Change Playlist Video Speed?");
                isSpeedChanged.setSelected(currentPlaylist.getIsSpeedChanged());
                isSpeedChanged.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            currentPlaylist.setIsSpeedChanged(isSpeedChanged.isSelected());
                            // If unchecked, reset the speed values of the playlist and video player
                            if (!isSpeedChanged.isSelected()){
                                currentPlaylist.setSpeed(1);
                                videoPlayer.getVideo().setRate(1);
                                speedField.setText(String.format("%.0f", currentPlaylist.getSpeed() * 100) + "%");
                            }
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }

                });
                isSpeedChanged.setTooltip(new Tooltip("Minimum is 50%, Maximum is 300%"));

                // Initializes the button that reset's the playlist's speed
                Button resetSpeedButton = new Button("Reset");
                resetSpeedButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent buttonEvent) {
                        try {
                            currentPlaylist.setSpeed(1);
                            videoPlayer.getVideo().setRate(1);
                            speedField.setText("100%");
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }
                });
                
                // Groups the speed elements into a row, and initializes the row's properties
                HBox speedRow = new HBox(speedField, setSpeedButton, resetSpeedButton);
                speedRow.setSpacing(5);
                speedRow.disableProperty().bind(isSpeedChanged.selectedProperty().not());

                // Initializes the checkbox that changes if videos will be saved
                CheckBox isSaved = new CheckBox("Save Playlist Videos?");
                isSaved.setSelected(currentPlaylist.getIsSaved());
                isSaved.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            currentPlaylist.setIsSaved(isSaved.isSelected());
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }

                });

                // Loads the description into a label
                Label descriptionText = new Label("Description:");
                if(playlistDescription.isEmpty()){
                    descriptionText.setText("Description: None");
                }
                
                // Initializes the text area that shows the description and it's properties
                TextArea textArea = new TextArea();
                textArea.maxHeight(100);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                
                // If the description isn't empty, change the size of the text area according to
                // how many lines the description has
                if(!playlistDescription.isEmpty()){
                    textArea.setText(playlistDescription);
                    if(playlistDescription.contains("\n")){
                        int lineCount = playlistDescription.split("\n").length;
                        if(lineCount > 10){
                            textArea.setPrefHeight(200);
                        } else {
                            textArea.setPrefHeight(20 * lineCount);
                        }

                    }
                }

                // Initializes the button that resets all of the playlist's video properties
                Button resetButton = new Button("Reset Overrided Properties");
                resetButton.setOnAction(new EventHandler<ActionEvent> () {
                    @Override
                    public void handle(ActionEvent event) {
                        // Initializes the alert that checks if the user wants to reset the playlist's properties, and then sets it's properties
                        Alert resetAlert = new Alert(AlertType.CONFIRMATION);
                        resetAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                        resetAlert.setTitle("Confirm Property Reset");
                        resetAlert.setHeaderText(null);
                        resetAlert.setContentText("Are you sure you want to reset the overrided properties of the playlist \"" + currentPlaylist.getPlaylistName() + "\"?");

                        ButtonType optionYes = new ButtonType("Yes");
                        ButtonType optionNo = new ButtonType("No");

                        resetAlert.getButtonTypes().setAll(optionYes, optionNo);

                        Optional<ButtonType> result = resetAlert.showAndWait();

                        // If the user chooses yes, then reset the playlist's properties
                        if (result.get() == optionYes) {
                            try {
                                currentPlaylist.setIsLooping(false);
                                currentPlaylist.setLoopCount(1);
                                currentPlaylist.setIsSpeedChanged(false);
                                currentPlaylist.setSpeed(1);
                                currentPlaylist.setIsSaved(false);

                                overrideSettings.setSelected(false);
                                isLooping.setSelected(false);
                                loopField.setText(1 + "");
                                isSpeedChanged.setSelected(false);
                                speedField.setText("100%");
                                isSaved.setSelected(false);

                                videoPlayer.getVideo().setCycleCount(currentVideo.getLoopCount());
                                videoPlayer.getVideo().setRate(currentVideo.getSpeed());
                            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                                showException(ex);
                            }
                        }
                    }
                });

                // Initializes the button that resets all of the playlist's properties, and of the videos inside of it
                Button playlistResetButton = new Button("Reset Video Properties");
                playlistResetButton.setOnAction(new EventHandler<ActionEvent> () {
                    @Override
                    public void handle(ActionEvent event) {
                        Alert resetAlert = new Alert(AlertType.CONFIRMATION);
                        resetAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                        resetAlert.setTitle("Confirm Property Reset");
                        resetAlert.setHeaderText(null);
                        resetAlert.setContentText("Are you sure you want to reset the properties of all the videos in the playlist \"" + currentPlaylist.getPlaylistName() + "\"?");

                        ButtonType optionYes = new ButtonType("Yes");
                        ButtonType optionNo = new ButtonType("No");

                        resetAlert.getButtonTypes().setAll(optionYes, optionNo);

                        Optional<ButtonType> result = resetAlert.showAndWait();
                        
                        // If the user chooses yes, then go through each video's properties and reset them
                        if (result.get() == optionYes) {
                            try {
                                ArrayList<PlaylistVideo> tempPlaylist = currentPlaylist.getPlaylist();
                                for (PlaylistVideo video : tempPlaylist){
                                    video.setPositionBias(0);
                                    video.setIsLooping(false);
                                    video.setLoopCount(1);
                                    video.setIsSpeedChanged(false);
                                    video.setSpeed(1);
                                    video.setIsSaved(false);
                                    video.setTags("None");
                                }
                                
                                currentPlaylist.setTags("None");
                                middleText.setText("Size: " + playlistSize
                                    + "\nTags: None");
                                
                                videoPlayer.getVideo().setCycleCount(1);
                                videoPlayer.getVideo().setRate(1);
                            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                                showException(ex);
                            }
                        }
                    }
                });
                
                // Groups the reset elements into a row, and sets the row's properties
                HBox resetRow = new HBox(resetButton, playlistResetButton);
                resetRow.setSpacing(5);
                
                // Groups the overridable properties into a column, and sets the column's properties
                VBox propertiesCol = new VBox(isLooping, loopRow, isSpeedChanged, speedRow, isSaved);
                propertiesCol.setSpacing(5);
                propertiesCol.disableProperty().bind(overrideSettings.selectedProperty().not());
                
                // Initializes the column of all of the properties
                VBox alertCol;
                
                // If the description is empty, load everything but the text area into the column
                if (playlistDescription.isEmpty()){
                    alertCol = new VBox(topText, urlRow, middleText, overrideSettings, propertiesCol, descriptionText, resetRow);
                } 
                // Otherwise, load everything into the column
                else {
                    alertCol = new VBox(topText, urlRow, middleText, overrideSettings, propertiesCol, descriptionText, textArea, resetRow);
                }
                
                // Sets the alert column's properties
                alertCol.setAlignment(Pos.CENTER_LEFT);
                alertCol.setPadding(new Insets(10));
                alertCol.setSpacing(5);
                
                // Loads the alert column into the alert, and then shows the alert.
                playlistPropertiesAlert.getDialogPane().setContent(alertCol);
                // Sets the ok button to not be the default button, as to allow the enter button to work as intended
                ((Button)playlistPropertiesAlert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
                playlistPropertiesAlert.setGraphic(null);
                playlistPropertiesAlert.show();
            }
        });
        
        // Initializes the delete playlist button and it's properties
        deletePlaylistButton = new Button();
        deletePlaylistButton.setPrefSize(25, 25);
        deletePlaylistButton.setPadding(new Insets(2));
        deletePlaylistButton.setGraphic(trashCanIcon);
        deletePlaylistButton.setDisable(true);
        deletePlaylistButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Initializes the delete alert and it's properties
                Alert deleteAlert = new Alert(AlertType.CONFIRMATION);
                deleteAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                deleteAlert.setTitle("Confirm Playlist Deletion");
                deleteAlert.setHeaderText(null);
                deleteAlert.setContentText("Are you sure you want to delete the playlist \"" + currentPlaylist.getPlaylistName() + "\"?");

                ButtonType optionYes = new ButtonType("Yes");
                ButtonType optionNo = new ButtonType("No");

                deleteAlert.getButtonTypes().setAll(optionYes, optionNo);

                Optional<ButtonType> result = deleteAlert.showAndWait();

                // If yes is chosen, then delete the playlist's folder, and load up the next playlist.
                // If no playlist is available, reset the app to it's default state
                if (result.get() == optionYes) {
                    VideoPlaylist deletedPlaylist = currentPlaylist;
                    String playlistName = deletedPlaylist.getPlaylistName();

                    videoPlayer.setVideo(null);
                    currentVideo.deleteVideo();
                    
                    savedPlaylistBox.getItems().remove(playlistName);
                    playlistView.getItems().clear();
                    currentPlaylist = new VideoPlaylist();
                    currentVideo = new PlaylistVideo();

                    try {
                        deletedPlaylist.deletePlaylist(new File(deletedPlaylist.getPlaylistPath()));
                    } catch (IOException ex) {
                        showException(ex);
                        
                    }

                    if (savedPlaylistBox.getItems().isEmpty()) {
                        savedPlaylistBox.getSelectionModel().clearSelection();
                        savedPlaylistBox.setDisable(true);
                        deletePlaylistButton.setDisable(true);
                    } else {
                        savedPlaylistBox.getSelectionModel().select(0);
                    }
                }
            }

        });
        
        // Initializes the refresh page button and it's properties
        refreshPageButton = new Button();
        refreshPageButton.setPrefSize(25, 25);
        refreshPageButton.setPadding(new Insets(2));
        refreshPageButton.setGraphic(refreshPageIcon);
        refreshPageButton.setDisable(true);
        refreshPageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // If the search results are filled, update the view to show them
                    if (searchResults.size() > 0){
                        updatePageList(searchResults, "=", false);
                    } 
                    // Otherwise, if the list is randomizes, then update the view to show them
                    else if (isRandom.isSelected()) {
                        updatePageList(randomizedPlaylist, "=", false);
                    } 
                    // Otherwise, update the view to show the normal playlist
                    else {
                        updatePageList(currentPlaylist.getPlaylist(), "=", false);
                    }
                    // Updates the current video to match the displayed list
                    updateVideoPlayer("=");
                    resetGUI();
                } catch (YoutubeException | IOException ex) {
                    showException(ex);
                }
            }
        });

        // Puts all of the playlist search variables into a Row, and initializes the Row's properties
        playlistSearchRow = new HBox(playlistSearchLabel, playlistSearchField, playlistSearchButton, savedPlaylistBox, playlistSettingsButton, deletePlaylistButton, refreshPageButton);
        playlistSearchRow.setAlignment(Pos.CENTER);
        playlistSearchRow.setPadding(new Insets(10));
        playlistSearchRow.setSpacing(5);

        // Sets up the video search variables
        videoSearchLabel = new Label("Enter Video Name Here: ");
        videoSearchField = new TextField();
        videoSearchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!event.getCode().equals(KeyCode.SHIFT) && !event.getCode().equals(KeyCode.ENTER)) {
                    searchSuggestions.getItems().clear();
                    // Obtains the items in the list from the field, and puts it into an array
                    String[] userText = videoSearchField.getText().split(",");
                    // Puts in the latest character typed into the array, and then trims it
                    userText[userText.length - 1] += event.getCode().toString();
                    userText[userText.length - 1] = userText[userText.length - 1].trim();
                    
                    // Copies the current playlist into a new one as to prevent editing issues
                    // While the current playlist is loading
                    ArrayList<PlaylistVideo> searchPlaylist = currentPlaylist.getPlaylist();
                    // Prepares the list of search suggestions into an array list
                    ArrayList<MenuItem> suggestedTags = new ArrayList<>();
                    // If tag search is selected, then search within the playlist's tags
                    if (isTagSearch.isSelected()) {
                        for (String tag : currentPlaylist.getTags().split(",")) {
                            // If the search term exists within the tag list, then add it to the search suggestions
                            if (suggestedTags.size() < 10 && !userText[userText.length - 1].isEmpty() && tag.trim().toLowerCase().startsWith(userText[userText.length - 1].toLowerCase()) && tag.trim().compareToIgnoreCase(userText[userText.length - 1]) != 0) {
                                MenuItem item = new MenuItem(tag);
                                item.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        // If the item's first characters exist, then add it into the field
                                        if (userText.length > 1) {
                                            userText[userText.length - 1] = tag;
                                            videoSearchField.setText("");
                                            for (String newTag : userText) {
                                                if (newTag.equals(userText[userText.length - 1])) {
                                                    videoSearchField.setText(videoSearchField.getText() + newTag.trim());
                                                } else {
                                                    videoSearchField.setText(videoSearchField.getText() + newTag.trim() + ", ");
                                                }
                                            }
                                        } else {
                                            videoSearchField.setText(tag.trim());
                                        }
                                        videoSearchField.positionCaret(videoSearchField.getText().length());
                                    }
                                });
                                suggestedTags.add(item);
                            }
                        }
                    } 
                    // Otherwise, look through the existing videos
                    else {
                        for (PlaylistVideo video : searchPlaylist) {
                            String videoName = video.getVideoName();
                            // If the search term exists within the tag list, then add it to the search suggestions
                            if (suggestedTags.size() < 10 && !userText[userText.length - 1].isEmpty() && videoName.toLowerCase().contains(userText[userText.length - 1].toLowerCase()) && videoName.compareToIgnoreCase(userText[userText.length - 1]) != 0) {
                                MenuItem item = new MenuItem(videoName);
                                item.setOnAction(new EventHandler<ActionEvent>() {

                                    @Override
                                    public void handle(ActionEvent event) {
                                        // If the item's first characters exist, then add it into the field
                                        if (userText.length > 1) {
                                            userText[userText.length - 1] = videoName;
                                            videoSearchField.setText("");
                                            for (String newName : userText) {
                                                if (newName.equals(userText[userText.length - 1])) {
                                                    videoSearchField.setText(videoSearchField.getText() + newName.trim());
                                                } else {
                                                    videoSearchField.setText(videoSearchField.getText() + newName.trim() + ", ");
                                                }
                                            }
                                        } else {
                                            videoSearchField.setText(videoName.trim());
                                        }
                                        videoSearchField.positionCaret(videoSearchField.getText().length());
                                    }
                                });
                                suggestedTags.add(item);
                            }
                        }
                    }
                    
                    // Once it is done, add all of the items to the search suggestions, and then show it
                    searchSuggestions.getItems().addAll(FXCollections.observableArrayList(suggestedTags));
                    searchSuggestions.show(videoSearchField, Side.BOTTOM, 0, 0);
                } else if (event.getCode().equals(KeyCode.ENTER)){
                    videoSearchButton.fire();
                }
            }
        });

        // Initializes the search suggestions context menu and it's properties
        searchSuggestions = new ContextMenu();
        searchSuggestions.setMaxSize(scene.getWidth() / 2, scene.getHeight() / 4);

        // Initializes the video search button and it's properties
        videoSearchButton = new Button("Search For Video(s)");
        videoSearchButton.setPadding(new Insets(5));
        videoSearchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Clears the search result list to start from a clean slate
                searchResults.clear();
                
                ArrayList<PlaylistVideo> searchPlaylist = currentPlaylist.getPlaylist();
                // Goes through each video, and checks if the given text is in their names
                // and then adds them to the search results
                for (PlaylistVideo vid : searchPlaylist) {
                    if (isTagSearch.isSelected()) {
                        ArrayList<String> tags = new ArrayList<>(Arrays.asList(vid.getTags().split(",")));
                        for (int i = 0; i < tags.size(); i++) {
                            String tag = tags.get(i).toLowerCase();
                            if (tag.contains(videoSearchField.getText().toLowerCase())) {
                                searchResults.add(vid);
                            }
                        }
                    } else {
                        if (vid.getVideoName().toLowerCase().contains(videoSearchField.getText().toLowerCase())) {
                            searchResults.add(vid);
                        }
                    }

                }
                // If the program found matching videos, then display them on the side
                if (searchResults.size() > 0) {
                    reorderedPlaylist.clear(); // Otherwise, tell the user that nothing was found
                    playlistPage.clear();
                    updatePageList(searchResults, "=", false);
                    if(searchResults.contains(currentVideo)){
                        updateCurrentVideo(searchResults, "=");
                    }
                }
                else {
                    Alert noVideoAlert = new Alert(AlertType.INFORMATION);
                    noVideoAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                    noVideoAlert.setTitle("No Videos Found");
                    noVideoAlert.setHeaderText(null);
                    noVideoAlert.setContentText("No videos were found that contained \"" + videoSearchField.getText() + "\". ");
                    noVideoAlert.showAndWait();
                }
            }
        });

        // Sets up the search reset button
        searchResetButton = new Button("Reset");
        searchResetButton.setPadding(new Insets(5));
        searchResetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // Clears the video search field
                    videoSearchField.setText("");
                    
                    // Resets the search results and temproary lists
                    searchResults.clear();
                    reorderedPlaylist.clear();
                    playlistPage.clear();
                    
                    // If the playlist is randomized, then turn it back to the randomized playlist view
                    if (isRandom.isSelected()) {
                        updatePageList(randomizedPlaylist, "=", false);
                    } 
                    // Otherwise, turn it back tot he normal playlist view
                    else {
                        updatePageList(currentPlaylist.getPlaylist(), "=", false);
                    }
                    // Updates the current video to match the list
                    updateVideoPlayer("=");
                } catch (YoutubeException | IOException ex) {
                    showException(ex);
                }
            }
        });

        // Initializes the tag search check box
        isTagSearch = new CheckBox("Search for tags?");

        // Groups the video search elements into a row, and sets the row's properties
        videoSearchRow = new HBox(videoSearchLabel, videoSearchField, videoSearchButton, searchResetButton, isTagSearch);
        videoSearchRow.setAlignment(Pos.CENTER);
        videoSearchRow.setPadding(new Insets(10));
        videoSearchRow.setSpacing(5);
        videoSearchRow.setDisable(true);

        // Sets up the column that contains the playlist and video searching rows and it's properties
        searchCol = new VBox(playlistSearchRow, videoSearchRow);
        searchCol.setAlignment(Pos.CENTER);
        searchCol.setPadding(new Insets(10));
        searchCol.setSpacing(5);
        searchCol.getStyleClass().add("topbottom");
        
        // Assigns the search column to the top
        layout.setTop(searchCol);
        
        // Initializes the video progress label and the string it uses
        loadedVideoTitle = "";
        videoProgressLabel = new Label("Loading (0/0)...");
        videoProgressLabel.setId("progress");
        
        // Initializes the update progress timeline and the int it uses
        currentPosition = 0;
        updateProgress = new Timeline(new KeyFrame(Duration.millis(200), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                Platform.runLater(() -> {
                    // If there are a hundred videos loaded, start playing videos, 
                    // as it might take a while for everything to load
                    if (layout.getCenter() == videoProgressCol && currentPosition > 100){
                        try {
                            currentPlaylist.setSize(currentPlaylist.getSize());
                            resetGUI();
                        } catch (MalformedURLException | YoutubeException | FileNotFoundException ex) {
                            showException(ex);
                            
                        } catch (IOException ex) {
                            showException(ex);
                        }
                    }
                    // If there are over a hundred videos loaded,
                    // show the progress in the window title
                    if (currentPosition > 100){
                        if (loadedVideoTitle.length() > 30) {
                            primaryStage.setTitle("Playlist Manager - Loading \"" + loadedVideoTitle.substring(0, 30) + "...\" (" + currentPosition + "/" + responseListSize + ")...");
                        } else if (currentPosition >= responseListSize - 1){
                            primaryStage.setTitle("Playlist Manager");
                            updateProgress.stop();
                        } else {
                            primaryStage.setTitle("Playlist Manager - Loading \"" + loadedVideoTitle + "\" (" + currentPosition + "/" + responseListSize + ")...");
                        }
                    } 
                    // Otherwise, show the progress in the progress bar and label
                    else {
                        if (loadedVideoTitle.length() > 30) {
                            videoProgressLabel.setText("Loading \"" + loadedVideoTitle.substring(0, 30) + "...\" (" + currentPosition + "/" + responseListSize + ")...");
                        } else if (currentPosition == 0){
                            videoProgressLabel.setText("Loading (" + currentPosition + "/" + responseListSize + ")...");
                        } else {
                            videoProgressLabel.setText("Loading \"" + loadedVideoTitle + "\" (" + currentPosition + "/" + responseListSize + ")...");
                        }
                    }
                });
            }
        }));
        updateProgress.setCycleCount(Animation.INDEFINITE);
        
        // Initializes the progress bar
        videoProgress = new ProgressBar();
        videoProgress.setProgress(0);

        // Groups the video progress elements into a column, and sets the column's properties
        videoProgressCol = new VBox(videoProgressLabel, videoProgress);
        videoProgressCol.setAlignment(Pos.CENTER);
        videoProgressCol.setPadding(new Insets(10));
        videoProgressCol.setSpacing(5);
        
        // Sets up the isRandom check box
        isRandom = new CheckBox("Randomize Order?");
        isRandom.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // If the playlist has items in it, then randomize it
                if (currentPlaylist.getSize() > 0) {
                    // If the isRandom checkbox is ticked, then randomize the
                    // other list, and display it
                    if (isRandom.isSelected()) {
                        try {
                            randomizedPlaylist.remove(currentVideo);
                            // If the bias check box is checked, randomize the list with a bias
                            if (isBiased.isSelected()) {
                                // Initializes the random variable
                                Random random = new Random();
                                // Receives the playlist's size
                                int listSize = randomizedPlaylist.size() - 1;
                                
                                // Goes through each item in the list,
                                // and assign it a new position, and adjust the item's bias
                                // depending on the new position
                                for (int i = 0; i <= listSize; i++) {
                                    int newPosition = random.nextInt(listSize) - randomizedPlaylist.get(i).getPositionBias();
                                    
                                    if (newPosition < 0) {
                                        newPosition = 0;
                                    } else if (newPosition > listSize) {
                                        newPosition = listSize;
                                    } else if (newPosition > listSize / 4) {
                                        try {
                                            randomizedPlaylist.get(i).incrementBias();
                                        } catch (FileNotFoundException ex) {
                                            showException(ex);
                                        }
                                    } else {
                                        try {
                                            randomizedPlaylist.get(i).decrementBias();
                                        } catch (FileNotFoundException ex) {
                                            showException(ex);
                                            
                                        }
                                    }
                                    randomizedPlaylist.add(newPosition, randomizedPlaylist.remove(listSize - i));
                                }
                            } 
                            // Otherwise, just shuffle the playlist normally
                            else {
                                Collections.shuffle(randomizedPlaylist);
                            }
                            
                            randomizedPlaylist.add(0, currentVideo);
                            playlistPage.clear();
                            reorderedPlaylist.clear();
                            updatePageList(randomizedPlaylist, "=", false);
                            updateVideoPlayer("=");
                        } // Otherwise, display the normal list
                        catch (YoutubeException | IOException ex) {
                            showException(ex);
                        }
                    }
                    // Otherwise, that means the checkbox is unchecked,
                    // so the program returns the view back to normal
                    else {
                        try {
                            if(searchResults.size() <= 0){
                                playlistPage.clear();
                                reorderedPlaylist.clear();
                                updatePageList(currentPlaylist.getPlaylist(), "=", false);
                                updateVideoPlayer("=");
                            }
                        } catch (YoutubeException | IOException ex) {
                            showException(ex);
                            
                        }
                    }
                } 
                // Otherwise, keep the checkbox unticked
                else {
                    isRandom.setSelected(false);
                }
            }
        });

        // Initializes the bias check box
        isBiased = new CheckBox("Implement Randomization Bias?");
        isBiased.setTooltip(new Tooltip("Higher bias means it will be lower on the list\nLower Bias means it will be higher on the list"));
        
        // Initializes the playlist view and it's properties
        playlistView = new ListView<>();
        playlistView.setCellFactory(param -> new VideoCell());
        playlistView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // If the user double clicks an item, then start playing that video
                if (click.getClickCount() == 2) {
                    try {
                        currentVideo = playlistView.getSelectionModel().getSelectedItem();
                        updateVideoPlayer("=");
                        if (searchResults.size() > 0){
                            updatePageList(searchResults, "=", true);
                        } else if (isRandom.isSelected()) {
                            updatePageList(randomizedPlaylist, "=", true);
                        } else {
                            updatePageList(currentPlaylist.getPlaylist(), "=", true);
                        }
                    } catch (YoutubeException | IOException ex) {
                        showException(ex);
                        
                    }
                } 
                // Otherwise, just show the selected video's tags
                else {
                    tagField.setText(playlistView.getSelectionModel().getSelectedItem().getTags());
                }
                videoOptions.hide();
            }
        });
        playlistView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                if (videoPlayer.getVideo() == null || videoPlayer.getVideo().getStatus().equals(MediaPlayer.Status.PAUSED) || !playlistView.getSelectionModel().getSelectedItem().equals(currentVideo)){
                    playOption.setText("Play");
                } else {
                    playOption.setText("Pause");
                }
                videoOptions.show(playlistView, event.getScreenX(), event.getScreenY());
            }

        });
        
        // Initializes the label that shows the page number
        pageNumberLabel = new Label("Page 1/1");
        
        // Initializes the first page button and it's properties
        firstPageButton = new Button("<<");
        firstPageButton.setAlignment(Pos.CENTER_LEFT);
        firstPageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchResults.size() > 0){
                    updatePageList(searchResults, firstPageButton.getText(), true);
                } else if (isRandom.isSelected()) {
                    updatePageList(randomizedPlaylist, firstPageButton.getText(), true);
                } else {
                    updatePageList(currentPlaylist.getPlaylist(), firstPageButton.getText(), true);
                }

            }
        });

        // Initializes the previous page button and it's properties
        previousPageButton = new Button("<");
        previousPageButton.setAlignment(Pos.CENTER_LEFT);
        previousPageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchResults.size() > 0){
                    updatePageList(searchResults, previousPageButton.getText(), true);
                } else if (isRandom.isSelected()) {
                    updatePageList(randomizedPlaylist, previousPageButton.getText(), true);
                } else {
                    updatePageList(currentPlaylist.getPlaylist(), previousPageButton.getText(), true);
                }
            }
        });

        // Initializes the next page button and it's properties
        nextPageButton = new Button(">");
        nextPageButton.setAlignment(Pos.CENTER_RIGHT);
        nextPageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchResults.size() > 0){
                    updatePageList(searchResults, nextPageButton.getText(), true);
                } else if (isRandom.isSelected()) {
                    updatePageList(randomizedPlaylist, nextPageButton.getText(), true);
                } else {
                    updatePageList(currentPlaylist.getPlaylist(), nextPageButton.getText(), true);
                }
            }
        });

        // Initializes the last page button and it's properties
        lastPageButton = new Button(">>");
        lastPageButton.setAlignment(Pos.CENTER_RIGHT);
        lastPageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchResults.size() > 0){
                    updatePageList(searchResults, lastPageButton.getText(), true);
                } else if (isRandom.isSelected()) {
                    updatePageList(randomizedPlaylist, lastPageButton.getText(), true);
                } else {
                    updatePageList(currentPlaylist.getPlaylist(), lastPageButton.getText(), true);
                }
            }
        });

        // Groups the page elements into a row, and sets the row's properties
        pageRow = new HBox(firstPageButton, previousPageButton, pageNumberLabel, nextPageButton, lastPageButton);
        pageRow.setAlignment(Pos.CENTER);
        pageRow.setPadding(new Insets(10));
        pageRow.setSpacing(5);

        // Groups all of the playlist view-related elements into a column, and sets the column's properties
        playlistInterface = new VBox(isRandom, isBiased, playlistView, pageRow);
        playlistInterface.setPrefWidth(scene.getWidth() / 3);
        playlistInterface.setPadding(new Insets(10));
        playlistInterface.setSpacing(5);
        playlistInterface.setDisable(true);
        playlistInterface.getStyleClass().add("middle");
        
        // Initializes the video settings button and it's properties
        videoSettingsButton = new Button();
        videoSettingsButton.setGraphic(videoSettingsIcon);
        videoSettingsButton.setId("interface");
        videoSettingsButton.setMinSize(23, 23);
        videoSettingsButton.setPrefSize(23, 23);
        videoSettingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showVideoProperties(currentVideo);
            }
        });
        
        // Initializes the previous video button for the video player and it's properties
        previousVideoButton = new Button();
        previousVideoButton.setGraphic(previousVideoIcon);
        previousVideoButton.setId("interface");
        previousVideoButton.setMinSize(23, 23);
        previousVideoButton.setPrefSize(23, 23);
        previousVideoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    updateVideoPlayer("<");
                    if (!currentPageList.contains(currentVideo)){
                        // If the playlist is randomized, then turn it back to the randomized playlist view
                        if (searchResults.size() > 0){
                            updatePageList(searchResults, "<", false);
                        } else if (isRandom.isSelected()) {
                            updatePageList(randomizedPlaylist, "<", false);
                        } else {
                            updatePageList(currentPlaylist.getPlaylist(), "<", false);
                        }
                    } else {
                        if (searchResults.size() > 0){
                            updatePageList(searchResults, "=", false);
                        } else if (isRandom.isSelected()) {
                            updatePageList(randomizedPlaylist, "=", false);
                        } else {
                            updatePageList(currentPlaylist.getPlaylist(), "=", false);
                        }
                    }
                } catch (YoutubeException | IOException ex) {
                    showException(ex);
                    
                }
            }
        });
        
        // Initializes the next video button for the video player and it's properties
        nextVideoButton = new Button();
        nextVideoButton.setGraphic(nextVideoIcon);
        nextVideoButton.setId("interface");
        nextVideoButton.setMinSize(23, 23);
        nextVideoButton.setPrefSize(23, 23);
        nextVideoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    updateVideoPlayer(">");
                    if (!currentPageList.contains(currentVideo)){
                        // If the playlist is randomized, then turn it back to the randomized playlist view
                        if (searchResults.size() > 0){
                            updatePageList(searchResults, ">", false);
                        } else if (isRandom.isSelected()) {
                            updatePageList(randomizedPlaylist, ">", false);
                        } else {
                            updatePageList(currentPlaylist.getPlaylist(), ">", false);
                        }
                    } else {
                        if (searchResults.size() > 0){
                            updatePageList(searchResults, "=", false);
                        } else if (isRandom.isSelected()) {
                            updatePageList(randomizedPlaylist, "=", false);
                        } else {
                            updatePageList(currentPlaylist.getPlaylist(), "=", false);
                        }
                    }
                } catch (YoutubeException | IOException ex) {
                    showException(ex);
                    
                }
            }
        });
        
        // Initializes the video player and it's properties
        // Also adds the video buttons to the interface
        videoPlayer = new VideoInterface();
        videoPlayer.maxWidthProperty().bind(layout.widthProperty());
        videoPlayer.setManaged(false);
        videoPlayer.getMediaBar().getChildren().add(videoPlayer.getMediaBar().getChildren().size(), videoSettingsButton);
        videoPlayer.getMediaBar().getChildren().add(0, previousVideoButton);
        videoPlayer.getMediaBar().getChildren().add(2, nextVideoButton);
        
        // Assigns the video player to the center
        layout.setCenter(videoPlayer.getLayout());
        layout.widthProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                double newWidth = (double) newValue;
                videoPlayer.autosize();
                playlistInterface.setPrefWidth(newWidth / 3);
                playlistInterface.autosize();
            }
        });

        layout.heightProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                double newHeight = (double) newValue;
                tagRow.setPrefHeight(newHeight / 12);
            }
        });

        // Assigns the playlist interface to the right
        layout.setRight(playlistInterface);

        // Sets up the tag elements
        tagLabel = new Label("Tags");
        tagField = new TextField();
        tagField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                if (!event.getCode().equals(KeyCode.SHIFT) && !event.getCode().equals(KeyCode.ENTER)) {
                    searchSuggestions.getItems().clear();
                    String[] userText = tagField.getText().split(",");
                    userText[userText.length - 1] += event.getCode().toString();
                    userText[userText.length - 1] = userText[userText.length - 1].trim();

                    ArrayList<MenuItem> suggestedTags = new ArrayList<>();

                    for (String tag : currentPlaylist.getTags().split(",")) {

                        if (!userText[userText.length - 1].isEmpty() && tag.trim().toLowerCase().startsWith(userText[userText.length - 1].toLowerCase()) && tag.trim().compareToIgnoreCase(userText[userText.length - 1]) != 0) {
                            MenuItem item = new MenuItem(tag);
                            item.setOnAction(new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent event) {
                                    if (userText.length > 1) {
                                        userText[userText.length - 1] = tag;
                                        tagField.setText("");
                                        for (String newTag : userText) {
                                            if (newTag.equals(userText[userText.length - 1])) {
                                                tagField.setText(tagField.getText() + newTag.trim());
                                            } else {
                                                tagField.setText(tagField.getText() + newTag.trim() + ", ");
                                            }
                                        }
                                    } else {
                                        tagField.setText(tag.trim());
                                    }
                                    tagField.positionCaret(tagField.getText().length());
                                }
                            });
                            suggestedTags.add(item);
                        }
                    }
                    searchSuggestions.getItems().addAll(FXCollections.observableArrayList(suggestedTags));
                    searchSuggestions.show(tagField, Side.BOTTOM, 0, 0);
                } else if (event.getCode().equals(KeyCode.ENTER)){
                    videoSearchButton.fire();
                }
            }
        });

        // Initializes the set tag button and it's properties
        setTagsButton = new Button("Set Tags");
        setTagsButton.setPadding(new Insets(5));
        setTagsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // Displays the current video's tags
                    playlistView.getSelectionModel().getSelectedItem().setTags(tagField.getText());
                    currentPlaylist.addTags(tagField.getText());
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    showException(ex);
                }
            }
        });

        // Puts all of the tag variables into a row
        tagRow = new HBox(tagLabel, tagField, setTagsButton);
        tagRow.setPrefHeight(layout.getHeight() / 12);
        tagRow.setAlignment(Pos.CENTER);
        tagRow.setPadding(new Insets(10));
        tagRow.setSpacing(5);
        tagRow.setDisable(true);
        tagRow.getStyleClass().add("topbottom");
        
        // Assigns the tag row to the bottom
        layout.setBottom(tagRow);
    }
    
    /**
     * This method uses the given playlist name and playlist object obtained from the Youtube API,
     * and uses them to load up the saved playlist from the files into an Array List.
     * 
     * @param playlistName - The name of the playlist to be loaded
     * @param playlist - The playlist object obtained from the Youtube API
     * 
     * @throws IOException
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     * @throws GoogleJsonResponseException
     * @throws ClassNotFoundException 
     */
    protected void loadVideoFromFile(String playlistName, Playlist playlist) throws IOException, FileNotFoundException, GeneralSecurityException, GoogleJsonResponseException, ClassNotFoundException {
        // The variables
        File playlistFolder = new File(playlistPath, playlistName.replace(":", "_").replace("?", "_").replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>]|[.]{2,}$|[.]$", "").trim());
        ArrayList<String> videoList = new ArrayList<>(Arrays.asList(playlistFolder.list()));
        videoList.remove("playlist.txt");
        int listSize = videoList.size();
        responseListSize = playlist.getContentDetails().getItemCount().intValue();
        int offset = 0;
        
        // If the playlist's folder exists, then go through each folder within it,
        // and load the videos from each folder.
        if(playlistFolder.exists()){
            for (int i = 0; i < listSize; i++) {
                String videoName = videoList.get(i);

                // If the video name is not empty, and is not already in the list,
                // and does not equal "playlist.txt", "Deleted Video", or "Private Video",
                // then load the video from the folder into the playlist
                if (!videoName.isEmpty() && !videoName.equals("") && !currentPlaylist.contains(videoName) && !videoName.equals("playlist.txt") && !videoName.equalsIgnoreCase("Deleted Video") &&!videoName.equalsIgnoreCase("Private video")) {
                    PlaylistVideo newVideo = new PlaylistVideo(playlistFolder.getCanonicalPath(), videoName);
                    // If the video was unable to be loaded, then delete the folder
                    if (newVideo.getVideoName() == null){
                        newVideo.deleteFolder(new File(newVideo.getFolderPath()));
                        videoList.remove(videoName);
                        i--;
                        listSize--;
                        responseListSize--;
                        offset++;
                    } 
                    // Otherwise, load the video as normal
                    else {
                        try {
                            currentPlaylist.addVideo(newVideo);
                        } catch (NullPointerException ex){
                            System.out.println(videoName + " Path: " + newVideo.getVideoPath());
                            showException(ex);
                        }
                        loadedVideoTitle = videoName;
                        currentPosition = i;
                        maxListSize = listSize;
                        videoProgress.setProgress(((double) currentPosition) / listSize);
                    }
                }
            }
            Collections.sort(currentPlaylist.getPlaylist());
        
            currentPlaylist.setSize(currentPlaylist.getSize());
        }
        
        // Obtains the playlist from the Youtube API just in case the saved playlist
        // does not have the same videos
        List<PlaylistItem> responseList = retrievePlaylist(currentPlaylist.getPlaylistID());
        // If the playlist folder does not exist or the loaded playlist is smaller than the Youtube playlist,
        // then load the new videos from the URLs.
        if (!playlistFolder.exists() || responseListSize > listSize){
            // For each item in the list, construct a Video object based on the properties in the video
            for (int i = 0; i < responseListSize; i++) {
                // The variables
                PlaylistItem item;
                if (i >= responseList.size()){
                    i = responseList.size();
                } else {
                    if (i >= i - offset){
                        item = responseList.get(i - offset);
                    } else {
                        item = responseList.get(i);
                    }
                    String videoName = item.getSnippet().getTitle();
                    String filteredVideoName = videoName.replace(":", "_").replace("?", "_").replaceAll("[/\\\\]", "&").replaceAll("[:\"|*<>]|[.]{2,}$|[.]$", "").trim();

                    // If the video name is not empty, and is not already in the list,
                    // and does not equal "Deleted Video" or "Private Video",
                    // then load the video from the folder into the playlist
                    if (!videoList.contains(filteredVideoName) && !videoName.equalsIgnoreCase("Deleted Video") &&!videoName.equalsIgnoreCase("Private video") && !videoName.isEmpty() && !videoName.equals("")) {
                        loadVideoFromURL(item, playlistFolder.getCanonicalPath(), videoName, filteredVideoName);
                        videoList = new ArrayList<>(Arrays.asList(playlistFolder.list()));
                        loadedVideoTitle = videoName;
                        currentPosition = i;
                        maxListSize = listSize;
                        videoProgress.setProgress(((double) currentPosition) / listSize);
                    }
                }
                
            }
            currentPlaylist.setSize(currentPlaylist.getSize());
        }
        Collections.sort(currentPlaylist.getPlaylist());
        
        // Checks the playlist for any videos with the same position,
        // and loads them into a new list
        int oldEqualStreak = 0;
        int equalStreak = 0;
        ArrayList<PlaylistVideo> duplicateList = new ArrayList<>();
        for (int i = 0; i < currentPlaylist.getSize(); i++){
            PlaylistVideo comparedVid = currentPlaylist.getVideo(i);
            for (int j = 0; j < currentPlaylist.getSize(); j++){
                PlaylistVideo vid = currentPlaylist.getVideo(j);
                if (!comparedVid.equals(vid) && comparedVid.getPosition() == vid.getPosition()){
                    if (!duplicateList.contains(comparedVid)){
                        duplicateList.add(comparedVid);
                    }
                    if (!duplicateList.contains(vid)){
                        duplicateList.add(vid);
                    }
                    oldEqualStreak++;
                    equalStreak++;
                } else {
                    equalStreak = 0;
                }
                if (oldEqualStreak != equalStreak){
                    oldEqualStreak = 0;
                    j = currentPlaylist.getSize();
                }
            }
        }
        
        // If any position duplicates in the list, 
        // update the positions of the affected videos
        if (duplicateList.size() > 0){
            for (int i = 0; i < duplicateList.size(); i++){
                PlaylistVideo vid = duplicateList.get(i);
                PlaylistItem item = responseList.get(i);
                if (!item.getSnippet().getTitle().equals(vid.getVideoName())){
                    for (int j = 0; j < responseList.size(); j++){
                        item = responseList.get(j);
                        if (item.getSnippet().getTitle().equals(vid.getVideoName())){
                            j = responseList.size();
                        }
                    }
                }
                
                int position = item.getSnippet().getPosition().intValue();
                currentPlaylist.getVideo(currentPlaylist.indexOf(vid)).setPosition(position);
            }
        }
        Collections.sort(currentPlaylist.getPlaylist());
    }
    
    /**
     * This method uses the given playlist item, path, and video names to create
     * a new folder for the video, and loads the video into the playlist
     * 
     * @param item - The Video to be loaded
     * @param path - The path of the playlist
     * @param videoName - The name of the video
     * @param filteredVideoName - The filtered name of the video
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws GoogleJsonResponseException
     * @throws ClassNotFoundException 
     */
    protected void loadVideoFromURL(PlaylistItem item, String path, String videoName, String filteredVideoName) throws IOException, GeneralSecurityException, GoogleJsonResponseException, ClassNotFoundException {
        
        // The path of the video's properties file
        File tempFile = new File(path + "/" + filteredVideoName, "video.txt");

        // If the file exists, then load the video from that file
        if (tempFile.exists()) {
            PlaylistVideo newVid = new PlaylistVideo(path, videoName);
            if (newVid.getVideoName() == null){
                newVid.deleteFolder(new File(newVid.getFolderPath()));
            } else {
                currentPlaylist.addVideo(newVid);
            }
        } 
        // Otherwise, load the video from it's URL
        else{
            String videoID = item.getContentDetails().getVideoId();
            List<Video> foundVideos = YoutubeAPI.requestVideo(videoID).getItems();
            
            // If the video was able to be loaded, then load it into a new PlaylistVideo object
            if (foundVideos.size() > 0){
                Video video = foundVideos.get(0);

                String description = video.getSnippet().getDescription();
                String channel = video.getSnippet().getChannelTitle();
                String thumbnailURL = video.getSnippet().getThumbnails().getMedium().getUrl();
                int thumbnailWidth = video.getSnippet().getThumbnails().getMedium().getWidth().intValue();
                int thumbnailHeight = video.getSnippet().getThumbnails().getMedium().getHeight().intValue();
                int position = item.getSnippet().getPosition().intValue();
                
                // Adds the playlist items into the playlist as a Video object
                PlaylistVideo newVid = new PlaylistVideo(path, videoID, videoName, description, channel, thumbnailURL, thumbnailWidth, thumbnailHeight, position);
                currentPlaylist.addVideo(newVid);
            }
        }
        
    }
    
    /**
     * This method retrieves the playlist from the Youtube API with the given ID
     * 
     * @param playlistID - The Playlist's ID
     * 
     * @return The YoutubeAPI's response list
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws GoogleJsonResponseException
     * @throws ClassNotFoundException
     */
    protected List<PlaylistItem> retrievePlaylist(String playlistID) throws GeneralSecurityException, IOException, GoogleJsonResponseException, ClassNotFoundException {
        
        // Obtains the playlist information from the Youtube API
        PlaylistItemListResponse playlistItemResponse = YoutubeAPI.requestPlaylistItems(playlistID);

        if (playlistItemResponse == null){
            return null;
        }
        
        // Loads all of the videos obtained from the playlist into a List
        List<PlaylistItem> responseList = playlistItemResponse.getItems();
        String nextPageToken = playlistItemResponse.getNextPageToken();

        // Goes through each page of the list and loads the items into the response list
        while (nextPageToken != null) {
            playlistItemResponse = YoutubeAPI.requestNextPlaylistItems(playlistID, nextPageToken);
            responseList.addAll(playlistItemResponse.getItems());
            nextPageToken = playlistItemResponse.getNextPageToken();
        }
        
        return responseList;
    }
    
    /**
     * Creates two snapshots of the current playlist, one from the playlist view,
     * and the other from the normal playlist.
     */
    protected void makePlaylistSnapshots() {
        int startingPoint = 0 + pageSizeLimit * (pageNumber - 1);
        int maxSize = pageSizeLimit * pageNumber;
        
        // Loads up to 50 items from the currently loaded playlist into another list,
        // depending on the current page
        playlistPage.clear();
        for (int i = startingPoint; i < maxSize; i++) {
            if (searchResults.size() > 0){
                playlistPage.add(searchResults.get(i));
                if(i + 1 >= searchResults.size()){
                    i = maxSize;
                }
            } else if (i < randomizedPlaylist.size() && isRandom.isSelected()){
                playlistPage.add(randomizedPlaylist.get(i));
            } else if (i < currentPlaylist.getSize() && !currentPlaylist.getPlaylist().isEmpty()){
                playlistPage.add(currentPlaylist.getVideo(i));
            }
        }
        
        // Loads every video that is in the playlist video and loads it into another list
        reorderedPlaylist.clear();
        for (int j = 0; j < playlistView.getItems().size(); j++){
            if(searchResults.size() > 0 && j >= searchResults.size()){
                j = playlistView.getItems().size();
            } else if (j > playlistView.getItems().size()) {
                j = playlistView.getItems().size();
            } else {
                reorderedPlaylist.add(playlistView.getItems().get(j));
            }
        }
    }
    
    /**
     * Updates the current video player with the next video
     * 
     * @param direction - Whether the program is going to the next, previous, or current video
     * 
     * @throws YoutubeException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    protected void updateVideoPlayer(String direction) throws YoutubeException, FileNotFoundException, IOException{
        
        // If the playlist is overriding the video's settings,
        // then use it's save property to check if the video should be deleted
        if (currentPlaylist.getOverrideSettings()){
            if (currentVideo.getVideoPath() != null && !currentPlaylist.getIsSaved()){
                videoPlayer.setVideo(null);
                currentVideo.deleteVideo();
            }
        } 
        // Otherwise, use the current video's save property to check if the video should be deleted
        else {
            if (currentVideo.getVideoPath() != null && !videoPlayer.getVideo().getMedia().getSource().equals(currentVideo.getVideoPath().replace(" ", "%20")) && !currentVideo.getIsSaved()){
                videoPlayer.setVideo(null);
                currentVideo.deleteVideo();
            }
        }
        
        makePlaylistSnapshots();
        
        // If there is only one video in the list
        boolean onlyVideoCheck = false;
        
        // Depending on the current playlist, update the current video
        if (!reorderedPlaylist.isEmpty() && !playlistPage.equals(reorderedPlaylist)){
            updateCurrentVideo(reorderedPlaylist, direction);
            onlyVideoCheck = reorderedPlaylist.size() == 1 && reorderedPlaylist.get(0).equals(currentVideo);
        } else if (!searchResults.isEmpty()){
            updateCurrentVideo(searchResults, direction);
            onlyVideoCheck = searchResults.size() == 1 && searchResults.get(0).equals(currentVideo);
        } else if (!randomizedPlaylist.isEmpty() && isRandom.isSelected()){
            updateCurrentVideo(randomizedPlaylist, direction);
            onlyVideoCheck = randomizedPlaylist.size() == 1 && randomizedPlaylist.get(0).equals(currentVideo);
        } else if (!currentPlaylist.getPlaylist().isEmpty()){
            updateCurrentVideo(currentPlaylist.getPlaylist(), direction);
            onlyVideoCheck = currentPlaylist.getSize() == 1 && currentPlaylist.getVideo(0).equals(currentVideo);
        }
        
        // If the video is the only one, or there is no video playing, or the video player's video doesn't equal the current video,
        // then load the current video into the video player, and update the tag row
        if (onlyVideoCheck || videoPlayer.getVideo() == null || !videoPlayer.getVideo().getMedia().getSource().equals(currentVideo.getVideoPath().replace(" ", "%20"))){
            
            if (videoPlayer.getVideo() != null){
                videoPlayer.setVideo(null);
            }
            
            tagField.setText(currentVideo.getTags());
            
            MediaPlayer newPlayer;
            try {
                newPlayer = new MediaPlayer(new Media(downloadVideo(currentVideo)));

                // If the playlist is overriding the video's settings,
                // then use it's speed property to set the video's speed
                if (currentPlaylist.getOverrideSettings() && currentPlaylist.getIsSpeedChanged() && !currentVideo.getIsSpeedChanged()){
                    newPlayer.setRate(currentPlaylist.getSpeed());
                } 
                // Otherwise, use the video's speed property to set the video's speed
                else if (currentVideo.getIsSpeedChanged()){
                    newPlayer.setRate(currentVideo.getSpeed());
                }

                // If the playlist is overriding the video's settings,
                // then use it's speed property to set the video's speed
                if (currentPlaylist.getOverrideSettings() && currentPlaylist.getIsLooping() & !currentVideo.getIsLooping()){
                    if (currentPlaylist.getLoopCount() == -1){
                        newPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    } else {
                        newPlayer.setCycleCount(currentPlaylist.getLoopCount());
                    }
                } 
                // Otherwise, use the video's loop property to set the video's loop count
                else if (currentVideo.getIsLooping()){
                    if (currentVideo.getLoopCount() == -1){
                        newPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                    } else {
                        newPlayer.setCycleCount(currentVideo.getLoopCount());
                    }
                }

                newPlayer.setAutoPlay(true);
                newPlayer.currentTimeProperty().addListener(new InvalidationListener(){
                    @Override
                    public void invalidated(Observable ov) {
                        videoPlayer.updateValues();
                    }
                });
                newPlayer.setOnReady(new Runnable() {
                    @Override
                    public void run() {
                        videoPlayer.setDuration(newPlayer.getMedia().getDuration());
                        videoPlayer.updateValues();
                    }
                });  
                newPlayer.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        // If the video has a loop count greater than one,
                        // then go back to the beginning and decrease the video's loop count
                        if (videoPlayer.getVideo().getCycleCount() > 1) {
                            videoPlayer.getVideo().seek(videoPlayer.getVideo().getStartTime());
                            videoPlayer.getVideo().setCycleCount(newPlayer.getCycleCount() - 1);
                        } 
                        // Otherwise, if the video's loop count is infinite,
                        // then just go back to the beginning
                        else if (videoPlayer.getVideo().getCycleCount() == MediaPlayer.INDEFINITE) {
                            videoPlayer.getVideo().seek(videoPlayer.getVideo().getStartTime());
                        } 
                        // Otherwise, load the next video
                        else {
                            try {
                                updateVideoPlayer(">");
                                // If the playlist doesn't contain the new video, 
                                // that means it is on the next page,
                                // so update the page list to go to the next page
                                if (!currentPageList.contains(currentVideo)){
                                    // If the playlist is randomized, then turn it back to the randomized playlist view
                                    if (searchResults.size() > 0){
                                        updatePageList(searchResults, ">", false);
                                    } else if (isRandom.isSelected()) {
                                        updatePageList(randomizedPlaylist, ">", false);
                                    } else {
                                        updatePageList(currentPlaylist.getPlaylist(), ">", false);
                                    }
                                } 
                                // Otherwise, keep the page list on the same page
                                else {
                                    if (searchResults.size() > 0){
                                        updatePageList(searchResults, "=", false);
                                    } else if (isRandom.isSelected()) {
                                        updatePageList(randomizedPlaylist, "=", false);
                                    } else {
                                        updatePageList(currentPlaylist.getPlaylist(), "=", false);
                                    }
                                }
                            } catch (YoutubeException | IOException ex) {
                                showException(ex);
                            }
                        }
                    }
                });
                videoPlayer.setVideo(newPlayer);

                if (isBiased.isSelected()) {
                    currentVideo.decrementBias();
                }

            } catch (IllegalArgumentException ex){
                showException(ex);
            }
        }
        
    }
    
    /**
     * Updates the current video from the list, depending on the direction.
     * @param list - The currently loaded playlist
     * @param direction - If the current video should be the previous, next, or same video
     */
    protected void updateCurrentVideo(ArrayList<PlaylistVideo> list, String direction){
        // If the list is the reordered playlist, 
        // then update the current video based on the order of that list
        if (list.equals(reorderedPlaylist)){
            switch (direction){
                case "<":
                    if(list.indexOf(currentVideo) == 0){
                        currentVideo = list.get(0);
                    } else if (list.indexOf(currentVideo) - 1 < 0){
                        if (searchResults.size() > 0 && searchResults.indexOf(currentVideo) - 1 > 0){
                            currentVideo = searchResults.get(pageSizeLimit * (pageNumber - 1) - 1);
                        } else if (isRandom.isSelected() && randomizedPlaylist.indexOf(currentVideo) - 1 > 0){
                            currentVideo = randomizedPlaylist.get(pageSizeLimit * (pageNumber - 1) - 1);
                        } else if (currentPlaylist.indexOf(currentVideo) - 1 >= 0){
                            currentVideo = currentPlaylist.getVideo(pageSizeLimit * (pageNumber - 1) - 1);
                        } else {
                            currentVideo = list.get(0);
                        }
                    } else {
                        currentVideo = list.get(list.indexOf(currentVideo) - 1);
                    }
                    break;
                case ">": 
                    if (list.indexOf(currentVideo) + 1 >= list.size()){
                        if (searchResults.size() > 0 && searchResults.indexOf(currentVideo) + 1 < searchResults.size()){
                            currentVideo = searchResults.get(pageSizeLimit * (pageNumber));
                        } else if (isRandom.isSelected() && randomizedPlaylist.indexOf(currentVideo) + 1 < randomizedPlaylist.size()){
                            currentVideo = randomizedPlaylist.get(pageSizeLimit * (pageNumber));
                        } else if (list.indexOf(currentVideo) == 49 && currentPlaylist.indexOf(currentVideo) + 1 < currentPlaylist.getSize()){
                            currentVideo = currentPlaylist.getVideo(pageSizeLimit * (pageNumber));
                        } else {
                            currentVideo = list.get(0);
                        }
                    } else {
                        currentVideo = list.get(list.indexOf(currentVideo) + 1);
                    }
                    break;
                case "0":
                    currentVideo = list.get(0);
                    break;
                default:
                    if (list.contains(currentVideo)){
                        currentVideo = list.get(list.indexOf(currentVideo));
                    } else {
                        currentVideo = list.get(0);
                    }
                    break;
            }
        } 
        // Otherwise, update thc urrent video based on the given list
        else {
            switch (direction){
                case "<":
                    if (list.indexOf(currentVideo) - 1 < 0){
                        currentVideo = list.get(0);
                    } else {
                        currentVideo = list.get(list.indexOf(currentVideo) - 1);
                    }
                    break;
                case ">": 
                    if (list.indexOf(currentVideo) + 1 >= list.size()){
                        currentVideo = list.get(0);
                    } else {
                        currentVideo = list.get(list.indexOf(currentVideo) + 1);
                    }
                    break;
                case "0":
                    currentVideo = list.get(0);
                    break;
                default:
                    if (list.contains(currentVideo)){
                        currentVideo = list.get(list.indexOf(currentVideo));
                    } else {
                        currentVideo = list.get(0);
                    }
                    break;
            }
        }
    }
    
    /**
     * Displays the given error to the user in an alert window,
     * and prints out the details to the console
     * @param ex 
     */
    protected void showException(Throwable ex){
        Platform.runLater(() -> {
            String exceptionType;
            // Sets the exception type depending on it's contents
            if(ex == null){
                exceptionType = "Unknown";
            } else if (ex.toString().indexOf(":") > 0){
                exceptionType = ex.toString().substring(0, ex.toString().indexOf(":"));
            } else {
                exceptionType = ex.toString().substring(0);
            }
            
            // Initializes the exception alert and it's properties
            Alert exceptionAlert = new Alert(AlertType.ERROR);
            exceptionAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
            exceptionAlert.setTitle("Exception Dialog");
            exceptionAlert.setHeaderText("Error: " + exceptionType);

            // Depending on the exception type, set the content of the alert
            switch (exceptionType){
                case "com.google.api.client.googleapis.json.GoogleJsonResponseException":
                    exceptionAlert.setContentText("Unable to retrieve videos from URL. Loading videos from File instead.");
                    break;
                case "java.lang.NullPointerException":
                    exceptionAlert.setContentText("Unable to set video after " + currentPlaylist.getVideo(currentPlaylist.getSize() - 1));
                default:
                    exceptionAlert.setContentText("An exception has occured.");
                    break;
            }

            // Creates an expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            VBox errorCol = new VBox(label, textArea);
            errorCol.setMaxWidth(Double.MAX_VALUE);
            exceptionAlert.getDialogPane().setExpandableContent(errorCol);

            // Prints the error to the console using the Logger
            Logger.getLogger(PlaylistManager.class.getName()).log(Level.SEVERE, null, ex);
            exceptionAlert.showAndWait();
        });
    }
    
    /**
     * Disables the GUI when loading the playlist
     */
    protected void disableGUI(){
        // Resets the variables for the progress bar
        currentPosition = 0;
        maxListSize = 0;
        loadedVideoTitle = "";
        videoProgress.setProgress(0);

        // Disables some GUI elements
        playlistView.getItems().clear();
        playlistInterface.setDisable(true);
        videoSearchRow.setDisable(true);

        // Resets the checkboxes
        isTagSearch.setSelected(false);
        isRandom.setSelected(false);
        isBiased.setSelected(false);
        layout.setCenter(videoProgressCol);

        // If there is a video loaded, umload it
        if (videoPlayer.getVideo() != null){
            videoPlayer.setVideo(null);
        }
        updateProgress.play();
    }
    
    /**
     * Displays the properties of the given video in a new Alert
     * @param video - The video to be displayed
     */
    protected void showVideoProperties(final PlaylistVideo video){
        // If the given video exists, display the settings
        if (video != null){
            
            // Initializes the properties alert and it's properties
            Alert propertiesAlert = new Alert(AlertType.INFORMATION);
            propertiesAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
            propertiesAlert.setTitle("Properties");
            propertiesAlert.setHeaderText(null);
            propertiesAlert.setWidth(600);
            propertiesAlert.setHeight(400);

            // Loads the video's properties into variables for later use
            String videoName = video.getVideoName();
            String videoID = video.getVideoID();
            String videoChannel = video.getChannel();
            String videoDescription = video.getDescription();
            int videoPosition = video.getPosition();
            int videoPositionBias = video.getPositionBias();
            String videoTags = video.getTags();
            
            // Initializes the labels that show some of the video's properties
            Label topText = new Label("Name: " + videoName + "\nChannel: " + videoChannel);
            Label urlLabel = new Label("Video URL: ");

            // Creates a link to the video
            Hyperlink videoURL = new Hyperlink("https://www.youtube.com/watch?v=" + videoID);
            videoURL.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    // If the used button was the left mouse button and the user has a browser,
                    // then open the video in the user's default browser
                    if (event.getButton().equals(MouseButton.PRIMARY) && Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(new URI(videoURL.getText()));
                        } catch (URISyntaxException | IOException ex) {
                            showException(ex);
                            
                        }
                        videoURL.setVisited(false);
                    }
                }
            });
            
            // Creates an options to copy the video URL into the user's clipboard
            MenuItem copyOption = new MenuItem("Cooy");
            copyOption.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();

                    content.putString(videoURL.getText());
                    clipboard.setContent(content);
                }
            });
            
            // Creates a context menu that will appear when the user right clicks the hyperlink, 
            // which will have the option to copy the URL
            ContextMenu copyMenu = new ContextMenu(copyOption);
            videoURL.setContextMenu(copyMenu);
                
            // Groups the URL elements into a row, and sets the row's properties
            HBox urlRow = new HBox(urlLabel, videoURL);
            urlRow.setSpacing(5);
            urlRow.setAlignment(Pos.CENTER_LEFT);
            
            // Initializes the loop field
            TextField loopField = new TextField(video.getLoopCount() + "");
            
            // Initializes the checkbox that enables the looping of the video
            CheckBox isLooping = new CheckBox("Loop Video?");
            isLooping.setSelected(video.getIsLooping());
            isLooping.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    try {
                        video.setIsLooping(isLooping.isSelected());
                        // Resets the video's looping properties and the loop field if the checkbox is unchecked
                        if (!isLooping.isSelected()){
                            video.setLoopCount(1);
                            if (video.equals(currentVideo)){
                                videoPlayer.getVideo().setCycleCount(1);
                            }
                            loopField.setText(video.getLoopCount() + "");
                        }
                    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        showException(ex);
                        
                    }
                }

            });
            isLooping.setTooltip(new Tooltip("Any value above -1 works, with -1 acting as Infinity."));

            // Initializes the button that sets the video's loop count
            Button setLoopButton = new Button("Set Loop Count");
            
            // If the user presses the enter key while in the field, then activate the loop button
            loopField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode().equals(KeyCode.ENTER)){
                        setLoopButton.fire();
                    }
                }
            });
            
            setLoopButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent buttonEvent) {
                    try {
                        int loopValue = Integer.parseInt(loopField.getText());
                        // If the value is a negative number, just set the loop count to infinite
                        if (loopValue < 0){
                            video.setLoopCount(Integer.parseInt(loopField.getText()));
                            if (video.equals(currentVideo)){
                                videoPlayer.getVideo().setCycleCount(MediaPlayer.INDEFINITE);
                            }
                        } 
                        // Otherwise, set the loop count normally
                        else {
                            video.setLoopCount(Integer.parseInt(loopField.getText()));
                            if (video.equals(currentVideo)){
                                videoPlayer.getVideo().setCycleCount(video.getLoopCount());
                            }
                        }
                        loopField.setText(video.getLoopCount() + "");
                    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        showException(ex);
                    }
                }
            });

            // Imitializes the reset loop count button
            Button resetLoopCountButton = new Button("Reset");
            resetLoopCountButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent buttonEvent) {
                    try {
                        video.setLoopCount(1);
                        if (video.equals(currentVideo)){
                            videoPlayer.getVideo().setCycleCount(1);
                        }
                        loopField.setText("1");
                    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        showException(ex);
                        
                    }
                }
            });
            
            // Groups the loop elements into a row, and sets the row's properties
            HBox loopRow = new HBox(loopField, setLoopButton, resetLoopCountButton);
            loopRow.setSpacing(5);
            loopRow.disableProperty().bind(isLooping.selectedProperty().not());

            // Initializes the set speed button
            Button setSpeedButton = new Button("Set Speed");
            
            // Initializes the speed field
            TextField speedField = new TextField(String.format("%.0f", video.getSpeed() * 100) + "%");
            
            // If the user presses the enter key while in the speed field, then activate the speed button
            speedField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode().equals(KeyCode.ENTER)){
                        setSpeedButton.fire();
                    }
                }
            });
            
            // Initializes the speed checkbox and it's properties
            CheckBox isSpeedChanged = new CheckBox("Change Video Speed?");
            isSpeedChanged.setSelected(video.getIsSpeedChanged());
            isSpeedChanged.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    try {
                        video.setIsSpeedChanged(isSpeedChanged.isSelected());
                        // If the speed checkbox gets unselected, then reset the field and video's speed values
                        if (!isSpeedChanged.isSelected()){
                            video.setSpeed(1);
                            if (video.equals(currentVideo)){
                                videoPlayer.getVideo().setRate(1);
                            }
                            speedField.setText(String.format("%.0f", video.getSpeed() * 100) + "%");
                        }
                    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        showException(ex);
                    }
                }

            });
            isSpeedChanged.setTooltip(new Tooltip("Minimum is 50%, Maximum is 300%"));
            
            setSpeedButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent buttonEvent) {
                    try {
                        double speedValue = Double.parseDouble(speedField.getText().replace("%", "")) / 100;
                        video.setSpeed(speedValue);
                        if (video.equals(currentVideo)){
                            videoPlayer.getVideo().setRate(video.getSpeed());
                        }
                        speedField.setText(String.format("%.0f", video.getSpeed() * 100) + "%");
                    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        showException(ex);
                    }
                }
            });
            
            // Initializes the speed reset button
            Button resetSpeedButton = new Button("Reset");
            resetSpeedButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent buttonEvent) {
                    try {
                        video.setSpeed(1);
                        if (video.equals(currentVideo)){
                            videoPlayer.getVideo().setRate(1);
                        }
                        speedField.setText("100%");
                    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        showException(ex);
                    }
                }
            });

            // Groups the speed elements into a row, and sets the row's properties
            HBox speedRow = new HBox(speedField, setSpeedButton, resetSpeedButton);
            speedRow.setSpacing(5);
            speedRow.disableProperty().bind(isSpeedChanged.selectedProperty().not());
            
            // Initializes the saved checkbox and it's properties
            CheckBox isSaved = new CheckBox("Save Video?");
            isSaved.setSelected(video.getIsSaved());
            isSaved.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    try {
                        video.setIsSaved(isSaved.isSelected());
                    } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                        showException(ex);
                    }
                }
            });
            
            // Initializzes the description text
            Label descriptionLabel = new Label("Description:");
            if(videoDescription.isEmpty()){
                descriptionLabel.setText("Description: None");
            }

            // Initializes the text area that shows the description and it's properties
            TextArea descriptionTextArea = new TextArea();
            descriptionTextArea.maxHeight(100);
            descriptionTextArea.setEditable(false);
            descriptionTextArea.setWrapText(true);
            if(!videoDescription.isEmpty()){
                descriptionTextArea.setText(videoDescription);
                // Changes the height of the text area
                // depending on how many lines the description has
                if(videoDescription.contains("\n")){
                    int lineCount = videoDescription.split("\n").length;
                    if(lineCount > 10){
                        descriptionTextArea.setPrefHeight(200);
                    } else {
                        descriptionTextArea.setPrefHeight(20 * lineCount);
                    }
                }
            }

            // Initializes the label that shows the rest of the video's properties
            // and it's properties
            Label bottomText = new Label("Position: " + videoPosition
                    + "\nPosition Bias: " + videoPositionBias
                    + "\nTags: " + videoTags);
            bottomText.setPrefWidth(propertiesAlert.getWidth());
            bottomText.setWrapText(true);

            // Initializes the button that deletes the video and it's properties
            Button deleteButton = new Button("Delete Video");
            deleteButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // Creates an alert that confirms if the user wants to delete the video
                    Alert deleteAlert = new Alert(AlertType.CONFIRMATION);
                    deleteAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                    deleteAlert.setTitle("Confirm Video Deletion");
                    deleteAlert.setHeaderText(null);
                    deleteAlert.setContentText("Are you sure you want to delete the video \"" + video.getVideoName() + "\"?");

                    ButtonType optionYes = new ButtonType("Yes");
                    ButtonType optionNo = new ButtonType("No");

                    deleteAlert.getButtonTypes().setAll(optionYes, optionNo);

                    Optional<ButtonType> result = deleteAlert.showAndWait();

                    // If the user chooses yes, then delete the video's folder and remove it from all lists
                    if (result.get() == optionYes) {
                        try {
                            video.deleteFolder(new File(video.getFolderPath()));
                            currentPlaylist.getPlaylist().remove(video);
                            if (playlistView.getItems().contains(video)){
                                playlistView.getItems().remove(video);
                            }
                            if (video.equals(currentVideo)){
                                updateVideoPlayer(">");
                            }
                            
                            propertiesAlert.close();
                        } catch (IOException | YoutubeException ex) {
                            showException(ex);
                        }
                    }
                }

            });
            
            // Initializes the button that resets the video's properties
            Button resetButton = new Button("Reset");
            resetButton.setOnAction(new EventHandler<ActionEvent> () {
                @Override
                public void handle(ActionEvent event) {
                    Alert resetAlert = new Alert(AlertType.CONFIRMATION);
                    resetAlert.getDialogPane().getStylesheets().add(getClass().getResource("/main.css").toExternalForm()); 
                    resetAlert.setTitle("Confirm Property Reset");
                    resetAlert.setHeaderText(null);
                    resetAlert.setContentText("Are you sure you want to reset the properties of the video \"" + video.getVideoName() + "\"?");

                    ButtonType optionYes = new ButtonType("Yes");
                    ButtonType optionNo = new ButtonType("No");

                    resetAlert.getButtonTypes().setAll(optionYes, optionNo);

                    Optional<ButtonType> result = resetAlert.showAndWait();

                    if (result.get() == optionYes) {
                        try {
                            video.setPositionBias(0);
                            video.setIsLooping(false);
                            video.setLoopCount(1);
                            video.setIsSpeedChanged(false);
                            video.setSpeed(1);
                            video.setIsSaved(false);
                            video.setTags("None");
                            
                            isLooping.setSelected(false);
                            loopField.setText(1 + "");
                            isSpeedChanged.setSelected(false);
                            speedField.setText("100%");
                            isSaved.setSelected(false);
                            
                            videoPlayer.getVideo().setCycleCount(1);
                            videoPlayer.getVideo().setRate(1);
                        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                            showException(ex);
                        }
                    }
                }
            });
            
            // Groups the reset and delete buttons together and sets the row's proeprties
            HBox resetRow = new HBox(resetButton, deleteButton);
            resetRow.setSpacing(5);
            
            // Initializes the column that holds the property elements,
            // and sets which elements it contains depending on if the description is empty.
            // Then, the column's properties are set.
            VBox alertCol;
            if(videoDescription.isEmpty()){
                alertCol = new VBox(topText, urlRow, isLooping, loopRow, isSpeedChanged, speedRow, isSaved, bottomText, descriptionLabel, resetRow);
            } else {
                alertCol = new VBox(topText, urlRow, isLooping, loopRow, isSpeedChanged, speedRow, isSaved, bottomText, descriptionLabel, descriptionTextArea, resetRow);
            }
            
            alertCol.setAlignment(Pos.CENTER_LEFT);
            alertCol.setPadding(new Insets(10));
            alertCol.setSpacing(5);

            propertiesAlert.getDialogPane().setContent(alertCol);
            ((Button)propertiesAlert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
            propertiesAlert.setGraphic(null);
            propertiesAlert.show();
        }
    }
    
    /**
     * Creates a VideoPlaylist object for each string in the given array
     * 
     * @param names - The array of playlist names
     * 
     * @return originalNames - The observable list of the playlist's original names
     * 
     * @throws FileNotFoundException 
     */
    ObservableList<String> getPlaylistNames(String[] names) throws FileNotFoundException {
        ArrayList<String> originalNames = new ArrayList<>();
        File playlistFile;
        Scanner playlistFileReader;
        
        for (String name : names){
            playlistFile = new File(playlistPath + "/" + name, "playlist.txt"); 
            playlistFileReader = new Scanner(playlistFile);
            playlistFileReader.nextLine();
            
            playlistFileReader.skip("Playlist Name: ");
            originalNames.add(playlistFileReader.nextLine());
            playlistFileReader.close();
        }
        
        return FXCollections.observableArrayList(originalNames);
    }
    
    /**
     * Downloads the given video from youtube and returns the path of it
     * 
     * @param vid - The video to be downloaded
     * 
     * @return The video's new path
     * 
     * @throws YoutubeException
     * @throws IOException 
     */
    protected String downloadVideo(PlaylistVideo vid) throws YoutubeException, IOException{
        File videoFolder = new File(vid.getFolderPath());
        ArrayList<String> files = new ArrayList<>(Arrays.asList(videoFolder.list()));
        File tempFile = new File(vid.getVideoPath().replace("file:/", "").replace("%20", " "));
        System.out.println("Name: " + vid.getVideoName() + " Exists: " + tempFile.exists() + " Path: " + vid.getVideoPath());
        
        // If the video does currently not have a video, then download the video from Youtube
        if (!files.contains(vid.getVideoFileName())){
            YoutubeVideo video = downloader.getVideo(vid.getVideoID());
            List<AudioVideoFormat> videoWithAudioFormats = video.videoWithAudioFormats();
            video.download(videoWithAudioFormats.get(0), videoFolder);
        }
        return vid.getVideoPath();
        
    }
    
    /**
     * Updates the application to match the current video
     * 
     * @throws MalformedURLException
     * @throws FileNotFoundException
     * @throws YoutubeException
     * @throws IOException 
     */
    void resetGUI() throws MalformedURLException, FileNotFoundException, YoutubeException, IOException {
        
        // Duplicates the playlist into another one for randomization
        reorderedPlaylist.clear();
        randomizedPlaylist.clear();
        randomizedPlaylist.addAll(currentPlaylist.getPlaylist());
        Collections.sort(currentPlaylist.getPlaylist());
        
        // If the video player has no video, or the current playlist does not have the current video,
        // then reset the GUI to it's default view of a playlist
        if (videoPlayer.getVideo() == null || !currentPlaylist.getPlaylist().contains(currentVideo)){
            pageNumber = 1;
            currentPageList = new ArrayList<>();

            // Selects the first item in the list to show it's information
            playlistView.setDisable(false);
            savedPlaylistBox.setDisable(false);
            savedPlaylistBox.setItems(getPlaylistNames(playlistsFolder.list()));
            savedPlaylistBox.getSelectionModel().select(currentPlaylist.getPlaylistName());
            
            // Resets the loaded video title and enables the disabled interface elements
            loadedVideoTitle = "";
            playlistSettingsButton.setDisable(false);
            deletePlaylistButton.setDisable(false);
            refreshPageButton.setDisable(false);
            tagRow.setDisable(false);
            playlistInterface.setDisable(false);
            
            // Disables the page buttons depending on the size of the playlist
            firstPageButton.setDisable(true);
            previousPageButton.setDisable(true);
            nextPageButton.setDisable(currentPlaylist.getSize() < pageSizeLimit);
            lastPageButton.setDisable(currentPlaylist.getSize() < pageSizeLimit);

            updatePageList(currentPlaylist.getPlaylist(), "=", true);
            updateVideoPlayer("=");
            
            videoSearchRow.setDisable(false);
            currentPosition = 0;
            maxListSize = 0;
            
            layout.setCenter(videoPlayer.getLayout());
        } 
        // Otherwise, reset the GUI to match the current page
        else {

            // Selects the first item in the list to show it's information
            playlistView.setDisable(false);

            savedPlaylistBox.setItems(getPlaylistNames(playlistsFolder.list()));
            savedPlaylistBox.getSelectionModel().select(currentPlaylist.getPlaylistName());

            // Enables the video search row for use
            loadedVideoTitle = "";
            playlistSettingsButton.setDisable(false);
            deletePlaylistButton.setDisable(false);
            refreshPageButton.setDisable(false);
            videoSearchRow.setDisable(false);
            tagRow.setDisable(false);
            playlistInterface.setDisable(false);
            firstPageButton.setDisable(true);
            previousPageButton.setDisable(true);
            nextPageButton.setDisable(currentPlaylist.getSize() < pageSizeLimit);
            lastPageButton.setDisable(currentPlaylist.getSize() < pageSizeLimit);

            if (!currentPlaylist.getPlaylist().contains(currentVideo)){
                updatePageList(currentPlaylist.getPlaylist(), "=", true);
                updateVideoPlayer("=");
            }

            layout.setCenter(videoPlayer.getLayout());
        }
        
    }

    /**
     * Updates the current page list to match the given list, depending on the direction
     * @param list - The currently loaded list
     * @param direction - Which way the page will update
     * @param isPageButtonCall - If the update was called by one of the page buttons
     */
    void updatePageList(ArrayList<PlaylistVideo> list, String direction, boolean isPageButtonCall) {
        firstPageButton.setDisable(false);
        previousPageButton.setDisable(false);
        nextPageButton.setDisable(false);
        lastPageButton.setDisable(false);
        
        maxPageNumber = (int)Math.ceil((double)list.size() / pageSizeLimit);
        // If the updates was not called from one of the page buttons,
        // update the page number depending on the position of the video
        if (!isPageButtonCall){
            if (list.indexOf(currentVideo) == -1){
                pageNumber = 1;
            } else {
                pageNumber = (int)Math.ceil((((double)list.indexOf(currentVideo) + 1) / pageSizeLimit));
                if (pageNumber < 1){
                    pageNumber = 1;
                } else if (pageNumber > maxPageNumber){
                    pageNumber = maxPageNumber;
                }
            }
        }
        // If the updates was not from one of the page buttons and the video is at the beginning or end of the list,
        // then just set the page number to the first one
        if (!isPageButtonCall && list.indexOf(currentVideo) == 0 || list.indexOf(currentVideo) == list.size()){
             pageNumber = 1;
        }
        
        currentPageList.clear();
        
        // Updates the playlist view's page depending on the direction
        switch (direction) {
            case "<<":
                if (pageNumber > 1) {
                    pageNumber = 1;
                    for (int i = 0; i < pageSizeLimit; i++) {
                        if (i < list.size()) {
                            currentPageList.add(list.get(i));
                        } else {
                            i = pageSizeLimit * pageNumber;
                        }
                    }

                    pageNumberLabel.setText("Page " + pageNumber + "/" + maxPageNumber);
                }
                break;
            case "<":
                if (pageNumber >= 1) {
                    if (isPageButtonCall){
                        pageNumber--;
                    }
                    for (int i = 0 + pageSizeLimit * (pageNumber - 1); i < pageSizeLimit * pageNumber; i++) {
                        if (i < list.size()) {
                            currentPageList.add(list.get(i));
                        } else {
                            i = pageSizeLimit * pageNumber;
                        }
                    }

                    pageNumberLabel.setText("Page " + pageNumber + "/" + maxPageNumber);
                }
                break;
            case "=":
                for (int i = 0 + pageSizeLimit * (pageNumber - 1); i < pageSizeLimit * pageNumber; i++) {
                    if (i < list.size()) {
                        currentPageList.add(list.get(i));
                    } else {
                        i = pageSizeLimit * pageNumber;
                    }
                }
                
                if (!reorderedPlaylist.isEmpty() && !playlistPage.equals(reorderedPlaylist)){
                    currentPageList.clear();
                    currentPageList.addAll(reorderedPlaylist);
                }
                
                
                break;
            case ">":
                if (pageNumber <= maxPageNumber) {
                    if (isPageButtonCall){
                        pageNumber++;
                    }
                    
                    for (int i = 0 + pageSizeLimit * (pageNumber - 1); i < pageSizeLimit * pageNumber; i++) {
                        if (i < list.size()) {
                            currentPageList.add(list.get(i));
                        } else {
                            i = pageSizeLimit * pageNumber;
                        }
                    }

                    pageNumberLabel.setText("Page " + pageNumber + "/" + maxPageNumber);
                }
                break;
            case ">>":
                if (pageNumber < maxPageNumber) {
                    pageNumber = maxPageNumber;
                    for (int i = 0 + pageSizeLimit * (pageNumber - 1); i < pageSizeLimit * pageNumber; i++) {
                        if (i < list.size()) {
                            currentPageList.add(list.get(i));
                        } else {
                            i = pageSizeLimit * pageNumber;
                        }
                    }

                    pageNumberLabel.setText("Page " + pageNumber + "/" + maxPageNumber);
                }
                break;
            default:
                break;
        }
        
        
        // Displays the playlist in the playlist view
        playlistView.setItems(FXCollections.observableArrayList(currentPageList));
        boolean searchEnabled = searchResults.size() > 0 && searchResults.size() <= pageSizeLimit;
        // Disables the page buttons depending on the size of the playlist
        // and the page number
        if (currentPlaylist.getSize() <= pageSizeLimit || searchEnabled) {
            pageNumberLabel.setText("Page 1/1");
            previousPageButton.setDisable(true);
            firstPageButton.setDisable(true);
            nextPageButton.setDisable(true);
            lastPageButton.setDisable(true);
        } else if (pageNumber == maxPageNumber) {
            pageNumberLabel.setText("Page " + pageNumber + "/" + maxPageNumber);
            nextPageButton.setDisable(true);
            lastPageButton.setDisable(true);
        } else if (pageNumber == 1) {
            pageNumberLabel.setText("Page " + pageNumber + "/" + maxPageNumber);
            previousPageButton.setDisable(true);
            firstPageButton.setDisable(true);
        } else {
            pageNumberLabel.setText("Page " + pageNumber + "/" + maxPageNumber);
        }
        
        // If the video's title is not empty and the playlist has the video,
        // select the current video
        if (!currentVideo.getVideoName().isEmpty() && currentPlaylist.getPlaylist().contains(currentVideo)){
            playlistView.getSelectionModel().select(currentVideo);
        } 
        // Otherwise, select the first video in the page list
        else {
            playlistView.getSelectionModel().select(0);
        }
        
        // Scrolls the page list up to the current position of the video - 3,
        // or the current video if the position is lower than 3
        if (list.indexOf(currentVideo) <= 2 || currentVideo.getVideoName().isEmpty()){
            playlistView.scrollTo(0);
        } else {
            playlistView.scrollTo(playlistView.getItems().indexOf(currentVideo) - 3);
        }
    }
}
