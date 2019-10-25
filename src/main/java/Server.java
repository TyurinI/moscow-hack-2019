import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistance.DAO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        DAO dao = new DAO();
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
            config.addStaticFiles("/images");
        }).error(404, ctx -> ctx.json("not found"))
                .start(8080);
        app.before(ctx -> log.info("request : " + ctx.fullUrl() + " body:" + ctx.body()));
        app.get("/", ctx ->
                ctx.json(Collections.singletonMap("Message", "Hello World"))
        );
        app.get("/nextImages", ctx ->
                ctx.json(dao.getAllSongs())
        );
        app.get("/neuron", ctx -> {
            Process process = Runtime.getRuntime().exec("python ./script.py");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info(reader.readLine());
            reader.close();
        });
    }
}
