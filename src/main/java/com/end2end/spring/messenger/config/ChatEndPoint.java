package com.end2end.spring.messenger.config;

import com.end2end.spring.employee.dto.EmployeeDTO;
import com.end2end.spring.messenger.dto.*;
import com.end2end.spring.messenger.service.MessengerService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/chat", configurator = WebSocketConfig.class)
public class ChatEndPoint {

    private MessengerService messengerService = SpringProvider.Spring.getBean(MessengerService.class);

    // 사용자 ID로 세션 매핑 (동시성 처리)
    private static final Map<String, Session> clientSessions = new ConcurrentHashMap<>();
    // 사번 : {방번호: 안읽은 갯수}
    private static final Map<String, Map<Integer, Integer>> alarmCounts = new ConcurrentHashMap<>();

    private HttpSession hSession = null;
    private EmployeeDTO dto = null;
    private final Gson gson = new Gson();

    @OnOpen
    public void onConnect(Session session, EndpointConfig config) throws Exception{
        this.hSession = (HttpSession) config.getUserProperties().get("hSession");
        if (hSession == null) {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY,"익명유저"));
            return;
        }
        dto = (EmployeeDTO) hSession.getAttribute("employee");
        if (dto != null) {
//            System.out.println("유저 추가됨 : " + dto.getName() + " session ID : " + session.getId());
            clientSessions.put(dto.getId(), session); // 사용자 ID로 세션 매핑
            alarmCounts.putIfAbsent(dto.getId(), new ConcurrentHashMap<>());
//            System.out.println("알람 Map 만들어짐" + alarmCounts.get(dto.getId()));
            sendTotalAlarmCount(dto.getId());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        JsonObject parsedMessage = gson.fromJson(message, JsonObject.class);
//        System.out.println(parsedMessage);
        String type = parsedMessage.has("type") ? parsedMessage.get("type").getAsString() : "";

        int roomId = 0;
        if (parsedMessage.has("roomId") && !parsedMessage.get("roomId").getAsString().trim().isEmpty()) {
            roomId = Integer.parseInt(parsedMessage.get("roomId").getAsString());
        }

        switch (type) {
            case "roomEnter": //방 입장
                resetRoomAlarm(dto.getId(), roomId); // 해당 방 알람 초기화
                loadAndSendChatHistory(parsedMessage, roomId);
                break;
            case "newRoom": //새로운 방
                addEmployeeToRoom(parsedMessage, roomId);
                break;
            case "invite": //방 초대 - 미완성
                processRoomInvitation(parsedMessage, roomId);
                break;
            case "message": //메세지전송
                processAndBroadcastMessage(parsedMessage, roomId);
                break;
            default:
//                System.out.println("기본출력");
        }
    }

    // newRoom
    public void addEmployeeToRoom(JsonObject parsedMessage, int roomId) throws IOException {
        String employeeId = parsedMessage.get("employeeId").getAsString();
        String id1 = dto.getId();
        String id2 = employeeId;
        long num1 = Long.parseLong(id1);
        long num2 = Long.parseLong(id2);
        String roomName = (num1 < num2) ? id1 + "|" + id2 : id2 + "|" + id1;
        int selectByName = messengerService.selectByName(roomName);
        if (selectByName == 0) {
            Map<String, Object> result = messengerService.createChatRoom(employeeId, dto.getId(), roomName);
            String newRoomId = result.get("messageRoomId").toString();
            String messageRoomUserId = result.get("messageRoomUserId").toString();
            JsonObject response = new JsonObject();
            response.addProperty("type", "NEW_CHAT_ROOM");
            response.addProperty("roomId", newRoomId);
            response.addProperty("messageRoomUserId", messageRoomUserId);
            try {
                clientSessions.get(dto.getId()).getBasicRemote().sendText(gson.toJson(response));
            } catch (NullPointerException ignored) {
            }
        } else {
            int newRoomId = messengerService.findByRoomId(roomName);
            JsonObject response = new JsonObject();
            response.addProperty("type", "NEW_CHAT_ROOM");
            response.addProperty("roomId", newRoomId);
            try {
                clientSessions.get(employeeId).getBasicRemote().sendText(gson.toJson(response));
                clientSessions.get(dto.getId()).getBasicRemote().sendText(gson.toJson(response));
            } catch (NullPointerException ignored) {}
        }
    }
    // invite
    public void processRoomInvitation(JsonObject parsedMessage, int roomId) throws Exception {
        List<MessageRoomDTO> dtos = messengerService.findByRoomId2(roomId); //select * from message_room where id = roomId
        //인원이 몇명인지 체크
        for (MessageRoomDTO dto : dtos) {
            if (dto.getEmployeeId().equals(parsedMessage.get("inviteeId").getAsString())) {
                return;
            }
        }
        // 처음 들어온 경우
        messengerService.insertUser(roomId, parsedMessage.get("inviteeId").getAsString());
        messengerService.insertUsertoRoom(roomId, parsedMessage.get("inviteeId").getAsString());
        List<String> roomEmployeeList = messengerService.findByRoomEmployeeList(roomId);
        String inviterName = this.dto.getName();
        String inviteeName = parsedMessage.get("inviteeName").getAsString();
        String notificationMessage = inviterName + "님이 " + inviteeName + "님을 초대하였습니다.";

        JsonObject response = new JsonObject();
        response.addProperty("type", "NEW_INVITE");
        response.addProperty("message", notificationMessage);
        response.add("employees", gson.toJsonTree(roomEmployeeList));
        response.addProperty("roomId", roomId);

        List<MessageUserListDTO> dtos2 = messengerService.selectRoomById(roomId);
        for (MessageUserListDTO dto : dtos2) {
            String employeeId = dto.getEmployeeId();
            try {
                clientSessions.get(employeeId).getBasicRemote().sendText(gson.toJson(response));
            } catch (NullPointerException ignored) {
            }
        }
    }
    // roomEnter
    public void loadAndSendChatHistory(JsonObject parsedMessage, int roomId) throws IOException {
        // select count(*) from message_room where id = roomId;
        String senderId = dto.getId();
        List<MessageHistoryDTO> messages = messengerService.selectMessageByRoomId(roomId, senderId);
        List<String> roomEmployeeList = messengerService.findByRoomEmployeeList(roomId);

        String employeeId = parsedMessage.get("employeeId").getAsString();
        JsonObject response = new JsonObject();

        response.addProperty("type", "history");
        response.add("messages", gson.toJsonTree(messages));
        response.add("employees", gson.toJsonTree(roomEmployeeList));

        String jsonResponse = gson.toJson(response);
        try {
            clientSessions.get(employeeId).getBasicRemote().sendText(jsonResponse);
        } catch (NullPointerException ignored) {}
    }

    // message
    public void processAndBroadcastMessage(JsonObject parsedMessage, int roomId) throws IOException {
        MessageUserDTO dto = messengerService.selectUserByEmployeeIdAndRoomId(parsedMessage.get("employeeId").getAsString(), roomId);
        String senderEmployeeId = (dto != null) ? dto.getEmployeeId() : "unknown";
        String senderName = (dto != null) ? dto.getName() : "unknown";

        MessageDTO messageDTO = MessageDTO.builder()
                .employeeId(senderEmployeeId)
                .messagerRoomId(roomId)
                .messagerRoomuserId((dto != null) ? dto.getId() : 0)
                .content(parsedMessage.get("message").getAsString())
                .name(senderName)
                .build();
        messengerService.insertMessage(messageDTO);

        incrementAlarmCount(roomId, senderEmployeeId);

        List<MessageUserListDTO> dtos = messengerService.selectRoomById(roomId);
        for (MessageUserListDTO dto2 : dtos) {
            String employeeId = dto2.getEmployeeId();
            try {
                clientSessions.get(employeeId).getBasicRemote().sendText(gson.toJson(messageDTO));
            } catch (NullPointerException ignored) {
            }
        }
    }

    private void incrementAlarmCount(int roomId, String senderEmployeeId) {
        List<MessageUserListDTO> roomUsers = messengerService.selectRoomById(roomId);
        for (MessageUserListDTO user : roomUsers) {
            String employeeId = user.getEmployeeId();
            if (!employeeId.equals(senderEmployeeId)) {
                Map<Integer, Integer> userAlarms = alarmCounts.getOrDefault(employeeId, new ConcurrentHashMap<>());
                int currentCount = userAlarms.getOrDefault(roomId, 0);
                userAlarms.put(roomId, currentCount + 1);
                alarmCounts.put(employeeId, userAlarms);
                sendTotalAlarmCount(employeeId);
            }
        }
    }

    private void resetRoomAlarm(String employeeId, int roomId) {
        Map<Integer, Integer> userAlarms = alarmCounts.get(employeeId);
        if (userAlarms != null) {
            userAlarms.put(roomId, 0);
            sendTotalAlarmCount(employeeId);
        }
    }

    private void sendTotalAlarmCount(String employeeId) {
        Map<Integer, Integer> userAlarms = alarmCounts.getOrDefault(employeeId, new HashMap<>());
        int totalAlarms = userAlarms.values().stream().mapToInt(Integer::intValue).sum();

        JsonObject response = new JsonObject();
        response.addProperty("type", "alarmCount");
        response.addProperty("count", totalAlarms);

        JsonObject roomAlarms = new JsonObject();
        userAlarms.forEach((roomId, count) -> {
            roomAlarms.addProperty(roomId.toString(), count);
        });
        response.add("roomAlarms", roomAlarms);


        Session session = clientSessions.get(employeeId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(gson.toJson(response));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        //System.out.println("유저 나감 " + session.getId());
        clientSessions.values().removeIf(s -> s.equals(session));
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        clientSessions.values().removeIf(s -> s.equals(session));
        throwable.printStackTrace();
    }
}