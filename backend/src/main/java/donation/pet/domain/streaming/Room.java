package donation.pet.domain.streaming;

import org.kurento.client.MediaPipeline;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class Room {

    private UserSession presenter;
    private MediaPipeline pipeline;
    private String roomName;
    private LocalDateTime startTime;
    // Map<userId(Viewer), Viewer>
    private final ConcurrentHashMap<String, UserSession> viewers = new ConcurrentHashMap<>();

    public Room(UserSession presenter, MediaPipeline pipeline) {
        this.presenter = presenter;
        this.pipeline = pipeline;
    }
}
