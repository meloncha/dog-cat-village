package donation.pet.domain.streaming;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.kurento.client.MediaPipeline;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Room {

    private UserSession presenter;
    private MediaPipeline pipeline;
    private String roomName;
    private LocalDateTime startTime;
    // Map<SessionId(Consumer), Viewer>
    private final ConcurrentHashMap<String, UserSession> viewers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> sessions = new ConcurrentHashMap<>();

    public Room(UserSession presenter, MediaPipeline pipeline, String roomName) {
        this.presenter = presenter;
        this.pipeline = pipeline;
        this.roomName = roomName;
        this.startTime = LocalDateTime.now();
    }

    public UserSession findViewerBySessionId(String sessionId) {
        return viewers.get(sessionId);
    }

    public void saveViewer(Long memberId, String sessionId, UserSession session) {
        sessions.put(memberId, sessionId);
        viewers.put(sessionId, session);
    }

    public void delete() throws IOException {
        for (UserSession viewer : viewers.values()) {
            JsonObject response = new JsonObject();
            response.addProperty("id", "stopCommunication");
            viewer.sendMessage(response);
        }

        if (pipeline != null) {
            pipeline.release();
        }
        pipeline = null;
        presenter = null;
    }

    public void disconnect(String sessionId) {
        if (viewers.size() != 0 && viewers.containsKey(sessionId)) {
            viewers.get(sessionId).getWebRtcEndpoint().release();
        } else {
            return;
        }

        viewers.remove(sessionId);

        Long consumerId = null;
        for (Map.Entry<Long, String> map : sessions.entrySet()) {
            if (map.getValue().contains(sessionId)) {
                consumerId = map.getKey();
            }
        }

        if (consumerId != null) {
            sessions.remove(consumerId);
        }
    }
}