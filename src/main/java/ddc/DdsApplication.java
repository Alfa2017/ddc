package ddc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication(scanBasePackages = {"ru.iteco.aft.dds"})
public class DdsApplication implements CommandLineRunner {

    @Autowired
    private AppState appState;


    @Value("${app.version}")
    private String appVersion;


    public static void main(String[] args) {
        SpringApplication.run(DdsApplication.class, args);
    }

    public void showVersion() {
        System.out.println("\n\n\t+=====================================+");
        System.out.println("\t|   DECENTRALISED DEPOSITORY SYSTEM   |");
        System.out.printf("\t|      version: \u001B[32m%-22s\u001B[0m|%n", appVersion);
        System.out.println("\t+=====================================+\n\n");
    }

    @Override
    public void run(String... args) {
        showVersion();
        appState.notifyObservers();
    }
}


