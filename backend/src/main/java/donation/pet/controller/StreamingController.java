package donation.pet.controller;

import donation.pet.dto.streaming.StreamingListResDto;
import donation.pet.dto.streaming.StreamingResDto;
import donation.pet.service.StreamingService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/live")
public class StreamingController {

    private final StreamingService streamingService;

    @ApiOperation("방송 리스트")
    @GetMapping("/list")
    public ResponseEntity<List<StreamingListResDto>> getStreamingList() {
        List<StreamingListResDto> streamingListResDtos = streamingService.getList(1L);
        return ResponseEntity.status(HttpStatus.OK).body(streamingListResDtos);
    }
}
