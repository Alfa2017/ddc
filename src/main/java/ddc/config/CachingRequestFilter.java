package ddc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

////@Slf4j
@Component
@Configuration
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class CachingRequestFilter implements Filter {

    //@Override
    //public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            if( request.getContentType().indexOf("multipart/") >= 0 ) chain.doFilter(new StandardMultipartHttpServletRequest((HttpServletRequest)request), response);
            else chain.doFilter(new ReadHttpServletRequest((HttpServletRequest) request), response);

        }
        catch (NullPointerException e) {
            chain.doFilter(request, response);
        }
    }

    //@Override
    public void destroy() { }
}
