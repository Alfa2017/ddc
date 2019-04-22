package ddc.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import ddc.AppState;
import ddc.service.FsStorageService;
import ddc.service.blockchain.BlockchainService;
import ddc.service.storage.MasterchainStorageService;
import ddc.service.storage.MockStorageService;
import ddc.service.storage.interfaces.RemoteStorageService;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Configuration
@EnableScheduling
@EnableCaching
public class AppConfig {

    @Bean
    public Web3jService getHttpService(@Value("${blockchain.nodeUrl}") String nodeUrl){
        if (nodeUrl.startsWith("ipc://")) {
            log.debug("Use IPC for blockchain: {}", nodeUrl.substring(6));
            return new UnixIpcService(nodeUrl.substring(6));
        }

        log.debug("Use RPC for blockchain");

        long timeOut = 3000L;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.SECONDS)
                .readTimeout(timeOut, TimeUnit.SECONDS)
                .writeTimeout(timeOut, TimeUnit.SECONDS);
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request request = chain.request();
//                        System.out.println(request.toString());
//                        Response response = chain.proceed(request);
//                        System.out.println(response.toString());
//                        return response;
//                    }
//                });
        return new HttpService(nodeUrl, builder.build(), false);
    }

    @Bean
    public RemoteStorageService getServiceStorage(AppState appState, BlockchainService blockchainService, FsStorageService fsStorageService){
        if (appState.getStorageEnabled()){
            return new MasterchainStorageService(appState, blockchainService);
        } else {
            return new MockStorageService(fsStorageService);
        }
    }
}
