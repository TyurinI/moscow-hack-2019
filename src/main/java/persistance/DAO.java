package persistance;

import org.sql2o.Connection;
import org.sql2o.Sql2o;
import persistance.model.Song;

import java.util.List;

public class DAO {
    private Sql2o sql2o = new Sql2o("jdbc:postgresql://localhost:5432/postgres", "postgres", "lal");
    public List<Song> getAllSongs(){
        String sql =
                "SELECT id, title, author, filename " +
                        "FROM songs";

        try(Connection con = sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(Song.class);
        }
    }
}
