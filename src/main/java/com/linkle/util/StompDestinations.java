package com.linkle.util;

/**
 * STOMP 목적지(토픽/큐) 상수 & 빌더
 * WebSocketConfig:
 *   registry.enableSimpleBroker("/sub", "/queue");
 *   registry.setApplicationDestinationPrefixes("/app");
 */
public final class StompDestinations {
    private StompDestinations() {}

    // ---- 브로커/앱 프리픽스 ----
    public static final String APP_PREFIX   = "/app";   // 클라이언트가 서버로 보낼 때
    public static final String SUB_PREFIX   = "/sub";   // 서버가 브로커로 브로드캐스트
    public static final String QUEUE_PREFIX = "/queue"; // 1:1 개인 큐

    // ---- 방 브로드캐스트 (구독 경로) ----
    // 예) /sub/room.123
    public static final String ROOM_TOPIC_PREFIX = SUB_PREFIX + "/room.";
    public static String room(long roomId) { return ROOM_TOPIC_PREFIX + roomId; }

    // ---- 유저 개인 큐 (선택) ----
    // 예) /queue/user.7
    public static final String USER_QUEUE_PREFIX = QUEUE_PREFIX + "/user.";
    public static String user(long userId) { return USER_QUEUE_PREFIX + userId; }

    // ---- 앱 수신 목적지 (클라 → 서버) 공용 정의 ----
    // 컨트롤러 @MessageMapping 경로는 APP_PREFIX를 뺀 "상대 경로"를 씀
    // 예) @MessageMapping("message.send")  <-> 클라 전송 목적지 "/app/message.send"
    public static final String APP_MESSAGE_SEND      = APP_PREFIX + "/message.send";
    public static final String APP_MESSAGE_SEND_PATH = "message.send";

    // 사용 예: 읽음 동기화도 STOMP로 받는다면
    public static final String APP_READ_SYNC      = APP_PREFIX + "/read.sync";
    public static final String APP_READ_SYNC_PATH = "read.sync";

    // ---- 기타 ----
    /** 임의 목적지를 그대로 사용해야 할 때 */
    public static String of(String raw) { return raw; }
}
