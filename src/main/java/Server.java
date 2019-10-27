import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistance.DAO;
import persistance.model.Image;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        DAO dao = new DAO();
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
            config.addStaticFiles("/public");
        }).error(404, ctx -> ctx.json("not found"))
                .start(80);
        app.before(ctx -> log.info("request : " + ctx.fullUrl() + " body:" + ctx.body()));

        app.get("/api/", ctx ->
                ctx.json(Collections.singletonMap("Message", "Healthcheck, server is running"))
        );

        app.get("/api/allSong", ctx ->
                ctx.json(dao.getAllSongs())
        );

        app.get("/api/neuron", ctx -> {
            Process process = Runtime.getRuntime().exec("python ./script.py");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info(reader.readLine());
            reader.close();
        });

        app.get("/api/vote", ctx -> {
            if (ctx.queryParam("songId") == null || ctx.queryParam("emotion") == null) {
                throw new IllegalArgumentException("songId and emotion required");
            } else {
                long songId = Long.parseLong(ctx.queryParam("songId"));
                String emotion = ctx.queryParam("emotion");
                dao.addNewVote(songId, emotion);
                ctx.status(201);
            }
        });

        app.get("/api/nextImage", ctx -> {
            List<Image> nextImages;
            long count = Long.parseLong(ctx.queryParam("count"));
            if (ctx.sessionAttribute("exclude") == null) {
                nextImages = dao.getNextImage(null, count);
                ctx.sessionAttribute("exclude", toExcludeString(nextImages));
            } else {
                nextImages = dao.getNextImage(ctx.sessionAttribute("exclude").toString(), count);
                ctx.sessionAttribute("exclude", ctx.sessionAttribute("exclude").toString().concat(", " + toExcludeString(nextImages)));
            }
            log.info(ctx.sessionAttribute("exclude").toString());
            ctx.json(nextImages);
        });

        app.get("/api/submitImage", ctx -> {
            ctx.sessionAttribute("exclude", null);
            if (ctx.queryParam("imageId") == null) {
                throw new IllegalArgumentException("imageId required");
            } else {
                long imageId = Long.parseLong(ctx.queryParam("imageId"));
                ctx.json(dao.getSongsRelatedToImage(imageId));
            }
        });

        app.get("/api/playlists", ctx -> ctx.json(dao.getAllPlaylists()));
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(Collections.singletonMap("message", e.getMessage()));
        });
    }

    private static String toExcludeString(List<Image> images) {
        if (images.isEmpty())
            throw new IllegalArgumentException("Нет больше картинок");
        String result = String.valueOf(images.get(0).getId());
        for (int i = 1; i < images.size(); i++) {
            result += ", " + images.get(i).getId();
        }
        return result;
    }
}
