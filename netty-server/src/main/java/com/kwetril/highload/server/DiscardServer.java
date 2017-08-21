package com.kwetril.highload.server;

import com.kwetril.highload.database.RepositoryProvider;
import com.kwetril.highload.database.TimestampProvider;
import com.kwetril.highload.parsing.RequestParser;
import com.kwetril.highload.request.*;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class DiscardServer {
    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup;
        EventLoopGroup workerGroup;
        if (Epoll.isAvailable()) {
            System.out.println("Using epoll");
            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup();
        } else {
            System.out.println("Using nio");
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
        }
        //final EventExecutorGroup logicProcessor = new DefaultEventExecutorGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);

            if (Epoll.isAvailable()) {
                b.channel(EpollServerSocketChannel.class);
            } else {
                b.channel(NioServerSocketChannel.class);
            }
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false))
                            .addLast("aggregator", new HttpObjectAggregator(2 * 1024 * 1024))
                            .addLast("encoder", new HttpResponseEncoder())
                            .addLast("handler", new WebServerHandler());
                }
            })
                    .option(ChannelOption.SO_BACKLOG, 128);
            //.childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}


class WebServerHandler extends SimpleChannelInboundHandler<Object> {
    private Object userLock = new Object();
    private Object locationLock = new Object();
    private Object visitLock = new Object();

