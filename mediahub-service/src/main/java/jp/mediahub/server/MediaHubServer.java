package jp.mediahub.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import java.io.IOException;
import jp.mediahub.service.GRPCMediaServiceAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Main class for the Woven Storage Server application.
 * <p>
 * Annotated with {@link EnableAspectJAutoProxy} is used to enable AspectJ support in the Spring application, used to
 * assess performance in certain methods
 * <p>
 * This class also implements the {@link CommandLineRunner} interface, allowing it to run the GRPCserver after the
 * Spring Boot application has been fully started.
 *
 * @author Renato Raeffray
 *
 *
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan("jp.mediahub")
public class MediaHubServer implements CommandLineRunner {

  @Value("${server.port}")
  private int RPC_SERVER_PORT;

  @Autowired
  private GRPCMediaServiceAPI mediaServiceApi;

  public static void main(String... args) throws IOException, InterruptedException {
    SpringApplication.run(MediaHubServer.class, args);
  }

  @Override
  public void run(String... args) throws Exception {

    Server server = ServerBuilder
        .forPort(RPC_SERVER_PORT)
        .addService(ProtoReflectionService.newInstance())
        .addService(mediaServiceApi)
        .build();

    server.start();
    server.awaitTermination();

  }

}
