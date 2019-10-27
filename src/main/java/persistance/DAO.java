package persistance;

import org.sql2o.Connection;
import org.sql2o.Sql2o;
import persistance.model.Playlist;
import persistance.model.Song;
import persistance.model.Image;

import java.util.List;

public class DAO {
    private Sql2o sql2o = new Sql2o("jdbc:postgresql://82.202.226.2:5432/postgres", "postgres", "lal");

    public List<Song> getAllSongs() {
        String sql = "SELECT id, title, author FROM songs";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(Song.class);
        }
    }

    public void addNewVote(long songId, String emotion) {
        String sql = "INSERT INTO votes(song_id, emotion) values (:songId, :emotion)";
        try (Connection con = sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("songId", songId)
                    .addParameter("emotion", emotion)
                    .executeUpdate();
        }
    }


    public List<Image> getNextImage(String excludeImageIds, long count) {
        String sql;
        try (Connection con = sql2o.open()) {
            if (excludeImageIds == null)
                sql = "select id, name from images order by RANDOM() limit " + count;
            else
                sql = "select id, name from images where id not in (" + excludeImageIds + ") order by RANDOM() limit " + count;
            return con.createQuery(sql)
                    .executeAndFetch(Image.class);
        }
    }

    public List<Song> getSongsRelatedToImage(long imageId) {
        try (Connection con = sql2o.open()) {
            String group = con.createQuery("select images.group from images where id = " + imageId).executeScalar(String.class);
            String sql = "SELECT id, " +
                    "title, " +
                    "author, " +
                    "album, " +
                    "genre, " +
                    "author || ' - ' || title || '.mp3' as link " +
                    "FROM songs " +
                    "where " + group + " = 1 order by RANDOM() limit 11"; //Бог простит
            return con.createQuery(sql).executeAndFetch(Song.class);
        }
    }

    public List<Playlist> getAllPlaylists() {
        String sql = "SELECT id, title, \"imageName\" FROM playlists order by RANDOM() limit 5";
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(Playlist.class);
        }
    }
}
