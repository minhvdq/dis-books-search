package networking;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    private static final String STATUS_ENDPOINT = "/status";
    private  int port;
    private OnRequestCallBack onRequestCallBack;
    private HttpServer server;

    public WebServer(int port, OnRequestCallBack onRequestCallBack) {
        this.port = port;
        this.onRequestCallBack = onRequestCallBack;
    }

    public void startServer(){
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext taskContext = server.createContext(onRequestCallBack.getEndpoint());

        statusContext.setHandler(this::handleStatusCheck);
        taskContext.setHandler(this::handleTask);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

    }

    public void stop(){
        server.stop(10);
    }

    public void handleStatusCheck( HttpExchange exchange ) throws IOException {
        if( !exchange.getRequestMethod().equalsIgnoreCase("get")){
            exchange.close();
            return;
        }
        System.out.println("port: " + port);
        String responseMessage = "Server is alive!";
        sendResponses(responseMessage.getBytes(), exchange);
    }
    public void handleTask( HttpExchange exchange) throws IOException {
        if( !exchange.getRequestMethod().equalsIgnoreCase("post")){
            exchange.close();
            return;
        }
        byte[] responseByte = onRequestCallBack.handleRequest(exchange.getRequestBody().readAllBytes());
        sendResponses(responseByte, exchange);
    }
    public void sendResponses(byte[] responseMessageBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseMessageBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseMessageBytes);
        outputStream.flush();
        outputStream.close();
    }
}
