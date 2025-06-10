package com.bfsforum.historyservice.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RequestReplyManager<T> {
    private final Map<String, CompletableFuture<T>> pendingRequests = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT_SECONDS = 1000;
    /**
     * Creates and stores a new CompletableFuture for a given correlation ID.
     * @param correlationId The unique ID for this request.
     * @return The CompletableFuture associated with this request.
     */
    public CompletableFuture<T> createAndStoreFuture(String correlationId) {
        CompletableFuture<T> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);
        return future;
    }

    /**
     * Completes a stored CompletableFuture with the received reply payload.
     *
     * @param correlationId The correlation ID of the request.
     * @param replyPayload The object received as a reply.
     * @return The CompletableFuture with replyPayload, or null if not found.
     */
    public void completeFuture(String correlationId, T replyPayload) {
        CompletableFuture<T> future = pendingRequests.get(correlationId);
        if (future != null) {
            future.complete(replyPayload);
            log.debug("Completed future for correlationId: {}", correlationId);
        } else {
            log.warn("No pending request found for correlationId: {}. Possibly timed out or duplicate response.", correlationId);
        }
    }

    /**
     * Awaits the completion of a CompletableFuture with a default timeout.
     * Cleans up the map entry regardless of success or failure.
     *
     * @param correlationId The correlation ID of the request.
     * @param future The CompletableFuture to await.
     * @return The completed reply payload.
     * @throws RuntimeException if the future times out or completes exceptionally.
     */
    public T awaitFuture(String correlationId, CompletableFuture<T> future) {
        return awaitFuture(correlationId, future, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Awaits the completion of a CompletableFuture with a specified timeout.
     * Cleans up the map entry regardless of success or failure.
     *
     * @param correlationId The correlation ID of the request.
     * @param future The CompletableFuture to await.
     * @param timeoutSeconds The maximum time to wait in seconds.
     * @return The completed reply payload.
     * @throws RuntimeException if the future times out or completes exceptionally.
     */
    public T awaitFuture(String correlationId, CompletableFuture<T> future, long timeoutSeconds) {
        try {
            return future.orTimeout(timeoutSeconds, TimeUnit.SECONDS).get();
        } catch (TimeoutException e) {
            log.error("Timeout waiting for reply for correlationId: {}", correlationId);
            throw new RuntimeException("Reply lookup timed out", e);
        } catch (Exception e) {
            log.error("Error retrieving reply for correlationId: {}", correlationId, e);
            throw new RuntimeException("Failed to retrieve reply", e);
        } finally {
            pendingRequests.remove(correlationId); // Clean up the map
        }
    }
}
