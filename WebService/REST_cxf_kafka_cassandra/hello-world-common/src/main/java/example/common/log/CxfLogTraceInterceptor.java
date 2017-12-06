package example.common.log;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxfLogTraceInterceptor extends AbstractPhaseInterceptor<Message> {

    private static Logger logger = LoggerFactory.getLogger(CxfLogTraceInterceptor.class);

    public CxfLogTraceInterceptor() {
        super(Phase.PREPARE_SEND);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        logger.debug(message.toString());
    }

}
