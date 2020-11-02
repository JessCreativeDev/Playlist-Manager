/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 *
 * @author Thema
 */
public class VideoCell extends ListCell<PlaylistVideo> {
    // The variables
    ImageView thumbnail;
    double imageWidth = 40;
    double imageHeight = 30;
    
    public VideoCell(){
        
        // Shows the image and name of the dragged video
        setOnDragDetected(event -> {
            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(getItem().getVideoName());
            dragboard.setDragView(thumbnail.getImage());
            dragboard.setContent(content);
            event.consume();
        });
        
        // Swaps the positions of the dragged video and the video it was dropped onto
        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Decreases the opacity of the video the dragged video is over
        setOnDragEntered(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                setOpacity(0.3);
            }
        });

        // Returns the opacity of the video the dragged video was over back to normal
        setOnDragExited(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                setOpacity(1);
            }
        });
        
        // Swaps the positions of the video the dragged video was over and the dragged video
        setOnDragDropped(event -> {

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    ObservableList<PlaylistVideo> items = getListView().getItems();
                    int draggedIdx = items.indexOf(getItem());
                    
                    for (int i = 0; i < items.size(); i++) {
                        PlaylistVideo vid = items.get(i);
                        if(vid.getVideoName().equals(db.getString())){
                            draggedIdx = i;
                        }
                    }
                    
                    int thisIdx = items.indexOf(getItem());

                    PlaylistVideo temp = items.get(draggedIdx);
                    
                    items.set(draggedIdx, getItem());
                    items.set(thisIdx, temp);
                    
                    getListView().setItems(items);
                    getListView().getSelectionModel().select(thisIdx);

                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(
                    DragEvent::consume
            );
            
            getStyleClass().add("list-cell");
    }
    
    @Override
    public void updateItem(PlaylistVideo vid, boolean empty){
        super.updateItem(vid, empty);
        if(empty){
            setText(null);
            setGraphic(null);
        } else {
            thumbnail = new ImageView(vid.getThumbnail());
            thumbnail.setFitWidth(imageWidth);
            thumbnail.setFitHeight(imageHeight);
                        
            // Changes the size of the thumbnail icon depending on the height of the window
            getScene().heightProperty().addListener(new ChangeListener(){
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    if((double)newValue > 600 && getScene().getWidth() > 800){
                        imageWidth = 60;
                        imageHeight = 45;
                        thumbnail.setFitWidth(imageWidth);
                        thumbnail.setFitHeight(imageHeight);
                    } else {
                        imageWidth = 40;
                        imageHeight = 30;
                        thumbnail.setFitWidth(imageWidth);
                        thumbnail.setFitHeight(imageHeight);
                    }
                }
            });
            setText(vid.getVideoName());
            setGraphic(thumbnail);
        }
    }
}
