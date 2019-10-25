package persistance.model;

import lombok.Data;

@Data
public class Song {
    private long id;
    private String title;
    private String author;
    private String filename;
}
