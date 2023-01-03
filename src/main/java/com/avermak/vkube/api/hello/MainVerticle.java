package com.avermak.vkube.api.hello;

import com.google.protobuf.util.JsonFormat;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;

import java.net.InetAddress;

public class MainVerticle extends AbstractVerticle {

    public static final String CONTEXT_API = "/api";
    public static final String CONTEXT_RPC = "/rpc";

    public static final int SERVICE_PORT = 22000;

    private ServerInfo serverInfo = null;

    @Override
    public void start() throws Exception {
        System.out.println("Starting hellovkube");

        System.out.println("Creating http server instance");
        HttpServer httpServer = vertx.createHttpServer();

        System.out.println("Creating grpc server instance");
        GrpcServer grpcServer = GrpcServer.server(vertx);

        System.out.println("Creating Router configuration");
        Router router = Router.router(vertx);
        Route apiRoute = router.route().path(CONTEXT_API);

        apiRoute.handler(ctx -> {
            System.out.println("Received request over http. " + ctx.request().absoluteURI());
            ctx.response().end(buildRESTResponse(ctx.request()));
        });
        System.out.println("API route configured at " + CONTEXT_API);
        Route rpcRoute = router.route();
        rpcRoute.consumes("application/grpc").handler(rc -> grpcServer.handle(rc.request()));
        System.out.println("RPC route configured at " + CONTEXT_RPC);

        System.out.println("Attaching router to server");
        httpServer.requestHandler(router);
        System.out.println("Configuring gRPC handler routine");
        grpcServer.callHandler(HelloVKubeServiceGrpc.getSayHelloMethod(), req -> {
            req.handler(hello -> {
                System.out.println("Received request over gRPC [" + req.fullMethodName() + "]");
                HelloReply replyObj = buildGRPCResponse(hello, req);
                System.out.println("Sending gRPC response ("+replyObj.getSerializedSize()+" bytes): ["+replyObj.toString()+"]");
                req.response().end(replyObj);
            });
        });
        System.out.println("Starting server on port " + SERVICE_PORT);
        httpServer.listen(SERVICE_PORT, ares -> {
            if (ares.failed()) {
                System.out.println("Failed to start server on port "+SERVICE_PORT+". "+ ares.cause());
                System.err.println("Failed to start server on port "+SERVICE_PORT+". "+ ares.cause());
                System.out.println("Closing vertx");
                vertx.close();
            } else {
                System.out.println("Server started on " + ares.result().actualPort());
            }
        });
    }

    private ServerInfo getServerInfo() {
        if (this.serverInfo == null) {
            synchronized (this) {
                if (this.serverInfo == null) {
                    this.serverInfo = new ServerInfo();
                    String podName = "unavailable";
                    try {
                        podName = InetAddress.getLocalHost().getHostName();
                        this.serverInfo.setPodName(podName);
                    } catch (Exception ex) {
                        System.out.println("Unable to retrieve podName. " + ex);
                        ex.printStackTrace();
                    }
                    String podIP = "0.0.0.0";
                    try {
                        podIP = InetAddress.getLocalHost().getHostAddress();
                        this.serverInfo.setPodIP(podIP);
                    } catch (Exception ex) {
                        System.out.println("Unable to retrieve podIP. " + ex);
                        ex.printStackTrace();
                    }
                    String nodeName = System.getenv("NODE_NAME");
                    if (nodeName == null) {
                        nodeName = "unavailable";
                    }
                    String nodeIP = System.getenv("NODE_IP");
                    if (nodeIP == null) {
                        nodeIP = "0.0.0.0";
                    }
                    this.serverInfo.setNodeName(nodeName);
                    this.serverInfo.setNodeIP(nodeIP);
                }
            }
        }
        this.serverInfo.update();
        return this.serverInfo;
    }

    private HelloReply buildGRPCResponse(HelloRequest hello, GrpcServerRequest<HelloRequest, HelloReply> req) {
        return buildResponse(hello.getName());
    }

    private String buildRESTResponse(HttpServerRequest request) {
        String responseData = "";
        try {
            HelloReply reply = buildResponse(request.getParam("name"));
            responseData = JsonFormat.printer().print(reply);
        } catch (Exception ex) {
            responseData = "INTERNAL SERVER ERROR";
            System.out.println("Error building REST response " + ex);
            ex.printStackTrace();
        }
        return responseData;
    }

    private HelloReply buildResponse(String name) {
        HelloReply.Builder b = HelloReply.newBuilder();
        ServerInfo s = getServerInfo();

        b.setMessage("Hello from vkube" + (name == null ? "!" : ", " + name + "!"));
        b.setNodeIP(s.getNodeIP());
        b.setNodeName(s.getNodeName());
        b.setPodIP(s.getPodIP());
        b.setPodName(s.getPodName());
        b.setPodTime(s.getPodTimeGMT());

        return b.build();
    }
}
