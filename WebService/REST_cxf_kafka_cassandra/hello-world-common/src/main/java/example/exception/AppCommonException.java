package example.exception;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

public class AppCommonException extends RuntimeException {

    private static final long serialVersionUID = 6851143173501965100L;
    private int httpStatus = 400;

    /**
     * please define it in AppCommonErrorEnum
     */
    private AppCommonErrorEnum errorCode;

    /**
     * the information defined in json error file
     */
    private AppCommonError error;

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

    public AppCommonException(){
    }

    public AppCommonException(AppCommonErrorEnum errorCode) {
        this.errorCode = errorCode;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, Throwable e) {
        super(e);
        this.errorCode = errorCode;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, Map<String, Object> extendInfo) {
        this.errorCode = errorCode;
        this.extendInfo = extendInfo;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, int httpStatus) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, String[] errorDescValueSortArray) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }
    }

    public AppCommonException(AppCommonErrorEnum errorCode, String[] errorDescValueSortArray, int httpStatus) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }
        this.httpStatus = httpStatus;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, String[] errorDescValueSortArray,
            Map<String, Object> extendInfo) {
        this.errorCode = errorCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }
        this.extendInfo = extendInfo;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, Throwable cause, int statusCode) {
        super(cause);
        this.errorCode = errorCode;
        this.httpStatus = statusCode;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, Throwable cause, String message) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public AppCommonException(AppCommonError AppCommonError, Throwable cause, String message) {
        super(message, cause);
        this.error = AppCommonError;
    }

    public AppCommonException(AppCommonError AppCommonError, int statusCode, Throwable cause, String message) {
        super(message, cause);
        this.error = AppCommonError;
        this.httpStatus = statusCode;
    }

    public AppCommonException(AppCommonErrorEnum errorCode, String[] errorDescValueSortArray, Throwable cause,
            int statusCode) {
        super(null, cause);
        this.errorCode = errorCode;
        this.httpStatus = statusCode;
        if (errorDescValueSortArray != null) {
            this.errorDescValueSortArray = Arrays.copyOf(errorDescValueSortArray, errorDescValueSortArray.length);
        }
    }

    public AppCommonError getError() {
        if (error == null) {
            Map<String, Object> errorMap = ErrorConfigurationHelper.getError(this.errorCode.getErrorCode());

            if (MapUtils.isEmpty(errorMap)) {
                errorMap = ErrorConfigurationHelper.getError(AppCommonErrorEnum.INTERNAL_SERVER_ERROR.getErrorCode());
                httpStatus = Integer.parseInt(errorMap.get(AppCommonConstants.ERROR_FIELD_HTTP_STATUS).toString());
                errorMap.remove(AppCommonConstants.ERROR_FIELD_HTTP_STATUS);
                error = buildError(errorMap);
                return this.error;
            }

            if (errorMap.get(AppCommonConstants.ERROR_FIELD_HTTP_STATUS) != null) {
                httpStatus = Integer.parseInt(errorMap.get(AppCommonConstants.ERROR_FIELD_HTTP_STATUS).toString());
                errorMap.remove(AppCommonConstants.ERROR_FIELD_HTTP_STATUS);
            }

            if ((errorMap.get(AppCommonConstants.ERROR_FIELD_ERROR_DESC) != null) && (errorDescValueSortArray != null)) {
                errorMap.put(AppCommonConstants.ERROR_FIELD_ERROR_DESC, MessageFormat.format(
                        errorMap.get(AppCommonConstants.ERROR_FIELD_ERROR_DESC).toString(), errorDescValueSortArray));
            }
            
            if(errorMap.containsKey(AppCommonConstants.MESSAGE_TRANSACTION)){
                this.setSimpleMessage((String) errorMap.get(AppCommonConstants.MESSAGE_TRANSACTION));
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

    private AppCommonError buildError(Map<String, Object> errorMap) {
        if (MapUtils.isEmpty(errorMap)) {
            return null;
        }

        String errorStr = (String) errorMap.get(AppCommonConstants.ERROR_FIELD_ERROR);
        String errorDesc = (String) errorMap.get(AppCommonConstants.ERROR_FIELD_ERROR_DESC);
        errorMap.remove(AppCommonConstants.ERROR_FIELD_ERROR);
        errorMap.remove(AppCommonConstants.ERROR_FIELD_ERROR_DESC);
        errorMap.remove(AppCommonConstants.MESSAGE_TRANSACTION);
        return new AppCommonError(errorStr, errorDesc, errorMap);
    }

    @Override
    public String getMessage() {
        if (super.getMessage() != null) {
            return super.getMessage();
        }
        AppCommonError err = getError();
        if(this.getSimpleMessage()!=null){
            return this.getSimpleMessage();
        }
       
        return err.getErrorDescription();
    }

    public AppCommonErrorEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(AppCommonErrorEnum errorCode) {
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

    public void setError(AppCommonError error) {
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
