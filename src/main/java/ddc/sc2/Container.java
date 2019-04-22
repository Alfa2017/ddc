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
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
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
public class Container extends Contract {
    private static final String BINARY = "606060405234156200001057600080fd5b60405160a08062001c0a83398101604052808051919060200180519190602001805191906020018051919060200180519150859050600160a060020a03811615620000cf5780600160a060020a031663589bfb59306040517c010000000000000000000000000000000000000000000000000000000063ffffffff8416028152600160a060020a039091166004820152602401600060405180830381600087803b1515620000bd57600080fd5b5af11515620000cb57600080fd5b5050505b6000805461010060a860020a031916610100600160a060020a039384160217905560058054600160a060020a031990811687841617909155600680549091169185169190911790556007829055620001358164010000000062000140810262000ef61704565b50505050506200037d565b600254600090156200015157600080fd5b6200016964010000000062000e3f620002bd82021704565b600160a060020a03166390e6d7106200018f640100000000620006c7620002cd82021704565b620001a7640100000000620015db620002dc82021704565b856040517c010000000000000000000000000000000000000000000000000000000063ffffffff8616028152600160a060020a03909316600484015260248301919091526044820152606401602060405180830381600087803b15156200020d57600080fd5b5af115156200021b57600080fd5b5050506040518051905090506000604051805910620002375750595b9080825280602002602001820160405250600082815260036020526040902090805162000269929160200190620002e2565b5060008181526003602052604090208054600181016200028a838262000334565b5060009182526020909120018290556002805460018101620002ad838262000334565b5060009182526020909120015550565b600554600160a060020a03165b90565b600654600160a060020a031690565b60075490565b82805482825590600052602060002090810192821562000322579160200282015b8281111562000322578251825560209092019160019091019062000303565b506200033092915062000360565b5090565b8154818355818115116200035b576000838152602090206200035b91810190830162000360565b505050565b620002ca91905b8082111562000330576000815560010162000367565b61187d806200038d6000396000f3006060604052600436106101be5763ffffffff60e060020a6000350416630a82675881146101c35780630afe78a5146101f25780630c9ff24e1461025b578063136c70ec146102865780631505f461146102bc5780631a573646146102e75780631c997788146102fa5780631ef92578146103105780631f3f2c3314610323578063211bc6e61461033c57806325658def146103525780632ec541cc146103685780632f2770db1461037e578063382a1571146103915780634b190dc8146103a7578063589bfb59146103bd5780636ff968c3146103dc578063731f77b3146103ef57806375d0c0dc146104115780637b1039991461049b5780637c5111de146104ae5780637fbccfc6146104e15780638b3e2df7146104f45780638f2241b91461050a5780639498bd71146105235780639558f9ec14610539578063962d364a1461054f578063985e0dfb14610568578063a0a8e4601461058e578063a3907d71146105a1578063ae856ff5146105b4578063afbe060f146105d7578063b048324714610639578063b2180eab1461065f578063e4f6499014610678578063ee0708051461068b578063f17a68f51461069e578063fc6becd9146106b4575b600080fd5b34156101ce57600080fd5b6101d66106c7565b604051600160a060020a03909116815260200160405180910390f35b34156101fd57600080fd5b6102086004356106d7565b60405160208082528190810183818151815260200191508051906020019060200280838360005b8381101561024757808201518382015260200161022f565b505050509050019250505060405180910390f35b341561026657600080fd5b610274600435602435610748565b60405190815260200160405180910390f35b341561029157600080fd5b6102a8600160a060020a0360043516602435610776565b604051901515815260200160405180910390f35b34156102c757600080fd5b6102e560043560243560443567ffffffffffffffff606435166107b3565b005b34156102f257600080fd5b61027461093e565b341561030557600080fd5b610274600435610944565b341561031b57600080fd5b6102a861096e565b341561032e57600080fd5b6102e56004356024356109cf565b341561034757600080fd5b610274600435610be5565b341561035d57600080fd5b610274600435610c0f565b341561037357600080fd5b610274600435610c39565b341561038957600080fd5b6102e5610c63565b341561039c57600080fd5b610274600435610ca6565b34156103b257600080fd5b610274600435610cb8565b34156103c857600080fd5b6102e5600160a060020a0360043516610d0f565b34156103e757600080fd5b6101d6610d39565b34156103fa57600080fd5b6102a8600160a060020a0360043516602435610d48565b341561041c57600080fd5b610424610dfe565b60405160208082528190810183818151815260200191508051906020019080838360005b83811015610460578082015183820152602001610448565b50505050905090810190601f16801561048d5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156104a657600080fd5b6101d6610e3f565b34156104b957600080fd5b6104c4600435610e4e565b60405167ffffffffffffffff909116815260200160405180910390f35b34156104ec57600080fd5b6101d6610e83565b34156104ff57600080fd5b6101d6600435610e97565b341561051557600080fd5b610274600435602435610ec8565b341561052e57600080fd5b6102e5600435610ef6565b341561054457600080fd5b610274600435611020565b341561055a57600080fd5b6102e560043560243561103f565b341561057357600080fd5b61027460043560243567ffffffffffffffff604435166110d1565b341561059957600080fd5b610424611238565b34156105ac57600080fd5b6102e5611264565b34156105bf57600080fd5b6102e560043567ffffffffffffffff60243516611284565b34156105e257600080fd5b6105ed600435611305565b604051600160a060020a039096168652602086019490945260408086019390935267ffffffffffffffff9091166060850152608084015260a083019190915260c0909101905180910390f35b341561064457600080fd5b61027460043560243567ffffffffffffffff60443516611360565b341561066a57600080fd5b6102e5600435602435611417565b341561068357600080fd5b610208611547565b341561069657600080fd5b6102a86115a6565b34156106a957600080fd5b6102a86004356115af565b34156106bf57600080fd5b6102746115db565b600654600160a060020a03165b90565b6106df611739565b60008281526003602090815260409182902080549092909182810201905190810160405280929190818152602001828054801561073c57602002820191906000526020600020905b81548152600190910190602001808311610727575b50505050509050919050565b60036020528160005260406000208181548110151561076357fe5b6000918252602090912001549150829050565b600082600160a060020a031660048381548110151561079157fe5b6000918252602090912060069091020154600160a060020a0316149392505050565b6000805460ff161580156107ca5750600254600090115b15156107d557600080fd5b6107dd610e3f565b600160a060020a0316633eea823d6107f36106c7565b6004548790899060405160e060020a63ffffffff8716028152600160a060020a039094166004850152602484019290925260448301526064820152608401600060405180830381600087803b151561084a57600080fd5b5af1151561085757600080fd5b505060048054925090508160018101610870838261174b565b9160005260206000209060060201600060c06040519081016040908152600160a060020a033216825260208201899052810187905267ffffffffffffffff861660608201526080810185905260a0810185905291905081518154600160a060020a031916600160a060020a03919091161781556020820151600182015560408201516002820155606082015160038201805467ffffffffffffffff191667ffffffffffffffff929092169190911790556080820151816004015560a082015181600501555050505050505050565b60045490565b600060048281548110151561095557fe5b9060005260206000209060060201600101549050919050565b6000610978610e3f565b600160a060020a031663e41cdac56040518163ffffffff1660e060020a028152600401602060405180830381600087803b15156109b457600080fd5b5af115156109c157600080fd5b505050604051805191505090565b60008054819060ff161580156109e85750600254600090115b15156109f357600080fd5b60008481526003602052604081205411610a0c57600080fd5b610a14610e3f565b600160a060020a031663d557dffb858560405160e060020a63ffffffff851602815260048101929092526024820152604401600060405180830381600087803b1515610a5f57600080fd5b5af11515610a6c57600080fd5b50505060008481526003602052604090205460011415610bdf5760025491505b6000821115610bdf57600280548591906000198501908110610aaa57fe5b6000918252602090912001541415610b0a57600280546000198101908110610ace57fe5b600091825260209091200154600280546000198501908110610aec57fe5b50600052506002805490610b04906000198301611777565b50610bdf565b60008481526003602052604090205491505b6000821115610bd357600084815260036020526040902080548491906000198501908110610b4657fe5b6000918252602090912001541415610bc75750600083815260036020526040902080546000198101919082908110610b7a57fe5b60009182526020808320909101548683526003909152604090912080546000198501908110610ba557fe5b5050506000848152600360205260409020805490610b04906000198301611777565b60001990910190610b1c565b60001990910190610a8c565b50505050565b6000600482815481101515610bf657fe5b9060005260206000209060060201600501549050919050565b6000600482815481101515610c2057fe5b9060005260206000209060060201600401549050919050565b6000600482815481101515610c4a57fe5b9060005260206000209060060201600201549050919050565b60005460ff16158015610c795750600254600090115b1515610c8457600080fd5b610c8c61096e565b1515610c9757600080fd5b6000805460ff19166001179055565b60009081526003602052604090205490565b60005b81600483815481101515610ccb57fe5b906000526020600020906006020160050154141515610d0b576004805483908110610cf257fe5b9060005260206000209060060201600501549150610cbb565b5090565b610d17610c63565b60018054600160a060020a031916600160a060020a0392909216919091179055565b600154600160a060020a031681565b6004546000908210610d5957600080fd5b610d61610e3f565b600160a060020a03166351a998dd8430600486815481101515610d8057fe5b90600052602060002090600602016001015460405160e060020a63ffffffff8616028152600160a060020a0393841660048201529190921660248201526044810191909152606401602060405180830381600087803b1515610de157600080fd5b5af11515610dee57600080fd5b5050506040518051949350505050565b610e06611739565b60408051908101604052601181527f4146544d73746f72436f6e7461696e65720000000000000000000000000000006020820152905090565b600554600160a060020a031690565b6000600482815481101515610e5f57fe5b600091825260209091206006909102016003015467ffffffffffffffff1692915050565b6000546101009004600160a060020a031681565b6000600482815481101515610ea857fe5b6000918252602090912060069091020154600160a060020a031692915050565b6000828152600360205260408120805483908110610ee257fe5b906000526020600020900154905092915050565b60025460009015610f0657600080fd5b610f0e610e3f565b600160a060020a03166390e6d710610f246106c7565b610f2c6115db565b8560405160e060020a63ffffffff8616028152600160a060020a03909316600484015260248301919091526044820152606401602060405180830381600087803b1515610f7857600080fd5b5af11515610f8557600080fd5b5050506040518051905090506000604051805910610fa05750595b90808252806020026020018201604052506000828152600360205260409020908051610fd092916020019061179b565b506000818152600360205260409020805460018101610fef8382611777565b50600091825260209091200182905560028054600181016110108382611777565b5060009182526020909120015550565b600280548290811061102e57fe5b600091825260209091200154905081565b60005460ff161580156110555750600254600090115b151561106057600080fd5b611068610e3f565b600160a060020a031663dd81ac16838360405160e060020a63ffffffff851602815260048101929092526024820152604401600060405180830381600087803b15156110b357600080fd5b5af115156110c057600080fd5b5050506110cd82826115e1565b5050565b60008054819060ff161580156110ea5750600254600090115b15156110f557600080fd5b6110fd610e3f565b600160a060020a031663c45ae9f08660405160e060020a63ffffffff84160281526004810191909152602401600060405180830381600087803b151561114257600080fd5b5af1151561114f57600080fd5b505060048054925090508160018101611168838261174b565b9160005260206000209060060201600060c06040519081016040908152600160a060020a0332168252602082018a9052810188905267ffffffffffffffff871660608201526080810185905260a0810185905291905081518154600160a060020a031916600160a060020a03919091161781556020820151600182015560408201516002820155606082015160038201805467ffffffffffffffff191667ffffffffffffffff929092169190911790556080820151816004015560a0820151600590910155509095945050505050565b611240611739565b604080519081016040526005815260d860020a64302e322e37026020820152905090565b600154600160a060020a03161561127a57600080fd5b611282611708565b565b60005460ff1615801561129a5750600254600090115b15156112a557600080fd5b6112af3383610776565b15156112ba57600080fd5b806004838154811015156112ca57fe5b906000526020600020906006020160030160006101000a81548167ffffffffffffffff021916908367ffffffffffffffff1602179055505050565b600480548290811061131357fe5b6000918252602090912060069091020180546001820154600283015460038401546004850154600590950154600160a060020a0390941695509193909267ffffffffffffffff9092169186565b600080548190819060ff1615801561137b5750600254600090115b151561138657600080fd5b61138e61093e565b861061139957600080fd5b6113a2866115af565b15156113ad57600080fd5b6113b686610944565b91506113c38286866110d1565b9050856004828154811015156113d557fe5b906000526020600020906006020160040181905550806004878154811015156113fa57fe5b600091825260209091206005600690920201015595945050505050565b6000805460ff1615801561142e5750600254600090115b151561143957600080fd5b611441610e3f565b600160a060020a0316630fe6b9493360405160e060020a63ffffffff8416028152600160a060020a039091166004820152602401602060405180830381600087803b151561148e57600080fd5b5af1151561149b57600080fd5b5050506040518051905090506114af610e3f565b600160a060020a03166339d666426114c56106c7565b6114cd6115db565b84878760405160e060020a63ffffffff8816028152600160a060020a039095166004860152602485019390935260448401919091526064830152608482015260a401600060405180830381600087803b151561152857600080fd5b5af1151561153557600080fd5b50505061154283836115e1565b505050565b61154f611739565b600280548060200260200160405190810160405280929190818152602001828054801561159c57602002820191906000526020600020905b81548152600190910190602001808311611587575b5050505050905090565b60005460ff1681565b6000816004838154811015156115c157fe5b906000526020600020906006020160050154149050919050565b60075490565b6000805460ff161580156115f85750600254600090115b151561160357600080fd5b600083815260036020526040902054151561167b57600280546001810161162a8382611777565b50600091825260208220018490556040518059106116455750595b9080825280602002602001820160405250600084815260036020526040902090805161167592916020019061179b565b506116d6565b506000828152600360205260409020545b60008111156116d6576000838152600360205260409020805483919060001984019081106116b657fe5b60009182526020909120015414156116cd57611542565b6000190161168c565b60008381526003602052604090208054600181016116f48382611777565b506000918252602090912001829055505050565b60005460ff1615156001148015611722575061172261096e565b151561172d57600080fd5b6000805460ff19169055565b60206040519081016040526000815290565b8154818355818115116115425760060281600602836000526020600020918201910161154291906117e4565b81548183558181151161154257600083815260209020611542918101908301611837565b8280548282559060005260206000209081019282156117d8579160200282015b828111156117d857825182556020909201916001909101906117bb565b50610d0b929150611837565b6106d491905b80821115610d0b578054600160a060020a03191681556000600182018190556002820181905560038201805467ffffffffffffffff191690556004820181905560058201556006016117ea565b6106d491905b80821115610d0b576000815560010161183d5600a165627a7a72305820aca1ed8c1860c4f1880d472a9395e1a43bd6f797f4fe49a50250ec3588802bd00029";

