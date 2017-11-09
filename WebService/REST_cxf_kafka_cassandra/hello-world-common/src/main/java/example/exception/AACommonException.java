package example.exception;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

public class AACommonException extends RuntimeException {
    private static final long serialVersionUID = 6851143173501965100L;
    private int httpStatus = 400;
    private AACommonErrorEnum errorCode;
    private AACommonError error;
    private Map<String, Object> extendInfo;
    private String[] errorDescValueSortArray;
    private String simpleMessage;

    public AACommonException() {
    }

    public AACommonException(AACommonErrorEnum errorCode) {
        this.errorCode = errorCode;
    }

    public AACommonException(AACommonErrorEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AACommonException(AACommonErrorEnum errorCode, Throwable e) {
        super(e);
        this.errorCode = errorCode;
    }

    public AACommonException(AACommonErrorEnum errorCode, Map<String, Object> extendInfo) {
        this.errorCode = errorCode;
        this.extendInfo = extendInfo;
    }

    public AACommonException(AACommonErrorEnum errorCode, int httpStatus) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public AACommonException(AACommonErrorEnum errorCode, String[] errorDescValueSortArray) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = (String[])Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }

    }

    public AACommonException(AACommonErrorEnum errorCode, String[] errorDescValueSortArray, int httpStatus) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = (String[])Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }

        this.httpStatus = httpStatus;
    }

    public AACommonException(AACommonErrorEnum errorCode, String[] errorDescValueSortArray, Map<String, Object> extendInfo) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = (String[])Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }

        this.extendInfo = extendInfo;
    }

    public AACommonException(AACommonErrorEnum errorCode, Throwable cause, int statusCode) {
        super(cause);
        this.errorCode = errorCode;
        this.httpStatus = statusCode;
    }

    public AACommonException(AACommonErrorEnum errorCode, Throwable cause, String message) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public AACommonException(AACommonError aaCommonError, Throwable cause, String message) {
        super(message, cause);
        this.error = aaCommonError;
    }

    public AACommonException(AACommonError aaCommonError, int statusCode, Throwable cause, String message) {
        super(message, cause);
        this.error = aaCommonError;
        this.httpStatus = statusCode;
    }

    public AACommonException(AACommonErrorEnum errorCode, String[] errorDescValueSortArray, Throwable cause, int statusCode) {
        super((String)null, cause);
        this.errorCode = errorCode;
        this.httpStatus = statusCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = (String[])Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }

    }

    public AACommonError getError() {
        if (this.error == null) {
            Map<String, Object> errorMap = ErrorConfigurationHelper.getError(this.errorCode.getErrorCode());
            if (MapUtils.isEmpty(errorMap)) {
                errorMap = ErrorConfigurationHelper.getError(AACommonErrorEnum.INTERNAL_SERVER_ERROR.getErrorCode());
                this.httpStatus = Integer.parseInt(errorMap.get("http_status").toString());
                errorMap.remove("http_status");
                this.error = this.buildError(errorMap);
                return this.error;
            }

            if (errorMap.get("http_status") != null) {
                this.httpStatus = Integer.parseInt(errorMap.get("http_status").toString());
                errorMap.remove("http_status");
            }

            if (errorMap.get("error_description") != null && this.errorDescValueSortArray != null) {
                errorMap.put("error_description", MessageFormat.format(errorMap.get("error_description").toString(), this.errorDescValueSortArray));
            }

            if (errorMap.containsKey("message")) {
                this.setSimpleMessage((String)errorMap.get("message"));
            }

            if (this.extendInfo != null) {
                Iterator i$ = this.extendInfo.keySet().iterator();

                while(i$.hasNext()) {
                    String key = (String)i$.next();
                    if (!errorMap.containsKey(key)) {
                        errorMap.put(key, this.extendInfo.get(key));
                    }
                }
            }

            this.error = this.buildError(errorMap);
        }

        return this.error;
    }

    private AACommonError buildError(Map<String, Object> errorMap) {
        if (MapUtils.isEmpty(errorMap)) {
            return null;
        } else {
            String errorStr = (String)errorMap.get("error");
            String errorDesc = (String)errorMap.get("error_description");
            errorMap.remove("error");
            errorMap.remove("error_description");
            errorMap.remove("message");
            return new AACommonError(errorStr, errorDesc, errorMap);
        }
    }

    public String getMessage() {
        if (super.getMessage() != null) {
            return super.getMessage();
        } else {
            AACommonError err = this.getError();
            return this.getSimpleMessage() != null ? this.getSimpleMessage() : err.getErrorDescription();
        }
    }

    public AACommonErrorEnum getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(AACommonErrorEnum errorCode) {
        this.errorCode = errorCode;
    }

    public int getHttpStatus() {
        this.getError();
        return this.httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String[] getErrorDescValueSortArray() {
        return this.errorDescValueSortArray;
    }

    public void setError(AACommonError error) {
        this.error = error;
    }

    public Map<String, Object> getExtendInfo() {
        return this.extendInfo;
    }

    public void setExtInfo(Map<String, Object> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getSimpleMessage() {
        return this.simpleMessage;
    }

    public void setSimpleMessage(String simpleMessage) {
        this.simpleMessage = simpleMessage;
    }
}
