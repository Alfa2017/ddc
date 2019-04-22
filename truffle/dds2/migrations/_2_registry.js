var Registry = artifacts.require("./Registry.sol")

var data = require("../test/data.js")(web3)

module.exports = async (deployer) => {
    await deployer.deploy(Registry, 0, data.AFT.orgId, data.AFT.nodeId,
            data.cert(), data.cert(), data.cert(), data.cert(),
            {from: data.AFT.admin.id})
}
