package Hub;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicPlayer{
    private static MediaPlayer player;

    public static void startMusic() {
        Media media = new Media(MusicPlayer.class.getResource("/music/795985__matio888__meditation-ambient-background.mp3").toExternalForm());
        player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(0.5); // Default volume
        player.play();
    }

    public static void setVolume(double vol) {
        if (player != null) player.setVolume(vol);
    }

    public static void stopMusic(){
        if (player != null) player.stop();
    }
    public static void pauseMusic(){
        if (player != null) player.pause();
    }
    public static void resumeMusic(){
        if (player != null) player.play();
    }
}