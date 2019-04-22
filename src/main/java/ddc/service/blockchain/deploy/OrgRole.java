package ddc.service.blockchain.deploy;

import ddc.service.blockchain.Web3jUtils;
import lombok.Getter;

public enum OrgRole {
    Aft("АФТ (оператор ДДС)"),
    Depository("Депозитарий");

    @Getter
    private final String desc;

    OrgRole(String desc) {
        this.desc = desc;
    }

    public byte[] getBytes(String prefix) {
        return Web3jUtils.strToBytes32(this.name());
    }

    public byte[] getBytes() {
        return getBytes("");
    }
}
