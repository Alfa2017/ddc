package ddc.service.blockchain.deploy;


import java.util.HashMap;
import java.util.Map;

public class Data {

    public static Map<Org, OrgInfo> getOrganizations(Map<Org, OrgAccounts> ethAccounts) {
        Map<Org, OrgInfo> map = new HashMap<>();
        map.put(Org.AFT, new OrgInfo().setShortName("АФТ")
                .setFullName("Ассоциация финансовых технологий")
                .setOgrn("1177700002150").setInn("9705086966").setKpp("770501001").setCode("AFT").setEthAccounts(ethAccounts.get(Org.AFT))
        );

        map.put(Org.SBERSD, new OrgInfo().setShortName("ООО «Спецдепозитарий Сбербанка»")
                .setFullName("Общество с ограниченной ответственностью «Специализированный депозитарий Сбербанка»")
                .setOgrn("1107746400827").setInn("7736618039").setKpp("772501001").setCode("SBERSD").setEthAccounts(ethAccounts.get(Org.SBERSD))
        );

        map.put(Org.RAIF, new OrgInfo().setShortName("АО «Райффайзенбанк»")
                .setFullName("Акционерное общество «Райффайзенбанк»")
                .setOgrn("1027739326449").setInn("7744000302").setKpp("770201001").setCode("RAIF").setEthAccounts(ethAccounts.get(Org.RAIF))
        );

        map.put(Org.REGION, new OrgInfo().setShortName("АО «ДК РЕГИОН»")
                .setFullName("Акционерное общество «Депозитарная компания «РЕГИОН»")
                .setOgrn("1037708002144").setInn("7708213619").setKpp("997950001").setCode("REGION").setEthAccounts(ethAccounts.get(Org.REGION))
        );

        map.put(Org.VTB, new OrgInfo().setShortName("Банк ВТБ (ПАО)")
                .setFullName("Банк ВТБ (публичное акционерное общество)")
                .setOgrn("1027739609391").setInn("7702070139").setKpp("997950001").setCode("VTB")
        );

        map.put(Org.AKBARS, new OrgInfo().setShortName("ПАО «АК БАРС» БАНК»")
                .setFullName(" Акционерный коммерческий банк «АК БАРС» (публичное акционерное общество)")
                .setOgrn("1021600000124").setInn("1653001805").setKpp("165601001").setCode("AKBARS")
        );

//        map.put(Org.VTBSD, new OrgInfo().setShortName("ЗАО ВТБ Специализированный депозитарий")
//                .setFullName("Закрытое акционерное общество ВТБ Специализированный депозитарий")
//                .setOgrn("1027739157522").setInn("7705110090").setKpp("770801001").setCode("VTBSD")
//        );
//
//                new OrgInfo().setShortName("Ассоциация ФинТех")
//                        .setFullName("Ассоциация развития финансовых технологий")
//                        .setOgrn("1177700002151").setInn("9705086966").setKpp("770501001").setCode("AFT"),
//
//                new OrgInfo().setShortName("ПАО Сбербанк")
//                        .setFullName("Общество с ограниченной ответственностью «Публичное акционерное общество «Сбербанк России»»")
//                        .setOgrn("1027700132195").setInn("7707083893").setKpp("773601001").setCode("SBER"),
//
//                new OrgInfo().setShortName("АО ИК «АК БАРС Финанс»")
//                        .setFullName("Акционерное общество Инвестиционная компания «АК БАРС Финанс»")
//                        .setOgrn("1047796383030").setInn("7706537061").setKpp("770901001").setCode("AKBF")

        return map;
    }
}
