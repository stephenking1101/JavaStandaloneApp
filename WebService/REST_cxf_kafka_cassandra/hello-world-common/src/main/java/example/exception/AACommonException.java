package example.exception;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

public class AACommonException extends RuntimeException {

    private static final long serialVersionUID = 6851143173501965100L;
    private int httpStatus = 400;

    /**
     * please define it in AACommonErrorEnum
     */
    private AACommonErrorEnum errorCode;

    /**
     * the information defined in json error file
     */
    private AACommonError error;

    /**
     * add the extends information
     */
    private Map<String, Object> extendInfo;

    /**
     * the value for error description variables please obey the order
     */
    private String[] errorDescValueSortArray;
    
    /**
     * shorten message for transaction log
     */
    private String simpleMessage;

    public AACommonException(){
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
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }
    }

    public AACommonException(AACommonErrorEnum errorCode, String[] errorDescValueSortArray, int httpStatus) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }
        this.httpStatus = httpStatus;
    }

    public AACommonException(AACommonErrorEnum errorCode, String[] errorDescValueSortArray,
            Map<String, Object> extendInfo) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
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

    public AACommonException(AACommonErrorEnum errorCode, String[] errorDescValueSortArray, Throwable cause,
            int statusCode) {
        super(null, cause);
        this.errorCode = errorCode;
        this.httpStatus = statusCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }
    }

    public AACommonError getError() {
        if (error == null) {
            Map<String, Object> errorMap = ErrorConfigurationHelper.getError(this.errorCode.getErrorCode());

            if (MapUtils.isEmpty(errorMap)) {
                errorMap = ErrorConfigurationHelper.getError(AACommonErrorEnum.INTERNAL_SERVER_ERROR.getErrorCode());
                httpStatus = Integer.parseInt(errorMap.get(AACommonConstants.ERROR_FIELD_HTTP_STATUS).toString());
                errorMap.remove(AACommonConstants.ERROR_FIELD_HTTP_STATUS);
                error = buildError(errorMap);
                return this.error;
            }

            if (errorMap.get(AACommonConstants.ERROR_FIELD_HTTP_STATUS) != null) {
                httpStatus = Integer.parseInt(errorMap.get(AACommonConstants.ERROR_FIELD_HTTP_STATUS).toString());
                errorMap.remove(AACommonConstants.ERROR_FIELD_HTTP_STATUS);
            }

            if ((errorMap.get(AACommonConstants.ERROR_FIELD_ERROR_DESC) != null) && (errorDescValueSortArray != null)) {
                errorMap.put(AACommonConstants.ERROR_FIELD_ERROR_DESC, MessageFormat.format(
                        errorMap.get(AACommonConstants.ERROR_FIELD_ERROR_DESC).toString(), errorDescValueSortArray));
            }
            
            if(errorMap.containsKey(AACommonConstants.MESSAGE_TRANSACTION)){
                this.setSimpleMessage((String) errorMap.get(AACommonConstants.MESSAGE_TRANSACTION));
            }
            // add extend info
            if (extendInfo != null) {
                for (String key : extendInfo.keySet()) {
                    if (!errorMap.containsKey(key)) {
                        errorMap.put(key, extendInfo.get(key));
                    }
                }
            }

            error = buildError(errorMap);
        }
        return this.error;
    }

    private AACommonError buildError(Map<String, Object> errorMap) {
        if (MapUtils.isEmpty(errorMap)) {
            return null;
        }

        String errorStr = (String) errorMap.get(AACommonConstants.ERROR_FIELD_ERROR);
        String errorDesc = (String) errorMap.get(AACommonConstants.ERROR_FIELD_ERROR_DESC);
        errorMap.remove(AACommonConstants.ERROR_FIELD_ERROR);
        errorMap.remove(AACommonConstants.ERROR_FIELD_ERROR_DESC);
        errorMap.remove(AACommonConstants.MESSAGE_TRANSACTION);
        return new AACommonError(errorStr, errorDesc, errorMap);
    }

    @Override
    public String getMessage() {
        if (super.getMessage() != null) {
            return super.getMessage();
        }
        AACommonError err = getError();
        if(this.getSimpleMessage()!=null){
            return this.getSimpleMessage();
        }
       
        return err.getErrorDescription();
    }

    public AACommonErrorEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(AACommonErrorEnum errorCode) {
        this.errorCode = errorCode;
    }

    public int getHttpStatus() {
        getError();
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String[] getErrorDescValueSortArray() {
        return errorDescValueSortArray;
    }

    public void setError(AACommonError error) {
        this.error = error;
    }

    public Map<String, Object> getExtendInfo() {
        return extendInfo;
    }

    public void setExtInfo(Map<String, Object> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public String getSimpleMessage() {
        return simpleMessage;
    }

    public void setSimpleMessage(String simpleMessage) {
        this.simpleMessage = simpleMessage;
    }
}
