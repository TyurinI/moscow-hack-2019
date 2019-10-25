import io.javalin.Javalin;

import java.util.Collections;

public class Server {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
        }).start(8080);
        app.get("/", ctx -> ctx.json(Collections.singletonMap("Message", "Hello World")));
    }
}
