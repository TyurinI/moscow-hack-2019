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
            config.addStaticFiles("/");
        }).error(404, ctx -> ctx.json("not found"))
                .start(8080);
        app.before(ctx -> log.info("request : " + ctx.fullUrl() + " body:" + ctx.body()));

        app.get("/", ctx ->
                ctx.json(Collections.singletonMap("Message", "Healthcheck, server is running"))
        );

        app.get("/allSong", ctx ->
                ctx.json(dao.getAllSongs())
        );

        app.get("/neuron", ctx -> {
            Process process = Runtime.getRuntime().exec("python ./script.py");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info(reader.readLine());
            reader.close();
        });

        app.get("/vote", ctx -> {
            long songId = Long.parseLong(ctx.queryParam("songId"));
            String emotion = ctx.queryParam("emotion");
            dao.addNewVote(songId, emotion);
            ctx.status(201);
        });
        app.get("/nextImage", ctx -> {
            Image nextImage;
            if (ctx.sessionAttribute("exclude") == null) {
                nextImage = dao.getNextImage(null);
                ctx.sessionAttribute("exclude", nextImage.getId());
            } else {
                nextImage = dao.getNextImage(ctx.sessionAttribute("exclude").toString());
                ctx.sessionAttribute("exclude", ctx.sessionAttribute("exclude").toString().concat("," + nextImage.getId()));
            }
            log.info(ctx.sessionAttribute("exclude").toString());
            ctx.json(nextImage);
        });
    }
}