    public static final String FUNC_ROLEMODEL = "roleModel";

    public static final String FUNC_GETORGROLEIDS = "getOrgRoleIds";

    public static final String FUNC_ORGROLEIDS = "orgRoleIds";

    public static final String FUNC_USERISAUTHOR = "userIsAuthor";

    public static final String FUNC_APPENDARCHIVEFROMCONSTRUCTOR = "appendArchiveFromConstructor";

    public static final String FUNC_GETARCHIVESCOUNT = "getArchivesCount";

    public static final String FUNC_GETARCHIVETYPEID = "getArchiveTypeId";

    public static final String FUNC_CANDISABLE = "canDisable";

    public static final String FUNC_REVOKEORGROLE = "revokeOrgRole";

    public static final String FUNC_GETARCHIVENEXTVERSION = "getArchiveNextVersion";

    public static final String FUNC_GETARCHIVEPREVVERSION = "getArchivePrevVersion";

    public static final String FUNC_GETARCHIVEHMAC = "getArchiveHMAC";

    public static final String FUNC_DISABLE = "disable";

    public static final String FUNC_GETORGROLEIDSCOUNT = "getOrgRoleIdsCount";

    public static final String FUNC_GETARCHIVELATESTVERSION = "getArchiveLatestVersion";

    public static final String FUNC_UPGRADEWITH = "upgradeWith";

