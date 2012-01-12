package ua.snuk182.asia.services.mrim.inner;

public final class MrimConstants {
	
    public static final int CS_MAGIC = 0xDEADBEEF;
    public static final int PROTO_VERSION = 0x00010016;
    public static final int CONTACT_FLAG_GROUP = 0x02;
    public static final int CONTACT_FLAG_IGNORE = 0x10;
    public static final int CONTACT_FLAG_INVISIBLE = 0x04;
    public static final int CONTACT_FLAG_REMOVED = 0x01;
    public static final int CONTACT_FLAG_SHADOW = 0x20;
    public static final int CONTACT_FLAG_SMS = 0x100000;
    public static final int CONTACT_FLAG_VISIBLE = 0x08;
    public static final int CONTACT_INTFLAG_NOT_AUTHORIZED = 0x01;
    public static final int CONTACT_OPER_ERROR = 0x01;
    public static final int CONTACT_OPER_GROUP_LIMIT = 0x6;
    public static final int CONTACT_OPER_INTERR = 0x02;
    public static final int CONTACT_OPER_INVALID_INFO = 0x04;
    public static final int CONTACT_OPER_NO_SUCH_USER = 0x03;
    public static final int CONTACT_OPER_SUCCESS = 0x00;
    public static final int CONTACT_OPER_USER_EXISTS = 0x05;
    public static final int FILE_TRANSFER_MIRROR = 4;
    public static final int FILE_TRANSFER_STATUS_DECLINE = 0;
    public static final int FILE_TRANSFER_STATUS_ERROR = 2;
    public static final int FILE_TRANSFER_STATUS_INCOMPATIBLE_VERS = 3;
    public static final int FILE_TRANSFER_STATUS_OK = 1;
    public static final int GET_CONTACTS_ERROR = 0x01;
    public static final int GET_CONTACTS_INTERR = 0x02;
    public static final int GET_CONTACTS_OK = 0x00;
    public static final int LOGOUT_NO_RELOGIN_FLAG = 0x10;
    public static final int MAX_CLIENT_DESCRIPTION = 256;
    public static final int MESSAGE_DELIVERED = 0x00;
    public static final int MESSAGE_FLAG_ALARM = 0x4000;
    public static final int MESSAGE_FLAG_AUTHORIZE = 0x08;
    public static final int MESSAGE_FLAG_CONTACT = 0x0200;
    public static final int MESSAGE_FLAG_MULTICAST = 0x1000;
    public static final int MESSAGE_FLAG_NORECV = 0x04;
    public static final int MESSAGE_FLAG_NOTIFY = 0x0400;
    public static final int MESSAGE_FLAG_OFFLINE = 0x01;
    public static final int MESSAGE_FLAG_OLD = 0x200000;
    public static final int MESSAGE_FLAG_RTF = 0x80;
    public static final int MESSAGE_FLAG_SMS = 0x0800;
    public static final int MESSAGE_FLAG_SMS_NOTIFY = 0x2000;
    public static final int MESSAGE_FLAG_SPAM = 0x010000;
    public static final int MESSAGE_FLAG_SYSTEM = 0x40;
    public static final int MESSAGE_FLAG_UNI = 0x100000;
    public static final int MESSAGE_REJECTED_DENY_OFFMSG = 0x8006;
    public static final int MESSAGE_REJECTED_INTERR = 0x8003;
    public static final int MESSAGE_REJECTED_LIMIT_EXCEEDED = 0x8004;
    public static final int MESSAGE_REJECTED_NOUSER = 0x8001;
    public static final int MESSAGE_REJECTED_TOO_LARGE = 0x8005;
    public static final int MESSAGE_USERFLAGS_MASK = 0x36A8;
    public static final int MRIM_ANKETA_INFO_STATUS_DBERR = 2;
    public static final int MRIM_ANKETA_INFO_STATUS_NOUSER = 0;
    public static final int MRIM_ANKETA_INFO_STATUS_OK = 1;
    public static final int MRIM_ANKETA_INFO_STATUS_RATELIMERR = 3;
    public static final int MRIM_CS_ADD_CONTACT = 0x1019;
    public static final int MRIM_CS_ADD_CONTACT_ACK = 0x101A;
    public static final int MRIM_CS_ANKETA_INFO = 0x1028;
    public static final int MRIM_CS_AUTHORIZE = 0x1020;
    public static final int MRIM_CS_AUTHORIZE_ACK = 0x1021;
    public static final int MRIM_CS_CHANGE_STATUS = 0x1022;
    public static final int MRIM_CS_CONNECTION_PARAMS = 0x1014;
    public static final int MRIM_CS_CONTACT_LIST2 = 0x1037;
    public static final int MRIM_CS_DELETE_OFFLINE_MESSAGE = 0x101E;
    public static final int MRIM_CS_FILE_TRANSFER = 0x1026;
    public static final int MRIM_CS_FILE_TRANSFER_ACK = 0x1027;
    public static final int MRIM_CS_GET_MPOP_SESSION = 0x1024;
    public static final int MRIM_CS_HELLO = 0x1001;
    public static final int MRIM_CS_HELLO_ACK = 0x1002;
    public static final int MRIM_CS_LOGIN_ACK = 0x1004;
    public static final int MRIM_CS_LOGIN_REJ = 0x1005;
    public static final int MRIM_CS_LOGIN2 = 0x1038;
    public static final int MRIM_CS_LOGIN3 = 0x1078;
    public static final int MRIM_CS_LOGOUT = 0x1013;
    public static final int MRIM_CS_MAILBOX_STATUS = 0x1033;
    public static final int MRIM_CS_MESSAGE = 0x1008;
    public static final int MRIM_CS_MESSAGE_ACK = 0x1009;
    public static final int MRIM_CS_MESSAGE_RECV = 0x1011;
    public static final int MRIM_CS_MESSAGE_STATUS = 0x1012;
    public static final int MRIM_CS_MODIFY_CONTACT = 0x101B;
    public static final int MRIM_CS_MODIFY_CONTACT_ACK = 0x101C;
    public static final int MRIM_CS_MPOP_SESSION = 0x1025;
    public static final int MRIM_CS_NEW_EMAIL = 0x1048;
    public static final int MRIM_CS_OFFLINE_MESSAGE_ACK = 0x101D;
    public static final int MRIM_CS_PING = 0x1006;
    public static final int MRIM_CS_SMS = 0x1039;
    public static final int MRIM_CS_SMS_ACK = 0x1040;
    public static final int MRIM_CS_USER_INFO = 0x1015;
    public static final int MRIM_CS_USER_STATUS = 0x100F;
    public static final int MRIM_CS_WP_REQUEST = 0x1029;
    public static final int MRIM_CS_WP_REQUEST_PARAM_BIRTHDAY = 6;
    public static final int MRIM_CS_WP_REQUEST_PARAM_BIRTHDAY_DAY = 14;
    public static final int MRIM_CS_WP_REQUEST_PARAM_BIRTHDAY_MONTH = 13;
    public static final int MRIM_CS_WP_REQUEST_PARAM_CITY_ID = 11;
    public static final int MRIM_CS_WP_REQUEST_PARAM_COUNTRY_ID = 15;
    public static final int MRIM_CS_WP_REQUEST_PARAM_DATE1 = 7;
    public static final int MRIM_CS_WP_REQUEST_PARAM_DATE2 = 8;
    public static final int MRIM_CS_WP_REQUEST_PARAM_DOMAIN = 1;
    public static final int MRIM_CS_WP_REQUEST_PARAM_FIRSTNAME = 3;
    public static final int MRIM_CS_WP_REQUEST_PARAM_LASTNAME = 4;
    public static final int MRIM_CS_WP_REQUEST_PARAM_MAX = 16;
    public static final int MRIM_CS_WP_REQUEST_PARAM_NICKNAME = 2;
    public static final int MRIM_CS_WP_REQUEST_PARAM_ONLINE = 9;
    public static final int MRIM_CS_WP_REQUEST_PARAM_SEX = 5;
    public static final int MRIM_CS_WP_REQUEST_PARAM_STATUS = 10;
    public static final int MRIM_CS_WP_REQUEST_PARAM_USER = 0;
    public static final int MRIM_CS_WP_REQUEST_PARAM_ZODIAC = 12;
    public static final int MRIM_GET_SESSION_FAIL = 0;
    public static final int MRIM_GET_SESSION_SUCCESS = 1;
    public static final int PARAM_VALUE_LENGTH_LIMIT = 64;
    public static final int PARAMS_NUMBER_LIMIT = 50;
    public static final int SMS_ACK_DELIVERY_STATUS_INVALID_PARAMS = 0x10000;
    public static final int SMS_ACK_DELIVERY_STATUS_SUCCESS = 1;
    public static final int SMS_ACK_SERVICE_UNAVAILABLE = 2;
    public static final int STATUS_AWAY = 0x02;
    public static final int STATUS_FLAG_INVISIBLE = 0x80000000;
    public static final int STATUS_OFFLINE = 0x00;
    public static final int STATUS_ONLINE = 0x01;
    public static final int STATUS_OTHER = 0x04;
    public static final int STATUS_UNDETERMINATED = 0x03;

}
