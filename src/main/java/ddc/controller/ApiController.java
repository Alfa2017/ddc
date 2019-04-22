package ddc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ddc.exception.NotFoundDdsException;
import ddc.model.enums.Dictionaries;
import ddc.model.response.SessionInfoResponse;
import ddc.security.Authorized;
import ddc.security.SessionInfo;
import ddc.security.Unauthorized;
import ddc.service.domain.manager.ManagerDbService;
import ddc.util.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Authorized
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ManagerDbService managerDbService;

    @Autowired
    private SessionInfo sessionInfo;

    @Value("${app.version}")
    private String appVersion;

    /**
     * Информация о текущей сессии
     * @return
     */
    @Unauthorized
    @GetMapping("/session-info")
    public SessionInfoResponse getHeaderInfo() throws NotFoundDdsException {
        if (sessionInfo.getAccountAddress() == null) {
            return new SessionInfoResponse().setApiVersion(appVersion);
        }

        return managerDbService.getLoginInfo(sessionInfo.getAccountAddress());
    }

    @GetMapping(value = "/dictionary2", produces = {"application/json"})
    public String getDictionaryEnum() {
        List<Dictionaries.DocType> docTypes = Arrays.asList(Dictionaries.DocType.values());

        List<Dictionaries.DocType> docTypesForManuallyReg = Arrays.asList(
                Dictionaries.DocType.OTHER,
                Dictionaries.DocType.OW_INSTRUCTION_OPEN_ACCOUNT,
                Dictionaries.DocType.OW_INSTRUCTION_CLOSED_ACCOUNT,
                Dictionaries.DocType.OW_INSTRUCTION_INF_OPERATIONS,
                Dictionaries.DocType.OW_STANDING_ORDER,
                Dictionaries.DocType.OW_INSTRUCTION_CHANGE_PERSONAL_DATA,
                Dictionaries.DocType.OW_INSTRUCTION_OPEN_SECTION,
                Dictionaries.DocType.OW_INSTRUCTION_CLOSED_SECTION,
                Dictionaries.DocType.OW_INSTRUCTION_PLEDGE,
                Dictionaries.DocType.D_REQUEST_INF_MG,
                Dictionaries.DocType.GOV_DOC_ARREST,
                Dictionaries.DocType.OW_REQUEST_CONTENT_MG,
                Dictionaries.DocType.OW_REQUEST_OWNER_MORTGAGE,
                Dictionaries.DocType.OW_REQUEST_PERSONAL_DATA,
                Dictionaries.DocType.REPORT_TO_OW_ACC_OPENED,
                Dictionaries.DocType.REPORT_TO_OW_ACC_CLOSED,
                Dictionaries.DocType.REPORT_TO_OW_BLOCKED_OPERATIONS,
                Dictionaries.DocType.REPORT_TO_OW_UNLOCK_OPERATIONS,
                Dictionaries.DocType.REPORT_TO_OW_PERSONAL_DATA_CHANGED,
                Dictionaries.DocType.REPORT_TO_OW_SECTION_OPENED,
                Dictionaries.DocType.REPORT_TO_OW_SECTION_CLOSED,
                Dictionaries.DocType.REPORT_TO_OW_ACC_OPEN_FALSE,
                Dictionaries.DocType.REPORT_TO_OW_ACC_CLOSE_FALSE,
                Dictionaries.DocType.REPORT_TO_OW_INF_OPERATION_FALSE,
                Dictionaries.DocType.REPORT_TO_OW_CHANGE_PERSONAL_DATA_FALSE,
                Dictionaries.DocType.REPORT_TO_OW_OPEN_SECTION_FALSE,
                Dictionaries.DocType.REPORT_TO_OW_CLOSE_SECTION_FALSE,
                Dictionaries.DocType.REPORT_TO_OW_PLEDGE_FALSE,
                Dictionaries.DocType.RESPONSE_TO_OW_MG_CONTENT,
                Dictionaries.DocType.RESPONSE_TO_OW_OWNER_PERSONAL_DATA,
                Dictionaries.DocType.RESPONSE_TO_OW_BORROWER_PLEDGOR_PERSONAL_DATA,
                Dictionaries.DocType.RESPONSE_TO_GOV_REQUEST,
                Dictionaries.DocType.REPORT_TO_GOV_ARREST_TRUE,
                Dictionaries.DocType.REPORT_TO_GOV_ARREST_FALSE,
                Dictionaries.DocType.DEPONENT_CONTRACT
        );

        List<Dictionaries.OpAdm> opAdm = Arrays.asList(
                Dictionaries.OpAdm.ACC_OPEN,
                Dictionaries.OpAdm.ACC_CLOSE,
                Dictionaries.OpAdm.SECTION_OPEN,
                Dictionaries.OpAdm.SECTION_CLOSE,
                Dictionaries.OpAdm.DEPONENT_REG,
                Dictionaries.OpAdm.DEPONENT_CHANGE,
                Dictionaries.OpAdm.MORTGAGE_FORM_REG,
                Dictionaries.OpAdm.MORTGAGE_UPDATE_DOC,
                Dictionaries.OpAdm.MORTGAGE_FORM_CHANGE,
                Dictionaries.OpAdm.MORTGAGE_FORM_CLOSE,
                Dictionaries.OpAdm.ACC_BLOCK,
                Dictionaries.OpAdm.ACC_UNBLOCK,
                Dictionaries.OpAdm.MORTGAGE_RECEIPT,
                Dictionaries.OpAdm.MORTGAGE_WITHDRAWAL
        );
        List<Dictionaries.OpInf> opInf = Arrays.asList(
                Dictionaries.OpInf.INF_OPERATIONS,
                Dictionaries.OpInf.INF_BALANCE_ON_PERIOD,
                Dictionaries.OpInf.INF_MORTGAGE_PATCH
        );
        List<Dictionaries.OpInv> opInv = Arrays.asList(
                Dictionaries.OpInv.MORTGAGE_ENROLL,
                Dictionaries.OpInv.MORTGAGE_DEDUCT,
                Dictionaries.OpInv.ENCUMBRANCE_ADD,
                Dictionaries.OpInv.ENCUMBRANCE_CANCEL,
                Dictionaries.OpInv.MORTGAGE_BLOCKING_ENROLL,
                Dictionaries.OpInv.SECTIONS_TRANSFER_DEDUCT,
                Dictionaries.OpInv.SECTIONS_TRANSFER_ENROLL
        );


        List<Dictionaries.DocStatus> docStatuses = Arrays.asList(
                Dictionaries.DocStatus.ACCEPTED,
                Dictionaries.DocStatus.DECLINE,
                Dictionaries.DocStatus.EXPIRED,
                Dictionaries.DocStatus.DELAYED,
                Dictionaries.DocStatus.NEW,
                Dictionaries.DocStatus.VALIDATING,
                Dictionaries.DocStatus.PROCESSING,
                Dictionaries.DocStatus.SUCCESS,
                Dictionaries.DocStatus.FAILED
        );

        Map<String, String> docTypesMap = new LinkedHashMap<>();
        for (Dictionaries.DocType docType : docTypes) {
            docTypesMap.put(docType.name(), docType.getText());
        }

        Map<String, String> opAdmMap = new LinkedHashMap<>();
        for (Dictionaries.OpAdm op : opAdm) {
            opAdmMap.put(op.name(), op.getText());
        }

        Map<String, String> opInfMap = new LinkedHashMap<>();
        for (Dictionaries.OpInf op : opInf) {
            opInfMap.put(op.name(), op.getText());
        }

        Map<String, String> opInvMap = new LinkedHashMap<>();
        for (Dictionaries.OpInv op : opInv) {
            opInvMap.put(op.name(), op.getText());
        }

        Map<String, String> docStatus = new LinkedHashMap<>();
        for (Dictionaries.DocStatus status : docStatuses) {
            docStatus.put(status.name(), status.getText());
        }

        Map<String, Map> retVal = new LinkedHashMap<>();

        retVal.put("doctype", docTypesMap);
        retVal.put("docTypeForManuallyReg", docTypesForManuallyReg.stream().collect(Collectors.toMap(Dictionaries.DocType::name, Dictionaries.DocType::getText, (k, v) -> k, LinkedHashMap::new)));
        retVal.put("opAdm", opAdmMap);
        retVal.put("opInf", opInfMap);
        retVal.put("opInv", opInvMap);
        retVal.put("docStatus", docStatus);

        return Utils.objectToJson(retVal);
    }

//    @GetMapping("/dictionary/")
//    public List<DictionaryEntity> getDictionary() {
//        return dictionaryService.getDictionary();
//    }

    @GetMapping(value={"/dictionary/RzdTypes"}, produces = {"application/json"})
    public String getRzdTypes() {

        Map<String, Object> rzdTypesMap = Arrays.stream(Dictionaries.RzdTypes.values())
                .collect(Collectors.toMap(Dictionaries.RzdTypes::name, rt -> new HashMap<String, Object>() {{
                    put("text", rt.getText());
                    put("code", rt.getCode());
                }}));

        return Utils.objectToJson(rzdTypesMap);
    }

    @GetMapping(value={"/dictionary/{name}"}, produces = {"application/json"})
    public String getDictionaryByName(@PathVariable("name") String dictionary) {
        Map<String, String> map = Dictionaries.getEnumFromName(dictionary);
        return Utils.objectToJson(map);
    }

}