package ddc.sc2;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.1.1.
 */
public class RoleModel extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b604051602080610bb383398101604052808051915081905080600160a060020a038116156100af5780600160a060020a031663589bfb59306040517c010000000000000000000000000000000000000000000000000000000063ffffffff8416028152600160a060020a039091166004820152602401600060405180830381600087803b151561009e57600080fd5b5af115156100ab57600080fd5b5050505b6000805461010060a860020a031916610100600160a060020a039384160217905533811632909116146100e157600080fd5b505060028054600160a060020a03191632600160a060020a0316179055610aa68061010d6000396000f3006060604052600436106101035763ffffffff60e060020a6000350416630afe78a581146101085780631876a34c146101715780631ef925781461018f5780632f2770db146101b657806334dcaa69146101c95780634c437d79146101e5578063589bfb59146101fe5780636ff968c31461021d57806375d0c0dc1461024c5780637fbccfc6146102d657806386770624146102e9578063a0a8e460146102fc578063a3907d711461030f578063ac16615114610322578063c52845e214610341578063d7a1a3aa1461035d578063dd03e81414610379578063ddb5db57146103a1578063e2f273bd146103bd578063ee070805146103dc578063f851a440146103ef575b600080fd5b341561011357600080fd5b61011e600435610402565b60405160208082528190810183818151815260200191508051906020019060200280838360005b8381101561015d578082015183820152602001610145565b505050509050019250505060405180910390f35b341561017c57600080fd5b61018d600435602435604435610473565b005b341561019a57600080fd5b6101a2610485565b604051901515815260200160405180910390f35b34156101c157600080fd5b61018d61049b565b34156101d457600080fd5b61018d6004356024356044356104cd565b34156101f057600080fd5b61011e6004356024356104da565b341561020957600080fd5b61018d600160a060020a0360043516610557565b341561022857600080fd5b610230610581565b604051600160a060020a03909116815260200160405180910390f35b341561025757600080fd5b61025f610590565b60405160208082528190810183818151815260200191508051906020019080838360005b8381101561029b578082015183820152602001610283565b50505050905090810190601f1680156102c85780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156102e157600080fd5b6102306105d1565b34156102f457600080fd5b61011e6105e5565b341561030757600080fd5b61025f610644565b341561031a57600080fd5b61018d610670565b341561032d57600080fd5b6101a2600160a060020a0360043516610690565b341561034c57600080fd5b6101a260043560243560443561069e565b341561036857600080fd5b6101a26004356024356044356106bb565b341561038457600080fd5b61038f6004356106ca565b60405190815260200160405180910390f35b34156103ac57600080fd5b61018d6004356024356044356106e9565b34156103c857600080fd5b61018d600160a060020a03600435166106f6565b34156103e757600080fd5b6101a261074f565b34156103fa57600080fd5b610230610758565b61040a6109dd565b60008281526003602090815260409182902080549092909182810201905190810160405280929190818152602001828054801561046757602002820191906000526020600020905b81548152600190910190602001808311610452575b50505050509050919050565b6104808383836004610767565b505050565b60025432600160a060020a039081169116145b90565b60005460ff16156104ab57600080fd5b6104b3610485565b15156104be57600080fd5b6000805460ff19166001179055565b6104808383836001610767565b6104e26109dd565b600083815260036020908152604080832085845260010182529182902080549092909182810201905190810160405280929190818152602001828054801561054a57602002820191906000526020600020905b81548152600190910190602001808311610535575b5050505050905092915050565b61055f61049b565b60018054600160a060020a031916600160a060020a0392909216919091179055565b600154600160a060020a031681565b6105986109dd565b60408051908101604052601181527f4146544d73746f72526f6c654d6f64656c0000000000000000000000000000006020820152905090565b6000546101009004600160a060020a031681565b6105ed6109dd565b600480548060200260200160405190810160405280929190818152602001828054801561063a57602002820191906000526020600020905b81548152600190910190602001808311610625575b5050505050905090565b61064c6109dd565b604080519081016040526005815260d860020a64302e322e37026020820152905090565b600154600160a060020a03161561068657600080fd5b61068e61097b565b565b600160a060020a0316151590565b600060046106ad8585856109ac565b1660ff161515949350505050565b600060066106ad8585856109ac565b60048054829081106106d857fe5b600091825260209091200154905081565b6104808383836002610767565b60005460ff161561070657600080fd5b61070e610485565b151561071957600080fd5b61072281610690565b151561072d57600080fd5b60028054600160a060020a031916600160a060020a0392909216919091179055565b60005460ff1681565b600254600160a060020a031681565b60005460ff161561077757600080fd5b61077f610485565b151561078a57600080fd5b60078116151561079957600080fd5b60008481526003602052604090205415156108225760048054600181016107c083826109ef565b506000918252602091829020018590556040519081016040528060006040518059106107e95750595b90808252806020026020018201604052509052600085815260036020526040902081518190805161081e929160200190610a13565b5050505b600084815260036020908152604080832086845260010190915290205415156108cd57600084815260036020526040902080546001810161086383826109ef565b5060009182526020918290200184905560405190810160405280600060405180591061088c5750595b81815260209182028101820160409081529252600087815260038252828120878252600101909152208151819080516108c9929160200190610a13565b5050505b6000848152600360209081526040808320868452600190810183528184208685520190915290205460ff16151561093d576000848152600360209081526040808320868452600190810190925290912080549091810161092d83826109ef565b5060009182526020909120018290555b60009384526003602090815260408086209486526001948501825280862093865292909301909252909120805460ff191660ff909216919091179055565b60005460ff16151560011480156109955750610995610485565b15156109a057600080fd5b6000805460ff19169055565b6000928352600360209081526040808520938552600193840182528085209285529190920190915290205460ff1690565b60206040519081016040526000815290565b81548183558181151161048057600083815260209020610480918101908301610a60565b828054828255906000526020600020908101928215610a50579160200282015b82811115610a505782518255602090920191600190910190610a33565b50610a5c929150610a60565b5090565b61049891905b80821115610a5c5760008155600101610a665600a165627a7a72305820ddca221fe1d171f26e56b6786067ef9d301c26f487acd3fdda88d1a9a0df0ead0029";

    public static final String FUNC_GETORGROLEIDS = "getOrgRoleIds";

    public static final String FUNC_ASSIGNWRITER = "assignWriter";

    public static final String FUNC_CANDISABLE = "canDisable";

    public static final String FUNC_DISABLE = "disable";

    public static final String FUNC_ASSIGNDENIED = "assignDenied";

    public static final String FUNC_GETUSERROLEIDS = "getUserRoleIds";

    public static final String FUNC_UPGRADEWITH = "upgradeWith";

    public static final String FUNC_SUCCESSOR = "successor";

    public static final String FUNC_CONTRACTNAME = "contractName";

    public static final String FUNC_PRECURSOR = "precursor";

    public static final String FUNC_GETMODIFIERIDS = "getModifierIds";

    public static final String FUNC_CONTRACTVERSION = "contractVersion";

    public static final String FUNC_ENABLE = "enable";

    public static final String FUNC_CANBEADMIN = "canBeAdmin";

    public static final String FUNC_CANWRITE = "canWrite";

    public static final String FUNC_CANREAD = "canRead";

    public static final String FUNC_MODIFIERIDS = "modifierIds";

    public static final String FUNC_ASSIGNREADER = "assignReader";

    public static final String FUNC_UPDATEADMIN = "updateAdmin";

    public static final String FUNC_DISABLED = "disabled";

    public static final String FUNC_ADMIN = "admin";

    @Deprecated
    protected RoleModel(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected RoleModel(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected RoleModel(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected RoleModel(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<List> getOrgRoleIds(byte[] _modifierId) {
        final Function function = new Function(FUNC_GETORGROLEIDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<TransactionReceipt> assignWriter(byte[] _modifierId, byte[] _orgRoleId, byte[] _userRoleId) {
        final Function function = new Function(
                FUNC_ASSIGNWRITER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_userRoleId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> canDisable() {
        final Function function = new Function(FUNC_CANDISABLE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> disable() {
        final Function function = new Function(
                FUNC_DISABLE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> assignDenied(byte[] _modifierId, byte[] _orgRoleId, byte[] _userRoleId) {
        final Function function = new Function(
                FUNC_ASSIGNDENIED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_userRoleId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getUserRoleIds(byte[] _modifierId, byte[] _orgRoleId) {
        final Function function = new Function(FUNC_GETUSERROLEIDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<TransactionReceipt> upgradeWith(String _successor) {
        final Function function = new Function(
                FUNC_UPGRADEWITH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_successor)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> successor() {
        final Function function = new Function(FUNC_SUCCESSOR, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> contractName() {
        final Function function = new Function(FUNC_CONTRACTNAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> precursor() {
        final Function function = new Function(FUNC_PRECURSOR, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<List> getModifierIds() {
        final Function function = new Function(FUNC_GETMODIFIERIDS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<String> contractVersion() {
        final Function function = new Function(FUNC_CONTRACTVERSION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> enable() {
        final Function function = new Function(
                FUNC_ENABLE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> canBeAdmin(String _newAdmin) {
        final Function function = new Function(FUNC_CANBEADMIN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_newAdmin)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canWrite(byte[] _modifierId, byte[] _orgRoleId, byte[] _userRoleId) {
        final Function function = new Function(FUNC_CANWRITE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_userRoleId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canRead(byte[] _modifierId, byte[] _orgRoleId, byte[] _userRoleId) {
        final Function function = new Function(FUNC_CANREAD, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_userRoleId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<byte[]> modifierIds(BigInteger param0) {
        final Function function = new Function(FUNC_MODIFIERIDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> assignReader(byte[] _modifierId, byte[] _orgRoleId, byte[] _userRoleId) {
        final Function function = new Function(
                FUNC_ASSIGNREADER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_userRoleId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> updateAdmin(String _newAdmin) {
        final Function function = new Function(
                FUNC_UPDATEADMIN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_newAdmin)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> disabled() {
        final Function function = new Function(FUNC_DISABLED, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> admin() {
        final Function function = new Function(FUNC_ADMIN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    @Deprecated
    public static RoleModel load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new RoleModel(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static RoleModel load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new RoleModel(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RoleModel load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new RoleModel(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static RoleModel load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new RoleModel(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<RoleModel> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String _precursor) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor)));
        return deployRemoteCall(RoleModel.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<RoleModel> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String _precursor) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor)));
        return deployRemoteCall(RoleModel.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<RoleModel> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _precursor) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor)));
        return deployRemoteCall(RoleModel.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<RoleModel> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _precursor) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor)));
        return deployRemoteCall(RoleModel.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }
}
