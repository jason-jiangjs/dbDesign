package org.vog.dbd.web.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * 向第三方发送HTTP请求时的异常处理Handler
 * 不具通用性，关联与具体业务(第三方平台)
 */
public class HttpResponseErrorHandler extends DefaultResponseErrorHandler {

    private String innerError = null;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        try {
            HttpStatus stsVal = getHttpStatusCode(response);
            if (stsVal == null) {
                innerError = "该请求返回的HttpStatusCode为空";
                return true;
            }
            boolean rst = super.hasError(stsVal);
            if (rst) {
                innerError = stsVal.value() + ":" + stsVal.getReasonPhrase();
            }
            return rst;
        } catch (Exception exp) {
            innerError = StringUtils.trimToNull(exp.getMessage());
            if (innerError == null) {
                innerError = exp.toString();
            }
            return true;
        }
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // 这里特意不作处理，由调用的业务来决定
        if (innerError != null) {
            response.getHeaders().add("_innerError", innerError);
        }
    }

}