    public static final String FUNC_SUCCESSOR = "successor";

    public static final String FUNC_USERCANREAD = "userCanRead";

    public static final String FUNC_CONTRACTNAME = "contractName";

    public static final String FUNC_REGISTRY = "registry";

    public static final String FUNC_GETARCHIVEEXPIRY = "getArchiveExpiry";

    public static final String FUNC_PRECURSOR = "precursor";

    public static final String FUNC_GETARCHIVEAUTHORID = "getArchiveAuthorId";

    public static final String FUNC_GETORGROLEID = "getOrgRoleId";

    public static final String FUNC_INITIALIZE = "initialize";

    public static final String FUNC_ORGIDS = "orgIds";

    public static final String FUNC_GRANTORGROLE = "grantOrgRole";

    public static final String FUNC_APPENDARCHIVE = "appendArchive";

    public static final String FUNC_CONTRACTVERSION = "contractVersion";

    public static final String FUNC_ENABLE = "enable";

    public static final String FUNC_UPDATEEXPIRY = "updateExpiry";

    public static final String FUNC_ARCHIVES = "archives";

    public static final String FUNC_UPDATEARCHIVE = "updateArchive";

    public static final String FUNC_GRANTORGROLEFROMCONSTRUCTOR = "grantOrgRoleFromConstructor";