    /**
     * Handles a new message.
     *
     * @param ctx The channel context.
     * @param msg The HTTP request message.
     */
    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final Object msg) {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }
        final FullHttpRequest request = (FullHttpRequest) msg;

        final String method = request.method().toString();
        final String uri = request.uri();

        try {
            switch (method.charAt(0)) {
                case 'G':
                    switch (uri.charAt(1)) {
                        case 'u':
                            String[] parts = uri.split("/");
                            if (parts.length == 3) {
                                //get user
                                getUser(parts, ctx);
                                return;
                            } else {
                                //get user visits
                                getUserVisits(parts, ctx);
                                return;
                            }
                        case 'l':
                            String[] parts1 = uri.split("/");
                            if (parts1.length == 3) {
                                //get locations
                                getLocation(parts1, ctx);
                                return;
                            } else {
                                //get locations avg
                                getLocationAvg(parts1, ctx);
                                return;
                            }
                        case 'v':
                            //get visits
                            getVisit(uri, ctx);
                            return;
                    }
                    break;
                case 'P':
                    switch (uri.charAt(1)) {
                        case 'u':
                            if (uri.charAt(7) == 'n') {
                                //new user
                                newUser(request.content(), ctx);
                                return;
                            } else {
                                //edit user
                                editUser(uri, request.content(), ctx);
                                return;
                            }
                        case 'l':
                            if (uri.charAt(11) == 'n') {
                                //new location
                                newLocation(request.content(), ctx);
                                return;
                            } else {
                                //edit location
                                editLocation(uri, request.content(), ctx);
                                return;
                            }
                        case 'v':
                            if (uri.charAt(8) == 'n') {
                                //new visit
                                newVisit(request.content(), ctx);
                                return;
                            } else {
                                //edit visit
                                editVisit(uri, request.content(), ctx);
                                return;
                            }
                    }
                    break;
            }
        }
        catch (Exception e) {
            System.out.println(String.format("Error %s: %s", e.getMessage(), uri));
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
            return;
        }
        System.out.println(String.format("Not found: %s", uri));
        writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
    }

    private void getLocationAvg(String[] parts, final ChannelHandlerContext ctx) {
        int locationId = Integer.parseInt(parts[2]);
        String[] query = parts[parts.length - 1].split("\\?");
        String fromDateStr = null;
        String toDateStr = null;
        String fromAgeStr = null;
        String toAgeStr = null;
        String gender = null;
        switch (query.length) {
            case 1:
                break;
            case 2:
                String[] params = query[1].split("&");
                for (int i = 0; i < params.length; i++) {
                    String p = params[i];
                    switch (p.charAt(5)) {
                        case 'a':
                            fromDateStr = p.substring(9);
                            break;
                        case 'e':
                            toDateStr = p.substring(7);
                            break;
                        case 'g':
                            fromAgeStr = p.substring(8);
                            break;
                        case '=':
                            toAgeStr = p.substring(6);
                            break;
                        case 'r':
                            gender = p.substring(7);
                            break;
                    }
                }
                break;
            default:
                writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
                return;
        }
        try {
            if (gender != null && (gender.length() > 1 || (gender.charAt(0) != 'm' && gender.charAt(0) != 'f'))) {
                writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
                return;
            }
            long fromDate = 0, toDate = 0, fromAge = 0, toAge = 0;
            long currentMillis = TimestampProvider.timestamp * 1000;
            boolean hasFromDate = fromDateStr != null;
            if (hasFromDate) fromDate = Long.parseLong(fromDateStr);
            boolean hasToDate = toDateStr != null;
            if (hasToDate) toDate = Long.parseLong(toDateStr);
            boolean hasFromAge = fromAgeStr != null;
            if (hasFromAge) {
                fromAge = Long.parseLong(fromAgeStr);
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(currentMillis);
                calendar.add(Calendar.YEAR, (int) -fromAge);
                fromAge = calendar.getTimeInMillis() / 1000;
            }
            boolean hasToAge = toAgeStr != null;
            if (hasToAge) {
                toAge = Long.parseLong(toAgeStr);
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(currentMillis);
                calendar.add(Calendar.YEAR, (int) -toAge);
                toAge = calendar.getTimeInMillis() / 1000;
            }
            double avg = RepositoryProvider.repo.getLocationMark(locationId,
                    hasFromDate, fromDate, hasToDate, toDate,
                    hasFromAge, fromAge, hasToAge, toAge,
                    gender);
            if (avg >= -0.5) {
                writeResponse(ctx, HttpResponseStatus.OK, String.format("{\"avg\":%.5f}", avg));
                return;
            } else {
                writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
                return;
            }
        } catch (Exception ex) {
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
        }
    }

    private void getUserVisits(String[] parts, final ChannelHandlerContext ctx) {
        int userId = Integer.parseInt(parts[2]);
        String[] query = parts[parts.length - 1].split("\\?");
        String fromDateStr = null;
        String toDateStr = null;
        String country = null;
        String toDistanceStr = null;
        switch (query.length) {
            case 1:
                break;
            case 2:
                String[] params = query[1].split("&");
                for (int i = 0; i < params.length; i++) {
                    String p = params[i];
                    switch (p.charAt(3)) {
                        case 'm':
                            fromDateStr = p.substring(9);
                            break;
                        case 'a':
                            toDateStr = p.substring(7);
                            break;
                        case 'n':
                            try {
                                country = java.net.URLDecoder.decode(p.substring(8), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
                                e.printStackTrace();
                                return;
                            }
                            break;
                        case 'i':
                            toDistanceStr = p.substring(11);
                            break;
                    }
                }
                break;
            default:
                writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
                return;
        }
        try {
            boolean hasFromDate = fromDateStr != null;
            long fromDate = 0;
            if (hasFromDate) fromDate = Long.parseLong(fromDateStr);
            boolean hasToDate = toDateStr != null;
            long toDate = 0;
            if (hasToDate) toDate = Long.parseLong(toDateStr);
            boolean hasDistance = toDistanceStr != null;
            int distance = 0;
            if (hasDistance) distance = Integer.parseInt(toDistanceStr);
            ArrayList<UserVisitData> userVisits = RepositoryProvider.repo.getUserVisits(userId,
                    hasFromDate, fromDate, hasToDate, toDate, country, hasDistance, distance);
            if (userVisits != null) {
                StringBuilder sb = new StringBuilder(200);
                sb.append("{\"visits\":[");
                userVisits.sort((a, b) -> Long.compare(a.visitedAt, b.visitedAt));
                int n = userVisits.size();
                for (int i = 0; i < n; i++) {
                    UserVisitData uv = userVisits.get(i);
                    sb.append(uv.toString());
                    if (i < n - 1) {
                        sb.append(',');
                    }
                }
                sb.append("]}");
                writeResponse(ctx, HttpResponseStatus.OK, sb.toString());
                return;
            } else {
                writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
                return;
            }
        } catch (Exception ex) {
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
        }
    }

    private void editVisit(String uri, ByteBuf content, final ChannelHandlerContext ctx) {
        int visitId = Integer.parseInt(uri.split("/")[2].split("\\?", 2)[0]);
        String data = content.toString(Charset.forName("UTF-8"));
        VisitUpdate update = RequestParser.parseEditVisit(data);
        if (update == null) {
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
            return;
        }
        update.visitId = visitId;
        boolean isUpdated;
        synchronized (visitLock) {
            isUpdated = RepositoryProvider.repo.editVisit(update);
        }
        if (isUpdated) {
            writeResponse(ctx, HttpResponseStatus.OK, "{}");
        } else {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
        }
    }

    private void newVisit(ByteBuf content, final ChannelHandlerContext ctx) {
        String data = content.toString(Charset.forName("UTF-8"));
        VisitData visit = RequestParser.parseNewVisit(data);
        if (visit != null) {
            boolean isInserted;
            synchronized (visitLock) {
                isInserted = RepositoryProvider.repo.addVisit(visit);
            }
            if (isInserted) {
                writeResponse(ctx, HttpResponseStatus.OK, "{}");
            } else {
                writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
            }
        } else {
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
        }
    }

    private void editLocation(String uri, ByteBuf content, final ChannelHandlerContext ctx) {
        int locationId = Integer.parseInt(uri.split("/")[2].split("\\?", 2)[0]);
        String data = content.toString(Charset.forName("UTF-8"));
        LocationUpdate update = RequestParser.parseEditLocation(data);
        if (update == null) {
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
            return;
        }
        update.locationId = locationId;
        boolean isUpdated;
        synchronized (locationLock) {
            isUpdated = RepositoryProvider.repo.editLocation(update);
        }
        if (isUpdated) {
            writeResponse(ctx, HttpResponseStatus.OK, "{}");
        } else {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
        }
    }

    private void newLocation(ByteBuf content, final ChannelHandlerContext ctx) {
        String data = content.toString(Charset.forName("UTF-8"));
        LocationData location = RequestParser.parseNewLocation(data);
        if (location != null) {
            boolean isInserted;
            synchronized (locationLock) {
                isInserted = RepositoryProvider.repo.addLocation(location);
            }
            if (isInserted) {
                writeResponse(ctx, HttpResponseStatus.OK, "{}");
            } else {
                writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
            }
        } else {
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
        }
    }

    private void editUser(String uri, ByteBuf content, final ChannelHandlerContext ctx) {
        int userId = Integer.parseInt(uri.split("/")[2].split("\\?", 2)[0]);
        String data = content.toString(Charset.forName("UTF-8"));
        UserUpdate update = RequestParser.parseEditUser(data);
        if (update == null) {
            writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
            return;
        }
        update.userId = userId;
        boolean isUpdated;
        synchronized (userLock) {
            isUpdated = RepositoryProvider.repo.editUser(update);
        }
        if (isUpdated) {
            writeResponse(ctx, HttpResponseStatus.OK, "{}");
        } else {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
        }
    }

    private void newUser(ByteBuf content, final ChannelHandlerContext ctx) {
        String data = content.toString(Charset.forName("UTF-8"));
        UserData user = RequestParser.parseNewUser(data);
        if (user != null) {
            boolean userAdded;
            synchronized (userLock) {
                userAdded = RepositoryProvider.repo.addUser(user);
            }
            if (userAdded) {
                writeResponse(ctx, HttpResponseStatus.OK, "{}");
            } else {
                writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, "{}");
            }
        } else {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
        }
    }

    private void getUser(String[] uriParts, final ChannelHandlerContext ctx) {
        String uid = uriParts[2].split("\\?", 2)[0];
        int userId;
        try {
            userId = Integer.parseInt(uid);
        } catch (Exception ex) {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
            return;
        }
        UserData user = RepositoryProvider.repo.getUser(userId);
        if (user != null) {
            writeResponse(ctx, HttpResponseStatus.OK, user.toString());
        } else {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
        }
    }

    private void getLocation(String[] uriParts, final ChannelHandlerContext ctx) {
        int locationId = Integer.parseInt(uriParts[2].split("\\?", 2)[0]);
        LocationData location = RepositoryProvider.repo.getLocation(locationId);
        if (location != null) {
            writeResponse(ctx, HttpResponseStatus.OK, location.toString());
        } else {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
        }
    }

    private void getVisit(String uri, final ChannelHandlerContext ctx) {
        int visitId = Integer.parseInt(uri.split("/")[2].split("\\?", 2)[0]);
        VisitData visit = RepositoryProvider.repo.getVisit(visitId);
        if (visit != null) {
            writeResponse(ctx, HttpResponseStatus.OK, visit.toString());
        } else {
            writeResponse(ctx, HttpResponseStatus.NOT_FOUND, "{}");
        }
    }

    /**
     * Writes a HTTP response.
     *
     * @param ctx     The channel context.
     * @param status  The HTTP status code.
     * @param content The response content.
     */
    private static void writeResponse(
            final ChannelHandlerContext ctx,
            final HttpResponseStatus status,
            final String content) {

        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        final ByteBuf entity = Unpooled.wrappedBuffer(bytes);

        // Build the response object.
        final FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status, entity, false);

        final DefaultHttpHeaders headers = (DefaultHttpHeaders) response.headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        headers.set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(bytes.length));

        // Close the non-keep-alive connection after the write operation is done.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Handles an exception caught.  Closes the context.
     *
     * @param ctx   The channel context.
     * @param cause The exception.
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        System.out.println(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
