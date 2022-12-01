package com.avermak.vkube.api.hello;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerResponse;

public class MainVerticle extends AbstractVerticle {

    public static final String CONTEXT_API = "/api";
    public static final String CONTEXT_RPC = "/rpc";

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
            ctx.response().end("Hello vkube over http!");
        });
        System.out.println("API route configured at " + CONTEXT_API);
        Route rpcRoute = router.route(); //.path(CONTEXT_RPC);
        rpcRoute.consumes("application/grpc").handler(rc -> grpcServer.handle(rc.request()));
        System.out.println("RPC route configured at " + CONTEXT_RPC);

        System.out.println("Attaching router to server");
        httpServer.requestHandler(router);
        System.out.println("Configuring gRPC handler routine");
        grpcServer.callHandler(HelloVKubeServiceGrpc.getSayHelloMethod(), req -> {
            req.handler(hello -> {
                System.out.println("Received request over gRPC [" + req.fullMethodName() + "]");
                GrpcServerResponse<HelloRequest, HelloReply> res = req.response();
                HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + hello.getName()).build();
                res.end(reply);
            });
        });
        final int port = 22000;
        System.out.println("Starting server on port " + port);
        httpServer.listen(port, ares -> {
            if (ares.failed()) {
                System.out.println("Failed to start server on port "+port+". "+ ares.cause());
                System.err.println("Failed to start server on port "+port+". "+ ares.cause());
                System.out.println("Closing vertx");
                vertx.close();
            } else {
                System.out.println("Server started on " + ares.result().actualPort());
            }
        });
    }
}