    public static final String FUNC_GETORGIDS = "getOrgIds";

    public static final String FUNC_DISABLED = "disabled";

    public static final String FUNC_ISARCHIVELATESTVERSION = "isArchiveLatestVersion";

    public static final String FUNC_MODIFIERID = "modifierId";

    @Deprecated
    protected Container(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Container(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Container(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Container(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<String> roleModel() {
        final Function function = new Function(FUNC_ROLEMODEL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<List> getOrgRoleIds(byte[] _orgId) {
        final Function function = new Function(FUNC_GETORGROLEIDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgId)), 
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

    public RemoteCall<byte[]> orgRoleIds(byte[] param0, BigInteger param1) {
        final Function function = new Function(FUNC_ORGROLEIDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(param0), 
                new org.web3j.abi.datatypes.generated.Uint256(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<Boolean> userIsAuthor(String _userId, BigInteger _archiveId) {
        final Function function = new Function(FUNC_USERISAUTHOR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_userId), 
                new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> appendArchiveFromConstructor(byte[] _orgRoleId, byte[] _modifierId, byte[] _hmac, BigInteger _expiry) {
        final Function function = new Function(
                FUNC_APPENDARCHIVEFROMCONSTRUCTOR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_hmac), 
                new org.web3j.abi.datatypes.generated.Uint64(_expiry)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getArchivesCount() {
        final Function function = new Function(FUNC_GETARCHIVESCOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<byte[]> getArchiveTypeId(BigInteger _archiveId) {
        final Function function = new Function(FUNC_GETARCHIVETYPEID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<Boolean> canDisable() {
        final Function function = new Function(FUNC_CANDISABLE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> revokeOrgRole(byte[] _orgId, byte[] _orgRoleId) {
        final Function function = new Function(
                FUNC_REVOKEORGROLE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getArchiveNextVersion(BigInteger _archiveId) {
        final Function function = new Function(FUNC_GETARCHIVENEXTVERSION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> getArchivePrevVersion(BigInteger _archiveId) {
        final Function function = new Function(FUNC_GETARCHIVEPREVVERSION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<byte[]> getArchiveHMAC(BigInteger _archiveId) {
        final Function function = new Function(FUNC_GETARCHIVEHMAC, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> disable() {
        final Function function = new Function(
                FUNC_DISABLE, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getOrgRoleIdsCount(byte[] _orgId) {
        final Function function = new Function(FUNC_GETORGROLEIDSCOUNT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> getArchiveLatestVersion(BigInteger _archiveId) {
        final Function function = new Function(FUNC_GETARCHIVELATESTVERSION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
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

    public RemoteCall<Boolean> userCanRead(String _userId, BigInteger _archiveId) {
        final Function function = new Function(FUNC_USERCANREAD, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_userId), 
                new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> contractName() {
        final Function function = new Function(FUNC_CONTRACTNAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> registry() {
        final Function function = new Function(FUNC_REGISTRY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<BigInteger> getArchiveExpiry(BigInteger _archiveId) {
        final Function function = new Function(FUNC_GETARCHIVEEXPIRY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint64>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> precursor() {
        final Function function = new Function(FUNC_PRECURSOR, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> getArchiveAuthorId(BigInteger _archiveId) {
        final Function function = new Function(FUNC_GETARCHIVEAUTHORID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<byte[]> getOrgRoleId(byte[] _orgId, BigInteger _index) {
        final Function function = new Function(FUNC_GETORGROLEID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgId), 
                new org.web3j.abi.datatypes.generated.Uint256(_index)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> initialize(byte[] _orgRoleId) {
        final Function function = new Function(
                FUNC_INITIALIZE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<byte[]> orgIds(BigInteger param0) {
        final Function function = new Function(FUNC_ORGIDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> grantOrgRole(byte[] _orgId, byte[] _orgRoleId) {
        final Function function = new Function(
                FUNC_GRANTORGROLE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> appendArchive(byte[] _modifierId, byte[] _hmac, BigInteger _expiry) {
        final Function function = new Function(
                FUNC_APPENDARCHIVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_hmac), 
                new org.web3j.abi.datatypes.generated.Uint64(_expiry)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
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

    public RemoteCall<TransactionReceipt> updateExpiry(BigInteger _archiveId, BigInteger _expiry) {
        final Function function = new Function(
                FUNC_UPDATEEXPIRY, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId), 
                new org.web3j.abi.datatypes.generated.Uint64(_expiry)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple6<String, byte[], byte[], BigInteger, BigInteger, BigInteger>> archives(BigInteger param0) {
        final Function function = new Function(FUNC_ARCHIVES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint64>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteCall<Tuple6<String, byte[], byte[], BigInteger, BigInteger, BigInteger>>(
                new Callable<Tuple6<String, byte[], byte[], BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple6<String, byte[], byte[], BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple6<String, byte[], byte[], BigInteger, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> updateArchive(BigInteger _prevId, byte[] _hmac, BigInteger _expiry) {
        final Function function = new Function(
                FUNC_UPDATEARCHIVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_prevId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_hmac), 
                new org.web3j.abi.datatypes.generated.Uint64(_expiry)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> grantOrgRoleFromConstructor(byte[] _orgId, byte[] _orgRoleId) {
        final Function function = new Function(
                FUNC_GRANTORGROLEFROMCONSTRUCTOR, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_orgId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getOrgIds() {
        final Function function = new Function(FUNC_GETORGIDS, 
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

    public RemoteCall<Boolean> disabled() {
        final Function function = new Function(FUNC_DISABLED, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> isArchiveLatestVersion(BigInteger _archiveId) {
        final Function function = new Function(FUNC_ISARCHIVELATESTVERSION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_archiveId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<byte[]> modifierId() {
        final Function function = new Function(FUNC_MODIFIERID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    @Deprecated
    public static Container load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Container(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Container load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Container(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Container load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Container(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Container load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Container(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Container> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String _precursor, String _registry, String _roleModel, byte[] _modifierId, byte[] _orgRoleId) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor), 
                new org.web3j.abi.datatypes.Address(_registry), 
                new org.web3j.abi.datatypes.Address(_roleModel), 
                new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)));
        return deployRemoteCall(Container.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<Container> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String _precursor, String _registry, String _roleModel, byte[] _modifierId, byte[] _orgRoleId) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor), 
                new org.web3j.abi.datatypes.Address(_registry), 
                new org.web3j.abi.datatypes.Address(_roleModel), 
                new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)));
        return deployRemoteCall(Container.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Container> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _precursor, String _registry, String _roleModel, byte[] _modifierId, byte[] _orgRoleId) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor), 
                new org.web3j.abi.datatypes.Address(_registry), 
                new org.web3j.abi.datatypes.Address(_roleModel), 
                new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)));
        return deployRemoteCall(Container.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Container> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _precursor, String _registry, String _roleModel, byte[] _modifierId, byte[] _orgRoleId) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_precursor), 
                new org.web3j.abi.datatypes.Address(_registry), 
                new org.web3j.abi.datatypes.Address(_roleModel), 
                new org.web3j.abi.datatypes.generated.Bytes32(_modifierId), 
                new org.web3j.abi.datatypes.generated.Bytes32(_orgRoleId)));
        return deployRemoteCall(Container.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }
}
