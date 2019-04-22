require('chai').use(require("chai-as-promised"));
require('chai/register-should');
//require('truffle-test-utils').init()

var Registry = artifacts.require("Registry");
var RoleModel = artifacts.require("RoleModel");
var Container = artifacts.require("Container");

//const Depositories = artifacts.require("./controllers/Organizations.sol");
//var Depositories = artifacts.require("./controllers/Organizations.sol");
var DDSSystems = artifacts.require("./DDSystem");

var data = require("./data.js")(web3);
var util = require("./util.js")(web3);

var registry = null;
var getRegistry = async () => {
    if (registry == null) {
        registry = await Registry.new(0, data.AFT.orgId, data.AFT.nodeId, data.AFT.nodesigcert, data.AFT.nodecrycert, data.AFT.sigcert, data.AFT.crycert);

        let roleModel = await RoleModel.new(0);
        let container = await Container.new(0, registry.address, roleModel.address, data.modifier, data.AFT.roleId);
        await container.appendArchive(data.modifier, data.archiveHMACs[0], data.expiry);
        await registry.createUser(data.AFT.orgId, data.AFT.writer.id, container.address, data.AFT.sigcert, data.AFT.crycert);
        await registry.grantUserRole(data.AFT.writer.id, data.AFT.writer.roleId, 0);  // sgn: разобраться почему c "container.address" не работает
        await registry.createUser(data.AFT.orgId, data.AFT.reader.id, container.address, data.AFT.sigcert, data.AFT.crycert);
        await registry.grantUserRole(data.AFT.reader.id, data.AFT.reader.roleId, 0);  // sgn: разобраться почему c "container.address" не работает
        await registry.createNode(data.QBT.orgId, container.address,
                                  data.QBT.nodeId, container.address, data.QBT.admin.id, container.address,
                                  data.AFT.sigcert, data.AFT.crycert);
        await registry.createUser(data.QBT.orgId, data.QBT.writer.id, container.address, data.QBT.sigcert, data.QBT.crycert);
        await registry.grantUserRole(data.QBT.writer.id, data.QBT.writer.roleId, 0);    // sgn: разобраться почему c "container.address" не работает
        await registry.createUser(data.QBT.orgId, data.QBT.reader.id, container.address, data.QBT.sigcert, data.QBT.crycert);
        await registry.grantUserRole(data.QBT.reader.id, data.QBT.reader.roleId, 0);    // sgn: разобраться почему c "container.address" не работает
    }
    return registry
};

var roleModel = null;
var getRoleModel = async () => {
    if (roleModel == null) {
        roleModel = await RoleModel.new(0);
        await roleModel.assignWriter(data.modifier, data.AFT.roleId, data.AFT.writer.roleId);
        await roleModel.assignReader(data.modifier, data.AFT.roleId, data.AFT.reader.roleId);
        await roleModel.assignWriter(data.modifier, data.QBT.roleId, data.QBT.writer.roleId);
        await roleModel.assignReader(data.modifier, data.QBT.roleId, data.QBT.reader.roleId);
    }
    return roleModel
};

var deployAll = async (orgRoleId, userId) => {
    let registry = await getRegistry();
    let roleModel = await getRoleModel();

    let container = await Container.new(0, registry.address, roleModel.address, data.modifier, orgRoleId, {from: userId});
    await container.appendArchive(data.modifier, data.archiveHMACs[0], data.expiry);

    return [registry, roleModel, container];
};

