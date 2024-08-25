package com.hayan.Account.redis;

import com.hayan.Account.exception.CustomException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.Socket;

import static com.hayan.Account.exception.ErrorCode.NO_AVAILABLE_PORT;

@Profile({"local", "embedded-test"})
@Configuration
public class RedisEmbeddedConfig {
    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    private void start() throws IOException {
        int port = isRedisRunning() ? findAvailablePort() : redisPort;
        redisServer = new RedisServer(port);
        redisServer.start();
    }

    @PreDestroy
    private void stop() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    private boolean isRedisRunning() {
        try (Socket socket = new Socket("localhost", redisPort)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int findAvailablePort() {
        for (int port = 10000; port <= 65535; port++) {
            if (!isPortInUse(port)) {
                return port;
            }
        }
        throw new CustomException(NO_AVAILABLE_PORT);
    }

    private boolean isPortInUse(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
