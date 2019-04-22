var Registry = artifacts.require("./storage/Registry.sol");
var RoleModel = artifacts.require("./storage/RoleModel.sol");
var DDSystem = artifacts.require("./DDSystem.sol");

var data = require("../test/data.js")(web3);

module.exports = async (deployer) => {
    //let registry = await deployer.deploy(Registry, 0, data.AFT.orgId, data.AFT.nodeId, data.cert(), data.cert(), data.cert(), data.cert(), {from: data.AFT.admin.id});
    //let rolemodel = await deployer.deploy(RoleModel, 0, {from: data.AFT.admin.id});
    //await deployer.deploy(DDSystem, 0, registry.address, rolemodel.address, 0, 0, {from: data.AFT.admin.id});

    // https://www.sitepoint.com/truffle-migrations-explained/
    deployer.deploy(Registry, 0, data.AFT.orgId, data.AFT.nodeId, data.cert(), data.cert(), data.cert(), data.cert(), {from: data.AFT.admin.id})
        .then(() => Registry.deployed())
        .then(() => deployer.deploy(RoleModel, 0, {from: data.AFT.admin.id}))
        .then(() => RoleModel.deployed())
        .then(() => deployer.deploy(DDSystem, 0, Registry.address, RoleModel.address, 0, 0, {from: data.AFT.admin.id}));
};
