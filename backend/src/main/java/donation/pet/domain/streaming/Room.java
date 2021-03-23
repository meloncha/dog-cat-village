package donation.pet.domain.streaming;

import org.kurento.client.MediaPipeline;

import java.util.concurrent.ConcurrentHashMap;

public class Presenter {

    private UserSession userSession;
    private MediaPipeline pipeline;
    private final ConcurrentHashMap<String, Viewer> viewers = new ConcurrentHashMap<>();

    public Presenter(UserSession userSession, MediaPipeline pipeline) {
        this.userSession = userSession;
        this.pipeline = pipeline;
    }
}
}
