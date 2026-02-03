package de.kseek.game.common;

/**
 * 游戏示例消息号常量
 * Login=1000, Hall=2000
 */
public final class GameMessageConst {

    /** 登录模块消息类型 */
    public static final int LOGIN_TYPE = 1000;
    /** 登录请求 */
    public static final int LOGIN_REQ = 1;
    /** 登录响应 */
    public static final int LOGIN_RESP = 2;

    /** 大厅模块消息类型 */
    public static final int HALL_TYPE = 2000;
    /** 进入大厅请求 */
    public static final int HALL_ENTER_REQ = 1;
    /** 进入大厅响应 */
    public static final int HALL_ENTER_RESP = 2;
    /** 大厅列表请求 */
    public static final int HALL_LIST_REQ = 3;
    /** 大厅列表响应 */
    public static final int HALL_LIST_RESP = 4;

    private GameMessageConst() {
    }
}
