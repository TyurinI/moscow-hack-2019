package persistance.model;

import lombok.Data;

import java.util.SplittableRandom;

@Data
public class Song {
    private long id;
    private String title;
    private String author;
    private String album;
    private String genre;
    private String link;
}
