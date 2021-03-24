package donation.pet.domain.streaming;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RoomRepository {

    // <userId, Room>
    private static final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

}
