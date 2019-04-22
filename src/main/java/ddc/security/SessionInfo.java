package ddc.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.springframework.web.util.TagUtils.SCOPE_REQUEST;

@Component
@Scope(value = SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class SessionInfo {

    private String sessionToken;

    private LocalDateTime createTime;

    private String userRole;

    private String accountAddress;

    private String depositoryOgrn;

    private String managerName;
}