contract("Organisations", (accounts) => {
    it("organizations", async () => {
            //let [registry, roleModel, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id) // <- разобраться, почему с этим account'ом не работает
            let [registry, roleModel, container] = await deployAll(data.AFT.roleId, data.AFT.admin.id);

            let orgIds = await container.getOrgIds.call();
            console.log('orgIds: ' + orgIds.map(web3.toUtf8));  //ok: строкаи в нормальном виде

            // создаем организацию
            //orgId, указанный в createOrg, не должен совпадать с orgId, указанный при создании смарт-контракта Registry
            //nodeId, указанный в createOrg, должен совпадать с nodeId, указанный при создании смарт-контракта Registry
            //админ создаваемой организации не должен совпадать с админом АФТ или каким-то другим уже использующимся адресом
            await registry.createOrg(web3.toHex("OGRN:1"), data.AFT.nodeId, container.address, accounts[8], container.address, data.AFT.sigcert, data.AFT.crycert);  // data.AFT.admin.id

            // связываем адрес админа организации с userRoleId
            //await registry.grantUserRole(accountList[3], data.AFT.writer.roleId, 0);
            await registry.grantUserRole(accounts[8], data.AFT.writer.roleId, 0, {from: data.AFT.admin.id});

            //sgn:-[ успользуемые данные ]-----------
            console.log(`acc= ${data.AFT.admin.id}`);
            console.log(`acc= ${accounts[8]}`);

            orgIds = await registry.getOrgIds.call();
            console.log(`registry :: orgIds (${orgIds.length}) => [${orgIds.map(web3.toUtf8)}]`);

            let userIds = await registry.getOrgUserIds.call(orgIds[1]);
            console.log(`registry :: orgId[${web3.toUtf8(orgIds[1])}] :: userIds (${userIds.length}) => [${userIds}]`);

            //for grandUser
            let archCnt = await container.getArchivesCount();
            let authorId = await container.getArchiveAuthorId(0);
            console.log(`container :: archive (${archCnt}) => archId=[0] authorId= [${authorId}]`);

            //sgn: end

            // выдаем права на создание контейнера
            //await roleModel.assignWriter(web3.utils.fromUtf8("AFTMstorContainerCreate"), data.AFT.roleId, data.AFT.writer.roleId);
            await roleModel.assignWriter(web3.toHex("AFTMstorContainerCreate"), data.AFT.roleId, data.AFT.writer.roleId);

            // выдаем права на создание архива в контейнере
            //await roleModel.assignWriter(web3.utils.fromUtf8("AFTMstorArchiveCreate"), data.AFT.roleId, data.AFT.writer.roleId);
            await roleModel.assignWriter(web3.toHex("AFTMstorArchiveCreate"), data.AFT.roleId, data.AFT.writer.roleId);


            //sgn :: создаем организацию
            let ddssystems = await DDSSystems.new(0, registry.address, roleModel.address, 0,0);
            let cn = await ddssystems.contractName();
            console.log('contract :: name -> ' + cn);
            depository = await ddssystems.editOrganization(web3.fromUtf8("AFTDDSAddOrganization"), data.AFT.orgId, container.address, web3.fromUtf8("testMeta"));
            depositoryAddress = depository.logs[0].args.organization;  // <- что это такое!?

            let [a,b,c] = await ddssystems.getOrgInfo(data.AFT.orgId);
            console.log(`depository :: isExist -> ${a} container -> ${b} meta -> ${c}`);

            await ddssystems.editOrganization(web3.fromUtf8("AFTDDSAddOrganization"), data.QBT.orgId, container.address, web3.fromUtf8("testMetaQBT"));
            await ddssystems.editDeponent(web3.fromUtf8("AFTDDSAddDeponent"), data.AFT.orgId,data.QBT.orgId, container.address, web3.fromUtf8("testMetaQBTDeponent"));

            /*
            // деплой контракта Depository
            //depository = await Depositories.newOrganization(0, data.AFT.orgId, web3.fromUtf8("testMeta"));  // sgn: "0x0000000000000000000000000000000000000000"
            depository = await Depositories.editOrganization(0, data.AFT.orgId, container.address, web3.fromUtf8("testMeta"));
            depositoryAddress = depository.logs[0].args.organization;
            //*
            // добавляем админа созданной организации в список менеджеров депозитария
            await managers.addManager(depositoryAddress, accountList[3], 1, web3.utils.fromUtf8("testName"));

            let depositoryInstance = await Depository.at(depositoryAddress);
            container = await DdsContainer.new(0,  //"0x0000000000000000000000000000000000000000"
                registry.address,  roleModel.address,
                web3.utils.fromUtf8("AFTMstorContainerCreate"), data.AFT.roleId,
                web3.utils.fromUtf8("testMeta"),
                {from: accountList[3]});
            await depositoryInstance.setContainer(container.address, {from: accountList[3]});
            */
            //console.log("End of preparation!");
    });
});