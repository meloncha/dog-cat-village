package donation.pet.service;

import donation.pet.domain.streaming.RoomRepository;
import donation.pet.dto.streaming.StreamingListResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final RoomRepository roomRepository;

    public List<StreamingListResDto> getList(Long userId) {

        return null;
    }
}
