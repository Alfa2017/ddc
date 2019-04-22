/*
package ru.iteco.aft.dds.security;

import Web3jUtils;

import java.math.BigInteger;
import java.util.Arrays;

public enum UserRole {

    MANAGER("Оператор"),
    ROBOT("Робот"),
    ADMIN("Администратор"),
    ADMIN_AFT("Администратор АФТ");

    public final String text;

    UserRole(String text) {
        this.text = text;
    }

    public static UserRole getByName(String roleName) {
        return Arrays.stream(values()).filter(role -> role.name().equals(roleName)).findFirst().get();
    }

    public BigInteger toBI() {
        return BigInteger.valueOf(ordinal());
    }

    public byte[] bytes(){
        return Web3jUtils.strToBytes32(this.name());
    }

}
*/
