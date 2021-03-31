package donation.pet.domain.streaming;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomRepository {

    // <sessionId(Shelter), Room>
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> sessions = new ConcurrentHashMap<>();

    public void getRoomList() {

    }

    public void save(Long id, String key, Room room) {
        sessions.put(id, key);
        rooms.put(key, room);
    }

    public Room findBySessionId(String key) {
        return rooms.get(key);
    }

    public Room findByMemberId(Long id) {
        String sessionsId = sessions.get(id);
        if (sessionsId != null) {
            return rooms.get(sessionsId);
        }

        return null;
    }


    public void deleteRoom(String sessionId) throws IOException {
        Room room = rooms.get(sessionId);
        room.delete();

        Long memberId = null;
        for (Map.Entry<Long, String> map : sessions.entrySet()) {
            if (map.getValue().equals(sessionId)) {
                memberId = map.getKey();
            }
        }

        if (memberId != null) {
            sessions.remove(memberId);
        }
    }

    public void disconnectAll(String sessionId) {
        for (Room room : rooms.values()) {
            room.disconnect(sessionId);
        }
    }
}
