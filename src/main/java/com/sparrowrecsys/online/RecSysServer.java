package com.sparrowrecsys.online;

import com.sparrowrecsys.online.datamanager.DataManager;
import com.sparrowrecsys.online.datamanager.Movie;
import com.sparrowrecsys.online.service.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;

/***
 * Recsys Server, end point of online recommendation service
 */

public class RecSysServer {

    public static void main(String[] args) throws Exception {
        new RecSysServer().run();
    }

    // recsys server port number
    private static final int DEFAULT_PORT = 6010;

    public void run() throws Exception {

        int port = DEFAULT_PORT;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException ignored) {
        }

        // set ip and port number
        InetSocketAddress inetAddress = new InetSocketAddress("0.0.0.0", port);
        Server server = new Server(inetAddress);

        // Try to find webroot in multiple locations
        String webRootPath = null;
        URL webRootLocation = this.getClass().getResource("/webroot/index.html");

        if (webRootLocation != null) {
            URI webRootUri = URI.create(webRootLocation.toURI().toASCIIString().replaceFirst("/index.html$", "/"));
            File webRootDir = new File(webRootUri);
            webRootPath = webRootDir.getAbsolutePath() + File.separator;
        } else {
            // Fallback to local src directory
            File srcDir = new File("src/main/resources/webroot/");
            if (srcDir.exists()) {
                webRootPath = srcDir.getAbsolutePath() + File.separator;
            }
        }

        if (webRootPath == null) {
            throw new IllegalStateException(
                    "Unable to determine webroot URL location. Checked classpath and src/main/resources/webroot/");
        }

        System.out.println("Resolved Data Path: " + webRootPath);

        // Initialize GameService (MyBatis) - this will trigger SqlSessionFactory build
        try {
            com.sparrowrecsys.online.service.GameService.getInstance();
            System.out.println("GameService initialized successfully (MyBatis).");
        } catch (Exception e) {
            System.err.println("Failed to initialize GameService: " + e.getMessage());
            e.printStackTrace();
        }

        // Legacy DataManager loading (Optional: Keep if other services still rely on
        // it,
        // but for Search we are now using DB. If other services use DataManager, they
        // might break if we don't load data.
        // For now, let's keep loading it to avoid breaking other endpoints until they
        // are refactored.)
        DataManager.getInstance().loadData(webRootPath + "sampledata/games_filtered.csv",
                "", "", "", "", "i2vEmb", "uEmb");

        // Verify data loaded (Legacy check)
        List<com.sparrowrecsys.online.datamanager.Movie> check = DataManager.getInstance().getMovies(10,
                "positiveReviews");
        if (check == null || check.isEmpty()) {
            System.err.println("CRITICAL ERROR: No games loaded! Please check games_filtered.csv path and format.");
        } else {
            System.out.println("Server Startup: Loaded " + check.size() + " sample games successfully.");
        }

        // create server context
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setBaseResource(Resource.newResource(webRootPath));
        context.setWelcomeFiles(new String[] { "index.html" });
        context.getMimeTypes().addMimeMapping("txt", "text/plain;charset=utf-8");

        // bind services with different servlets
        context.addServlet(DefaultServlet.class, "/");
        context.addServlet(new ServletHolder(new MovieService()), "/getmovie");
        context.addServlet(new ServletHolder(new UserService()), "/getuser");
        context.addServlet(new ServletHolder(new SimilarMovieService()), "/getsimilarmovie");
        context.addServlet(new ServletHolder(new RecommendationService()), "/getrecommendation");
        context.addServlet(new ServletHolder(new RecForYouService()), "/getrecforyou");
        context.addServlet(new ServletHolder(new SearchService()), "/search");

        // set url handler
        server.setHandler(context);
        System.out.println("RecSys Server has started.");

        // start Server
        server.start();
        server.join();
    }
}
