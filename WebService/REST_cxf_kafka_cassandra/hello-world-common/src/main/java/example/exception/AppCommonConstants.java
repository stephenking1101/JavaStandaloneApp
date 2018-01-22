package example.exception;

public class AppCommonConstants {

    public static int SECONDS_PER_HOUR = 3600;
    public static long MILLISECONDS_PER_SECOND = 1000L;
    public static long MILLISECONDS_PER_HOUR = SECONDS_PER_HOUR * 1000L;

    public static String SPACE_SEPARATOR = " ";
    public static String COMMA_SEPARATOR = ",";
    public static String COLON_SEPARATOR = ":";
    public static String DOT_SEPARATOR = ".";
    public static String EQUAL_SEPARATOR = "=";

    public static final String IAM = "iam";
    public static final String USER_ID = "user_id";
    public static final String USERID = "userId";
    public static final String CLIENTID = "clientId";

    // logger
    public static final String COMMON_LOGGER_NAME = "facility.common";

    // error
    public static final String ERROR_FIELD_HTTP_STATUS = "http_status";
    public static final String ERROR_FIELD_ERROR = "error";
    public static final String ERROR_FIELD_ERROR_DESC = "error_description";
    public static final String HARD_LOCK_REASON = "hard_lock_reason";
    public static final String MESSAGE_TRANSACTION = "message";

    //ip related
    public static final String ALARM_REQUIRED = "alarm_required";
    public static final String ERROR_RESERVED = "error_reserved";
    public static final String ALARM_DESCRIPTION = "alarm_description";
    public static final String ERROR_TYPE = "error_type";
    // configuration: client connection pooling
    public static final String CLIENT_POOL_MAX_TOTAL_CONNECTION = "iam.client.pool.max.connection";
    public static final String CLIENT_POOL_MAX_PER_ROUTE = "iam.client.pool.max.perroute";
    public static final String CLIENT_POOL_SOCKET_TIME_OUT = "iam.client.pool.socket.timeout";
    public static final String CLIENT_POOL_CONNECT_TIME_OUT = "iam.client.pool.connect.timeout";
	// default value for client connection pooling
    public static final int MAX_TOTAL_CONNECTION = 100;
    public static final int MAX_CONNECTION_PREHOST = 100;
    public static final int CONNECT_TIMEOUT = 30000;
    public static final int SOCKET_TIMEOUT = 30000;

    public static final String IAM_TEMPPASSWORD_TTL_SECOND = "iam.temppassword.ttl.second";
    public static final int IAM_TEMPPASSWORD_DEFAULT_TTL_SECOND = 86400;
    public static final String IAM_VALIDATIONCODE_TTL_SECOND = "iam.validationcode.ttl.second";
    public static final int IAM_VALIDATIONCODE_DEFAULT_TTL_SECOND = 300;

    public static final String AUTHENTICATION_SESSION_ENCODE_MODE = "authentication.session.encode.mode";

    // Credential extensions key
    public static final String CREDENTIAL_EXTENSIONS_KEY_SALT = "salt";
    public static final String CREDENTIAL_EXTENSIONS_KEY_ALGORITHM = "algorithm";
    public static final String CREDENTIAL_EXTENSIONS_KEY_HASH_PARAMETERS = "hashParameters";
    public static final String CREDENTIAL_EXTENSIONS_KEY_MUST_CHANGE = "mustChange";
    public static final String CREDENTIAL_EXTENSIONS_KEY_MUST_CHANGE_REASON = "mustChangeReason";
    public static final String CREDENTIAL_EXTENSIONS_KEY_MUST_CHANGE_TIME = "mustChangeTime";

    // biometric related
    public static final String BIO_FIDO_SERVER_URL = "bio.call.fido.url";
    public static final String BIOMETRIC_API_DISABLED = "biometric.api.disabled";
	public static final String BIOMETRIC_ENABLED = "biometricEnabled";

    // user_id schema
    public static final String USERID_PREFIX_MAILTO = "mailto:";
    public static final String USERID_PREFIX_TEL = "tel:";
    public static final String USERID_PREFIX_UNAME = "uname:";
    public static final String USERID_PREFIX_UID = "uid:";

    // AA Adaptation: USN
    public static final String USN_ENABLED = "iam.usn.enabled";
    public static final String KEY_TOKEN_USN = "tokenUSN";
    public static final String KEY_SESSION_USN = "sessionUSN";
    public static final String KEY_HEADER_USN = "usn";
    public static final String KEY_USN = "Unique-Session-Number";
    public static final String RESPONSE_TOKEN_USN = "TokenUSN";
    public static final String RESPONSE_SESSION_USN = "SessionUSN";
    public static final String RESPONSE_HEADER_USN = "HeaderUSN";
    public static final String TX_IAM_SESSION_USN = "tx.iam.sessionUSN";
    public static final String TX_IAM_TOKEN_USN = "tx.iam.tokenUSN";
    public static final String TX_IAM_HEADER_USN = "tx.iam.header.usn";
}