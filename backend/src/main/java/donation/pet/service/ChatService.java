package donation.pet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import donation.pet.domain.chat.ChatMessage;
import donation.pet.domain.member.Member;
import donation.pet.domain.member.MemberRepository;
import donation.pet.dto.chat.*;
import donation.pet.exception.BaseException;
import donation.pet.exception.ErrorCode;
import donation.pet.exception.FunctionWithException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    private ListOperations<String, String> listOps;
    private ValueOperations<String, String> valOps;

    public ChatCheckResponseDto check(ChatCheckRequestDto dto) throws JsonProcessingException {

        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        String roomId = null;

        Long myId = dto.getMyId();
        Long oppId = dto.getOppId();
        Member myMember = memberRepository.findById(myId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR));
        Member oppMember = memberRepository.findById(oppId)
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR));

        Boolean isRoom = setOps.isMember("checkRoomKey:" + myId, oppId +"");
        if(isRoom != null && !isRoom) {
            setOps.add("checkRoomKey:" + myId, oppId + "");
            setOps.add("checkRoomKey:" + oppId, myId + "");
            roomId = makeRoomId(myMember, oppMember);
        }else {
            valOps = redisTemplate.opsForValue();
            String roomInfoString = valOps.get("roomInfo:" + myId + ":" + oppId);
            RoomInfo roomInfo = objectMapper.readValue(roomInfoString, RoomInfo.class);
            roomId = roomInfo.getRoomId();
        }

        return new ChatCheckResponseDto(roomId);
    }

    // ????????? ????????????
    public String makeRoomId(Member myMember, Member oppMember) throws JsonProcessingException {

        String roomId = UUID.randomUUID().toString();

        valOps = redisTemplate.opsForValue();

        // ??? ????????? ??????
        String myRoomStr = objectMapper.writeValueAsString(makeRoomInfoOtherMember(roomId,oppMember));
        String oppRoomStr = objectMapper.writeValueAsString(makeRoomInfoOtherMember(roomId,myMember));

        // ????????? ??????
        valOps.set("roomInfo:" + myMember.getId() + ":" + oppMember.getId(), myRoomStr);
        valOps.set("roomInfo:" + oppMember.getId() + ":" + myMember.getId(), oppRoomStr);

        return roomId;

    }
    
    // dto??? ?????? ??????
    private RoomInfo makeRoomInfoOtherMember(String roomId, Member member) {
        return RoomInfo.builder()
                .oppProfileImage(member.getProfileImage())
                .oppName(member.getName())
                .oppId(member.getId())
                .roomId(roomId)
                .used(1)
                .build();
    }

    /*
     * ????????? ?????? ????????????
     * */
    public List<ChatRoomInfoDto> getRoomList(String memberId) throws JsonProcessingException {

        Set<String> keys = redisTemplate.keys("roomInfo:" + memberId + ":*");
        if (keys == null) {
            return new ArrayList<ChatRoomInfoDto>();
        }

        valOps = redisTemplate.opsForValue();

        return keys.stream()
                .map(key -> valOps.get(key))
                .map(wrapper(roomInfoStr -> objectMapper.readValue(roomInfoStr, RoomInfo.class)))
                .map(wrapper(roomInfo -> ChatRoomInfoDto.builder()
                        .recentMsg(getRecentMessage(roomInfo.getRoomId())) // ????????? ???????????? ???????????? ????????? ?????????
                        .oppId(roomInfo.getOppId())
                        .oppName(roomInfo.getOppName())
                        .roomId(roomInfo.getRoomId())
                        .oppId(roomInfo.getOppId())
                        .build())).collect(Collectors.toList());

//        List<ChatRoomInfoDto> roomList = new ArrayList<>();
//        for (String oppId : keys) {
//            String roomInfoStr = valOps.get(oppId);
//            Map<String, String> roomInfoObj = objectMapper.readValue(roomInfoStr, Map.class);
//            log.info("room??? ??????????????? ????????? ????????? ?????? : {} ", roomInfoStr);
//            String roomId = roomInfoObj.get("roomId");
//            String oppName = roomInfoObj.get("oppName");
//
//            String recentMsg = getRecentMessage(roomId); // ????????? ???????????? ???????????? ????????? ?????????
//
//            ChatRoomInfoDto dto = ChatRoomInfoDto.builder()
//                    .roomId(roomId)
//                    .oppName(oppName)
//                    .recentMsg(recentMsg).build();
//            roomList.add(dto);
//        }

    }

    /*
     * ?????? ????????? ????????????
     * */
    public String getRecentMessage(String roomId) throws JsonProcessingException {
        listOps = redisTemplate.opsForList();
        List<String> stringList = listOps.range("message:" + roomId, 0, 0);
        if (stringList == null || stringList.isEmpty()) {
            return "";
        }
        return objectMapper.readValue(stringList.get(0), ChatMessage.class).getMsg();
    }

    /*
    * ?????? ????????????
    * */
    public ChatNoticeResponseDto getNotice(String memberId) {

        Set<String> keys = redisTemplate.keys("notice:" + memberId + ":*");

        if(keys == null) {
            return new ChatNoticeResponseDto(new ArrayList<>());
        }

        valOps = redisTemplate.opsForValue();
        // ????????? ?????? ???????????? ?????? ??????
        List<ChatNoticeDto> collect = keys.stream().filter(key -> Integer.parseInt(Objects.requireNonNull(valOps.get(key))) != 0)
                .map(key -> {
                    Member member = memberRepository.findById(Long.parseLong(key.split(":")[2]))
                            .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
                    return ChatNoticeDto.builder()
                            .count(Long.valueOf(Objects.requireNonNull(valOps.get(key))))
                            .oppName(member.getName())
                            .build();
                }).collect(Collectors.toList());

        return new ChatNoticeResponseDto(collect);
    }

    /*
    * ?????? ?????????
    * */
    public void updateNotice(String myId, String oppId) {
        String key = "notice:" + myId + ":" + oppId;
        valOps = redisTemplate.opsForValue();
        valOps.set(key, "0");
    }

    public void deleteRoom(String myId, String oppId) {
        // ???????????????..
    }

    /*
    * ????????? ?????? ????????????
    * */
    public ChatDetailDto getMessageList(int startNum, int endNum, String roomId, String myId, String oppId) throws JsonProcessingException {

        Member member = memberRepository.findById(Long.parseLong(oppId))
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // ????????? ??????
        String roomStr = "message:" + roomId;
        List<ChatMessageDto> collect = Objects.requireNonNull(redisTemplate.opsForList().range(roomStr, startNum, endNum)).stream()
                .map(wrapper(s -> objectMapper.readValue(s, ChatMessageDto.class)))
                .collect(Collectors.toList());

        log.info("????????? ??????");
        return new ChatDetailDto(roomId, myId, oppId, member.getName(), collect);

    }

    /*
    * /receive
    * */
    public void receiveMsg(ChatMessageDto message) throws JsonProcessingException {
        String roomId = message.getRoomId();
        String oppId = message.getOppId();
        String myId = message.getMyId();
        String oppName = message.getOppName();

        // ????????? ?????? ?????? ????????????
        log.info("?????????: {} ", message.getDate());
        valOps = redisTemplate.opsForValue();
        String key = "notice:" + oppId + ":" + myId;
        // ????
        valOps.increment(key, 1);
        // {count: 1, nickname:""}
        Map<String, String> notices = new HashMap<>();
        notices.put("oppName", oppName);
        simpMessagingTemplate.convertAndSend("/notice/" + oppId, notices);
        insertMessage(message); // ????????? ??????

        simpMessagingTemplate.convertAndSend("/message/" + roomId, message);

    }

    /*
     * ????????? ????????????
     * */
    public void insertMessage(ChatMessageDto message) throws JsonProcessingException {
        String key = "message:" + message.getRoomId();
        listOps = redisTemplate.opsForList();
        log.info("key:{}", key);
        log.info("messages:{}", message.getMsg());
        String strMsg = objectMapper.writeValueAsString(message);
        listOps.rightPush(key, strMsg);
    }

    // ????????? ??? try catch ?????? ????????? ?????? ??????
    private <T, R, E extends Exception> Function<T, R> wrapper(FunctionWithException<T, R, E> fe) {
        return arg -> {
            try {
                return fe.apply(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}