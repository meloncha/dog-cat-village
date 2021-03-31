package donation.pet.domain.streaming;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class UserSession {

    private final WebSocketSession session;
    private WebRtcEndpoint webRtcEndpoint;

    public UserSession(WebSocketSession session) {
        this.session = session;
    }

    public WebSocketSession getSession() { return session; }

    public WebRtcEndpoint getWebRtcEndpoint() { return webRtcEndpoint; }

    public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) { this.webRtcEndpoint = webRtcEndpoint; }

    public void addCandidate(IceCandidate candidate) { webRtcEndpoint.addIceCandidate(candidate);}

    public void sendMessage(JsonObject message) throws IOException {
        session.sendMessage(new TextMessage(message.toString()));
    }
}
