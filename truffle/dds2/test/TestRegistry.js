require('chai').use(require("chai-as-promised"))
require('chai/register-should')

var Registry = artifacts.require("Registry")
var RoleModel = artifacts.require("RoleModel")
var Container = artifacts.require("Container")

var data = require("./data.js")(web3)
var util = require("./util.js")(web3)

function newRegistry() {
    return Registry.new(0, data.AFT.orgId, data.AFT.nodeId,
            data.cert(), data.cert(), data.cert(), data.cert(),
            {from: data.AFT.admin.id})
}

var newProfile = async (registry) => {
    let roleModel = await RoleModel.new(0)
    let container = await Container.new(0, registry.address,
            roleModel.address, data.modifier, data.AFT.roleId)
    await container.appendArchive(data.modifier, data.hmac(), data.expiry)
    return container
}

contract("Registry", (accounts) => {
    it("admin should be the deployer and should have the orgId and the nodeId", async () => {

        let registry = await newRegistry()

        let admin = await registry.admin.call()
        util.assertEq(admin, data.AFT.admin.id, "wrong admin")

        let nodeIsKnown = await registry.nodeIsKnown.call(data.AFT.nodeId)
        assert.equal(nodeIsKnown, true, "node is unknown")

        let orgIsKnown = await registry.orgIsKnown.call(data.AFT.orgId)
        assert.equal(orgIsKnown, true, "org is unknown")

        nodeIsKnown = await registry.nodeIsKnown.call(data.QBT.orgId)
        assert.equal(nodeIsKnown, false, "node is known")

        orgIsKnown = await registry.orgIsKnown.call(data.QBT.orgId)
        assert.equal(orgIsKnown, false, "org is known")

        let userIsKnown = await registry.userIsKnown.call(data.AFT.admin.id)
        assert.equal(userIsKnown, true, "user is unknown")

        userIsKnown = await registry.userIsKnown.call(data.QBT.admin.id)
        assert.equal(userIsKnown, false, "user is known")

        let orgId = await registry.getUserOrgId.call(admin)
        util.assertEq(orgId, data.AFT.orgId, "wrong orgId")

        let nodeIds = await registry.getUserNodeIds.call(admin)
        util.assertEq(nodeIds, [data.AFT.nodeId], "wrong nodeId")

        let orgIds = await registry.getOrgIds.call()
        util.assertEq(orgIds, [data.AFT.orgId], "wrong orgIds")

        let userIds = await registry.getOrgUserIds.call(data.AFT.orgId)
        util.assertEq(userIds, [data.AFT.admin.id], "wrong node user list")
    })
    it("should not update admin by not the registry admin", async () => {

        let registry = await newRegistry()
        await util.runTillGasLimit(
            registry.updateAdmin(data.AFT.admin.id, {from: data.AFT.signer.id})
            ).should.be.rejected
    })
    it("should not update admin if new admin is unknown", async () => {

        let registry = await newRegistry()
        await util.runTillGasLimit(
            registry.updateAdmin(data.unknown.userId)
            ).should.be.rejected
    })
    it("should update admin to a known user by current admin", async () => {

        let registry = await newRegistry()
        let container = await newProfile(registry)

        await registry.createUser(data.AFT.orgId, data.unknown.userId,
                container.address, data.cert(), data.cert())

        let userIds = await registry.getOrgUserIds.call(data.AFT.orgId)
        util.assertEq(userIds, [data.AFT.admin.id, data.unknown.userId], "wrong node user list")

        let userIsKnown = await registry.userIsKnown.call(data.unknown.userId)
        assert.equal(userIsKnown, true, "user is unknown")

        await registry.updateAdmin(data.unknown.userId)

        let admin = await registry.admin.call()
        util.assertEq(admin, data.unknown.userId, "wrong admin")
    })
    it("should recognize the admin and a user by their signatures", async () => {

        let registry = await newRegistry()

        let s = await util.sign(data.AFT.admin.id, Date.now(), registry)
        let recovered = await registry.getUserBySignature.call(s.hash, s.r, s.s, s.v)
        util.assertEq(recovered, data.AFT.admin.id, 'admin not recognized')
    })
    it("should not create a node by not the registry admin", async () => {

        let registry = await newRegistry()
        let container = await newProfile(registry)

        await util.runTillGasLimit(
            registry.createNode(data.QBT.orgId, container.address,
                data.QBT.nodeId, container.address, data.cert(), data.cert(),
                data.QBT.admin.id, container.address, data.cert(), data.cert(),
                {from: data.AFT.signer.id})
            ).should.be.rejected
    })
    it("should not create a node if its admin is unknown", async () => {

        let registry = await newRegistry()
        let container = await newProfile(registry)

        await util.runTillGasLimit(
            registry.createNode(data.QBT.orgId, container.address,
                data.QBT.nodeId, container.address, data.cert(), data.cert(),
                data.unknown.userId, container.address, data.cert(), data.cert(),
                {from: data.AFT.signer.id})
            ).should.be.rejected
    })
    it("should create a node and point to it a user with the nodeId", async () => {

        let registry = await newRegistry()
        let container = await newProfile(registry)

        await registry.createNode(data.QBT.orgId, container.address,
                data.QBT.nodeId, container.address, data.cert(), data.cert(),
                data.QBT.admin.id, container.address, data.cert(), data.cert(),
                {from: data.AFT.admin.id})

        let orgIds = await registry.getOrgIds.call()
        util.assertEq(orgIds, [data.AFT.orgId, data.QBT.orgId], "wrong orgIds")

        let nodeIds = await registry.getUserNodeIds.call(data.QBT.admin.id)
        util.assertEq(nodeIds, [data.QBT.nodeId], "wrong nodeIds")
    })
    it("should create a node and change its admin and nodeId", async () => {

        let registry = await newRegistry()
        let container = await newProfile(registry)

        await registry.createNode(data.QBT.orgId, container.address,
                data.QBT.nodeId, container.address, data.cert(), data.cert(),
                data.QBT.admin.id, container.address, data.cert(), data.cert(),
                {from: data.AFT.admin.id})

        await registry.createUser(data.QBT.orgId, data.unknown.userId,
                container.address, data.cert(), data.cert())

        await registry.updateNodeAdmin(data.QBT.nodeId, data.unknown.userId)
        let admin = await registry.getNodeAdmin.call(data.QBT.nodeId)
        util.assertEq(admin, data.unknown.userId, "wrong admin")

        await registry.updateOrgAdmin(data.QBT.orgId, data.unknown.userId)
        admin = await registry.getOrgAdmin.call(data.QBT.orgId)
        util.assertEq(admin, data.unknown.userId, "wrong admin")

        await registry.appendOrgNodeId(data.QBT.orgId, data.AFT.nodeId)

        let nodeIds = await registry.getOrgNodeIds.call(data.QBT.orgId)
        util.assertEq(nodeIds, [data.QBT.nodeId, data.AFT.nodeId], "wrong nodeIds")

        await registry.removeOrgNodeId(data.QBT.orgId, data.QBT.nodeId)

        nodeIds = await registry.getOrgNodeIds.call(data.QBT.orgId)
        util.assertEq(nodeIds, [data.AFT.nodeId], "wrong nodeIds")
    })
    it("should not add a user if not the registry admin or the node admin", async () => {

        let registry = await newRegistry()
        let container = await newProfile(registry)

        await registry.createNode(data.QBT.orgId, container.address,
                data.QBT.nodeId, container.address, data.cert(), data.cert(),
                data.QBT.admin.id, container.address, data.cert(), data.cert(),
                {from: data.AFT.admin.id})

        await util.runTillGasLimit(
            registry.createUser(data.unknown.userId, data.QBT.orgId,
                {from: data.QBT.signer.id})
            ).should.be.rejected
    })
    it("should return Registry node and admin certificates", async () => {

        let [sigAFTNode, encAFTNode] = [data.cert(), data.cert()]
        let [sigAFTAdmin, encAFTAdmin] = [data.cert(), data.cert()]

        let registry = await Registry.new(0, data.AFT.orgId, data.AFT.nodeId,
                sigAFTNode, encAFTNode, sigAFTAdmin, encAFTAdmin,
                {from: data.AFT.admin.id})
        let container = await newProfile(registry)

        let sigCert = await registry.getNodeSigCert(data.AFT.nodeId)
        util.assertEq(sigCert, sigAFTNode, "wrong node signature certificate")
        let encCert = await registry.getNodeEncCert(data.AFT.nodeId)
        assert.equal(encCert, encAFTNode, "wrong node encryption certificate")

        sigCert = await registry.getUserSigCert(data.AFT.admin.id)
        util.assertEq(sigCert, sigAFTAdmin, "wrong admin signature certificate")
        encCert = await registry.getUserEncCert(data.AFT.admin.id)
        assert.equal(encCert, encAFTAdmin, "wrong admin encryption certificate")

    })
    it("should return new node and its admin and user certificates", async () => {

        let registry = await newRegistry()
        let container = await newProfile(registry)

        let [sigQBTNode, encQBTNode] = [data.cert(), data.cert()]
        let [sigQBTAdmin, encQBTAdmin] = [data.cert(), data.cert()]
        await registry.createNode(data.QBT.orgId, container.address,
                data.QBT.nodeId, container.address, sigQBTNode, encQBTNode,
                data.QBT.admin.id, container.address, sigQBTAdmin, encQBTAdmin,
                {from: data.AFT.admin.id})

        let sigCert = await registry.getNodeSigCert(data.QBT.nodeId)
        util.assertEq(sigCert, sigQBTNode, "wrong node signature certificate")
        let encCert = await registry.getNodeEncCert(data.QBT.nodeId)
        assert.equal(encCert, encQBTNode, "wrong node encryption certificate")

        sigCert = await registry.getUserSigCert(data.QBT.admin.id)
        util.assertEq(sigCert, sigQBTAdmin, "wrong admin signature certificate")
        encCert = await registry.getUserEncCert(data.QBT.admin.id)
        assert.equal(encCert, encQBTAdmin, "wrong admin encryption certificate")

        let [sigQBTUser, encQBTUser] = [data.cert(), data.cert()]
        await registry.createUser(data.QBT.orgId, data.QBT.signer.id,
                container.address, sigQBTUser, encQBTUser)

        sigCert = await registry.getUserSigCert(data.QBT.signer.id)
        assert.equal(sigCert, sigQBTUser, "wrong user signature certificate")
        encCert = await registry.getUserEncCert(data.QBT.signer.id)
        assert.equal(encCert, encQBTUser, "wrong user encryption certificate")
    })
})

