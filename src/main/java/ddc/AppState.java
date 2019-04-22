package ddc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ddc.model.UserContext;
import ddc.security.SessionInfo;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class AppState extends Observable {

    @Getter
    private String nodeUrl;

    @Getter
    private String ddsAddress;

    @Getter
    private String robotAddress;

    @Getter
    @Setter
    private String robotName;

    /**
     * ОГРН депозитария с имени котогорго запущена ДДС
     */
    @Setter
    @Getter
    private String depositoryOgrn;

    @Getter
    private String storageUrl;

    @Getter
    private Boolean storageEnabled;

    @Getter
    @Setter
    private String roleModelAddress;

    @Getter
    @Setter
    private String registryAddress;

    @Getter
    @Setter
    private Boolean autoHandle;

    @Getter
    @Setter
    private String autoOpen;


    @Getter
    @Setter
    private String certAlias;

    @Getter
    @Setter
    private String certPwd;

    @Getter
    @JsonIgnore
    private final Map<String, SessionInfo> sessions = Collections.synchronizedMap(new HashMap<>());

    @Getter
    @JsonIgnore
    private final Map<String, UserContext> userContext = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    public AppState(
            @Value("${blockchain.nodeUrl}") String nodeUrl,
            @Value("${blockchain.ddsAddress}") String ddsAddress,
            @Value("${blockchain.robotAddress}") String robotAddress,
            @Value("${dds.depositoryOgrn}") String depositoryOgrn,
            @Value("${storage.url}") String storageUrl,
            @Value("${blockchain.storage.enabled}") Boolean storageEnabled,
            @Value("${app.autoHandle}") Boolean autoHandle,
            @Value("${app.auto.open.oper.day}") String autoOpen,
            @Value("${certificate.alias}") String certAlias,
            @Value("${certificate.password}") String certPwd
    ) {
        this.nodeUrl = nodeUrl;
        this.ddsAddress = ddsAddress;
        this.robotAddress = robotAddress;
        this.depositoryOgrn = depositoryOgrn;
        this.storageUrl = storageUrl;
        this.storageEnabled = storageEnabled;
        this.autoHandle = autoHandle;
        this.autoOpen = autoOpen;
        this.certAlias = certAlias;
        this.certPwd = certPwd;
        this.setChanged();
    }

    public synchronized void setDdsAddress(String ddsAddress) {
        this.ddsAddress = ddsAddress;
        this.setChanged();
    }

    public synchronized void setRobotAddress(String robotAddress) {
        this.robotAddress = robotAddress;
        this.setChanged();
    }

    public void cleanup()
    {
        try {
            Set<String> keySet = new HashSet<>();

            for (Map.Entry<String, UserContext> entry : userContext.entrySet()) {
                UserContext ctx = entry.getValue();
                LocalDateTime t = ctx.getDt().withMinute(30);  // todo: 30 минут
                if (t.compareTo(LocalDateTime.now()) < 0) {
                    //userContext.remove(entry.getKey()); // <- todo: удалить просроченную запись
                    keySet.add(entry.getKey());
                }
            }

            userContext.keySet().removeAll(keySet);
        }
        catch (Exception ex ) {}
    }
}
