package org.wiredwidgets.cow.ac.util.server;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

/**
 * A custom error handler for http responses to the COW client.  This allows
 * for logging to support debugging. In a more robust implementation, this 
 * class could be expanded to handle, and ideally recover, from specific errors.
 * @author RYANMILLER
 * @see applicationContext.xml
 * @see org.wiredwidgets.cow.ac.util.server.client.BpmClient
 */
public class BpmClientErrorHandler implements ResponseErrorHandler {

    private static final Logger logger = Logger.getLogger(BpmClientErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse chr) throws IOException {
        if (chr.getStatusCode().series().compareTo(HttpStatus.Series.SUCCESSFUL) != 0) {
            return true;

            // example call to perform handling for a class of responses
            //if (chr.getStatusCode().series().compareTo(Series.SUCCESSFUL) == 0)
            // { }

            // example handler routine for specific error code
            //if (chr.getStatusCode() == HttpStatus.FORBIDDEN) {
            //    logger.debug("Call returned a error 403 forbidden response.");
            //    return true;
            //}
        }
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse chr) throws IOException {
        //logger.debug("ClientHttpResponse error handler called");
        String errtxt = "Http request resulted in a failed respose: " 
                    + "[Status code: " + chr.getStatusCode()
                    + "] [Response: " + chr.getStatusText()
                    + "] [Body: " + chr.getBody() + "]";
        
        logger.debug(errtxt);
        throw new RestClientException(errtxt);
        // example handler 
        //if (chr.getStatusCode() == HttpStatus.FORBIDDEN) {
        //    logger.debug(HttpStatus.FORBIDDEN + " response. Throwing authentication exception");
        //    try {
        //        throw new AuthenticationException();
        //    } catch (AuthenticationException ex) {
        //        Exceptions.printStackTrace(ex);
        //    }
        //}
    }
}