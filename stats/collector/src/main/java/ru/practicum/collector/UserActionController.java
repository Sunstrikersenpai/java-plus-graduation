package ru.practicum.collector;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.message.UserActionProto;
import ru.practicum.ewm.stats.proto.service.collector.UserActionControllerGrpc;

@GrpcService
@AllArgsConstructor
@Slf4j
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionHandler handler;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            handler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC ERROR collectUserAction: request={}", request, e);

            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("collector failed" + e.getClass().getSimpleName())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }
}
