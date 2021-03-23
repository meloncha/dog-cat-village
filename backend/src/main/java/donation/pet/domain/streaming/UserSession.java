package donation.pet.domain.streaming;

import lombok.RequiredArgsConstructor;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

@RequiredArgsConstructor
public class UserSession {

    private final WebSocketSession session;
    private WebRtcEndpoint webRtcEndpoint;

    public WebSocketSession getSession() { return session; }

    public WebRtcEndpoint getWebRtcEndpoint() { return webRtcEndpoint; }

    public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) { this.webRtcEndpoint = webRtcEndpoint; }

    public void addCandidate(IceCandidate candidate) { webRtcEndpoint.addIceCandidate(candidate);}

//    public void sendMessage(JsonObject message) throws IOException {
//        session.sendMessage(new TextMessage(message.toString()));
//    }
}
