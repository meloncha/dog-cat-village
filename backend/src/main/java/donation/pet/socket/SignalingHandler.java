package donation.pet.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import donation.pet.domain.member.Member;
import donation.pet.domain.member.MemberRepository;
import donation.pet.domain.member.MemberRole;
import donation.pet.domain.streaming.Room;
import donation.pet.domain.streaming.RoomRepository;
import donation.pet.domain.streaming.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Slf4j
public class SignalingHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private KurentoClient kurento;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.debug("Incoming message from session : {} : {}", session.getId(), jsonMessage);

        switch (jsonMessage.get("id").getAsString()) {
            case "presenter":
                try {
                    presenter(session, jsonMessage);
                } catch (Throwable t) {
                    handleErrorResponse(t, session, "presenterResponse");
                }
                break;
            case "viewer":
                try {
                    viewer(session, jsonMessage);
                } catch (Throwable t) {
                    handleErrorResponse(t, session, "viewerResponse");
                }
                break;
            case "onIceCandidate":
                log.info("handleTextMessage - send data : onIceCandidate");
                JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

                Room room = roomRepository.findBySessionId(session.getId());
                UserSession user = null;

                if (room != null) { // presenter
                    UserSession presenterUserSession = room.getPresenter();
                    if (presenterUserSession.getSession() == session) {
                        user = presenterUserSession;
                    } else {
                    }
                }

                break;
            case "stop":
                try {
                    Long shelterId = jsonMessage.get("joinId").getAsLong();
                    stop(session, shelterId);
                } catch (NullPointerException e) {
                    stop(session);
                }
                break;
            default:
                break;
        }
    }

    private void handleErrorResponse(Throwable throwable, WebSocketSession session, String responseId)
            throws IOException {
        stop(session);
        log.error(throwable.getMessage(), throwable);
        JsonObject response = new JsonObject();
        response.addProperty("id", responseId);
        response.addProperty("response", "rejected");
        response.addProperty("message", throwable.getMessage());
        session.sendMessage(new TextMessage(response.toString()));
    }

    private synchronized void presenter(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {

        // 방의 유무확인
        Room resultRoom = roomRepository.findBySessionId(session.getId());
        Long id = jsonMessage.get("myId").getAsLong();
        Member member = memberRepository.findById(id).orElse(null);

        // 만든 방이 없으면
        if (resultRoom == null && member != null && member.getRoles().contains(MemberRole.SHELTER)) {

            log.info("presenter - new UserSession :{}", session.getId());

            // UserSession 생성
            UserSession presenterUserSession = new UserSession(session);
            // Pipe 생성
            MediaPipeline pipeline = kurento.createMediaPipeline();

            // userRTCEndpoint을 써줘야 dataChannel을 이용 가능
            presenterUserSession.setWebRtcEndpoint(new WebRtcEndpoint.Builder(pipeline).useDataChannels().build());
            WebRtcEndpoint presenterWebRtc = presenterUserSession.getWebRtcEndpoint();

            // 실제 Presenter와 Kurento가 생성한 미디어 파이프라인 사이에 연결
            // addIceCandidateFoundListener를 사용하여 Candidate를 찾는 과정이 이루어 짐
            presenterWebRtc.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {
                @Override
                public void onEvent(IceCandidateFoundEvent event) {
                    JsonObject response = new JsonObject();
                    response.addProperty("id", "iceCandidate");
                    response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                    try {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(response.toString()));
                        }
                    } catch (IOException e) {
                        log.debug(e.getMessage());
                    }
                }
            });

            String sdpOffer = jsonMessage.getAsJsonPrimitive("adpOffer").getAsString();
            String sdpAnswer = presenterWebRtc.processOffer(sdpOffer);

            JsonObject response = new JsonObject();
            response.addProperty("id", "presenterResponse");
            response.addProperty("response", "accepted");
            response.addProperty("sdpAnswer", sdpAnswer);

            synchronized (session) {
                presenterUserSession.sendMessage(response);
            }
            presenterWebRtc.gatherCandidates();

            // 방 만들기
            Room room = new Room(presenterUserSession, pipeline, session.getId());
            roomRepository.save(id, session.getId(), room);

        } else {    // 이미 방이 있을 때
            JsonObject response = new JsonObject();
            response.addProperty("id", "presenterResponse");
            response.addProperty("response", "rejected");
            response.addProperty("message",
                "방을 만들 수 없습니다.");
            session.sendMessage(new TextMessage(response.toString()));
        }
    }


    private synchronized void viewer(final WebSocketSession session, JsonObject jsonMessage)
            throws IOException {

        // 들어갈 방
        Long shelterId = jsonMessage.get("joinId").getAsLong();
        Long consumerId = jsonMessage.get("myId").getAsLong();
        Room room = roomRepository.findByMemberId(shelterId);
        Member member = memberRepository.findById(consumerId).orElse(null);

        if (room != null && member != null && member.getRoles().contains(MemberRole.CONSUMER)) {

            UserSession viewer = room.findViewerBySessionId(session.getId());

            if (viewer != null) {
                JsonObject response = new JsonObject();
                response.addProperty("id", "viewerResponse");
                response.addProperty("response", "rejected");
                response.addProperty("message", "이미 참여중인 방송입니다.");
                session.sendMessage(new TextMessage(response.toString()));
            } else {

                viewer = new UserSession(session);
                room.saveViewer(consumerId, session.getId(), viewer);

                // presenter의 파이프라인을 가져오기
                MediaPipeline pipeline = room.getPresenter().getWebRtcEndpoint().getMediaPipeline();
                // viewer와 webRtcEndPoint를 만들어 준다.
                WebRtcEndpoint nextWebRtc = new WebRtcEndpoint.Builder(pipeline).useDataChannels().build();

                nextWebRtc.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {
                    @Override
                    public void onEvent(IceCandidateFoundEvent event) {
                        JsonObject response = new JsonObject();
                        response.addProperty("id", "iceCandidate");
                        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                        try {
                            synchronized (session) {
                                session.sendMessage(new TextMessage(response.toString()));
                            }
                        } catch (IOException e) {
                            log.debug(e.getMessage());
                        }
                    }
                });

                viewer.setWebRtcEndpoint(nextWebRtc);
                room.getPresenter().getWebRtcEndpoint().connect(nextWebRtc);
                String sdpOffer = jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString();
                String sdpAnswer = nextWebRtc.processOffer(sdpOffer);

                JsonObject response = new JsonObject();
                response.addProperty("id", "viewerResponse");
                response.addProperty("response", "accepted");
                response.addProperty("sdpAnswer", sdpAnswer);

                synchronized (session) {
                    viewer.sendMessage(response);
                }
                nextWebRtc.gatherCandidates();
            }
        } else {    // Consumer가 아닐 때
            JsonObject response = new JsonObject();
            response.addProperty("id", "viewerResponse");
            response.addProperty("response", "rejected");
            response.addProperty("message", "방송을 참여할 수 없습니다.");
            session.sendMessage(new TextMessage(response.toString()));
        }
    }

    private synchronized void stop(WebSocketSession session) throws IOException {

        Room room = roomRepository.findBySessionId(session.getId());

        if (room != null) {     // presenter
            roomRepository.deleteRoom(session.getId());
        } else {                // All disconnect
            roomRepository.disconnectAll(session.getId());
        }
    }

    private synchronized void stop(WebSocketSession session, Long presenterId) throws IOException {
        Room room = roomRepository.findByMemberId(presenterId);
        if (room != null) {
            room.disconnect(session.getId());
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        stop(session);
    }
}
