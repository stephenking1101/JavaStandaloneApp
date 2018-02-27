package example.client.interceptor;

import java.io.InputStream;

import org.apache.cxf.interceptor.MessageSenderInterceptor.MessageSenderEndingInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 * The purpose of this class is for fixing cxf connection leak issue,
 * https://issues.apache.org/jira/browse/CXF-5144
 */
@Aspect
public class InputStreamCloseInterceptor extends AbstractPhaseInterceptor<Message> {

    private static ThreadLocal<InputStream> threadLocal = new ThreadLocal<InputStream>();

    public InputStreamCloseInterceptor() {
        super(Phase.PREPARE_SEND_ENDING);
        super.addAfter(MessageSenderEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) {
        InputStream inputSteam = message.getExchange().getInMessage().getContent(InputStream.class);
        threadLocal.set(inputSteam);
    }

    public static InputStream getInputStream() {
        return threadLocal.get();
    }

	@After("execution(* example.service.api.*.*(..))")
    public void closeInputStream() {
        InputStream inputStream = getInputStream();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                // do nothing
            }
        }
    }
}
