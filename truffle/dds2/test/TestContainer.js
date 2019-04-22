require('chai').use(require("chai-as-promised"))
require('chai/register-should')

var Registry = artifacts.require("Registry")
var RoleModel = artifacts.require("RoleModel")
var Container = artifacts.require("Container")

var data = require("./data.js")(web3)
var util = require("./util.js")(web3)

var registry = null
var getRegistry = async () => {
    if (registry == null) {
        registry = await Registry.new(0, data.AFT.orgId, data.AFT.nodeId,
                data.cert(), data.cert(), data.cert(), data.cert())

        let roleModel = await RoleModel.new(0)
        let container = await Container.new(0, registry.address,
                roleModel.address, data.modifier, data.AFT.roleId)
        await container.appendArchive(data.modifier, data.hmac(), data.expiry)

        await registry.createUser(data.AFT.orgId, data.AFT.writer.id,
                container.address, data.cert(), data.cert())
        await registry.grantUserRole(data.AFT.writer.id, data.AFT.writer.roleId, 0)

        await registry.createUser(data.AFT.orgId, data.AFT.reader.id,
                container.address, data.cert(), data.cert())
        await registry.grantUserRole(data.AFT.reader.id, data.AFT.reader.roleId, 0)

        await registry.createNode(data.QBT.orgId, container.address,
                data.QBT.nodeId, container.address, data.cert(), data.cert(),
                data.QBT.admin.id, container.address, data.cert(), data.cert())

        await registry.createUser(data.QBT.orgId, data.QBT.writer.id,
                container.address, data.cert(), data.cert())
        await registry.grantUserRole(data.QBT.writer.id, data.QBT.writer.roleId, 0)

        await registry.createUser(data.QBT.orgId, data.QBT.reader.id,
                container.address, data.cert(), data.cert())
        await registry.grantUserRole(data.QBT.reader.id, data.QBT.reader.roleId, 0)
    }
    return registry
}

var roleModel = null
var getRoleModel = async () => {
    if (roleModel == null) {
        roleModel = await RoleModel.new(0)
        await roleModel.assignWriter(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)
        await roleModel.assignReader(data.modifier, data.AFT.roleId, data.AFT.reader.roleId)
        await roleModel.assignWriter(data.modifier, data.QBT.roleId, data.QBT.writer.roleId)
        await roleModel.assignReader(data.modifier, data.QBT.roleId, data.QBT.reader.roleId)
    }
    return roleModel
}

var deployAll = async (orgRoleId, userId) => {
    let registry = await getRegistry()
    let roleModel = await getRoleModel()
    let container = await Container.new(0, registry.address, roleModel.address,
            data.modifier, orgRoleId, {from: userId})
    return [registry, container]
}

contract("Container", (accounts) => {
    it("creator should be the deployer and he should have the writer role", async () => {
            let [registry, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id)

            let orgIds = await container.getOrgIds.call()
            util.assertEq(orgIds, [data.AFT.orgId], "wrong orgs list")

            let canWrite = await registry.userCanWrite.call(data.AFT.writer.id, container.address, data.modifier)
            assert(canWrite, "creator cannot write")

            let canRead = await registry.userCanRead.call(data.AFT.writer.id, container.address, data.modifier)
            assert(canRead, "creator cannot read")

            canWrite = await registry.userCanWrite.call(data.unknown.userId, container.address, data.modifier)
            assert(!canWrite, "unknown user can write")

            canRead = await registry.userCanRead.call(data.unknown.userId, container.address, data.modifier)
            assert(!canRead, "unknown user can read")
        })
    it("should not be created for an unknown role or an unknown user", async () => {
            await util.runTillGasLimit(deployAll(data.unknown.roleId, data.AFT.writer.id)
            ).should.be.rejected
            await util.runTillGasLimit(deployAll(data.AFT.roleId, data.unknown.userId)
            ).should.be.rejected
        })
    it("should not add an unknown org or an unknown role", async () => {
            let [registry, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id)

            await util.runTillGasLimit(
                container.grantOrgRole(data.unknown.orgId, data.AFT.roleId, {from: data.AFT.writer.id})
                ).should.be.rejected
        })
    it("should add a known reader and a known writer", async () => {
            let [registry, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id)

            await container.grantOrgRole(data.QBT.orgId, data.QBT.roleId, {from: data.AFT.writer.id})

            let orgIds = await container.getOrgIds.call()
            util.assertEq(orgIds, [data.AFT.orgId, data.QBT.orgId],
                "wrong users list")

            let canRead = await registry.userCanRead.call(data.QBT.reader.id,
                container.address, data.modifier)
            assert(canRead, "reader cannot read")

            await container.grantOrgRole(data.QBT.orgId, data.AFT.roleId,
                {from: data.AFT.writer.id})

            let canWrite = await registry.userCanWrite.call(
                data.QBT.writer.id, container.address, data.modifier)
            assert(canWrite, "writer cannot write")

            canWrite = await registry.userCanWrite.call(
                data.unknown.userId, container.address, data.modifier)
            assert(!canWrite, "unknown user can write")

            canRead = await registry.userCanRead.call(data.unknown.userId,
                container.address, data.modifier)
            assert(!canRead, "unknown user can read")
        })
    it("should not add an archive by a reader", async () => {
            let [registry, container] =
                await deployAll(data.AFT.roleId, data.AFT.writer.id)

            await container.grantOrgRole(data.QBT.orgId, data.QBT.roleId,
                {from: data.AFT.writer.id})

            await util.runTillGasLimit(
                container.appendArchive(data.hmac(),
                    data.expiry, {from: data.QBT.reader.id})
                ).should.be.rejected
        })
    it("should add archives by writers", async () => {
            let [registry, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id)

            let hmac = data.hmac()
            await container.appendArchive(data.modifier, hmac, data.expiry, {from: data.AFT.writer.id})

            let count = await container.getArchivesCount.call()
            util.assertEq(count, 1, "wrong archives count")

            let author = await container.getArchiveAuthorId.call(0)
            util.assertEq(author, data.AFT.writer.id, "wrong archive author")

            let HMAC = await container.getArchiveHMAC.call(0)
            util.assertEq(HMAC, hmac, "wrong archive HMAC")

            let expiry = await container.getArchiveExpiry.call(0)
            util.assertEq(expiry, data.expiry, "wrong archive expiry")

            await container.grantOrgRole(data.QBT.orgId, data.AFT.roleId,
                {from: data.AFT.writer.id})

            hmac = data.hmac()
            await container.appendArchive(data.modifier, hmac, data.expiry,
                    {from: data.QBT.writer.id})

            count = await container.getArchivesCount.call()
            util.assertEq(count, 2, "wrong archives count")

            author = await container.getArchiveAuthorId.call(1)
            util.assertEq(author, data.QBT.writer.id, "wrong archive author")

            HMAC = await container.getArchiveHMAC.call(1)
            util.assertEq(HMAC, hmac, "wrong archive HMAC")

            expiry = await container.getArchiveExpiry.call(1)
            util.assertEq(expiry, data.expiry, "wrong archive expiry")
        })
    it("should add public archive to be read by " + data.any.orgRoleId + '-' + data.any.userRoleId, async () => {
            let [registry, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id)

            await roleModel.assignWriter(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)

            await container.appendArchive(data.modifier, data.hmac(), data.expiry, {from: data.AFT.writer.id})

            let count = await container.getArchivesCount.call()
            util.assertEq(count, 1, "wrong archives count")

            let canReadBefore = await registry.userCanRead.call(data.QBT.reader.id, container.address, data.modifier)
            assert(!canReadBefore, "reader can read but not public yet")

            await roleModel.assignReader(data.modifier, data.any.orgRoleId, data.any.userRoleId)

            let canReadInRoleModel = await roleModel.canRead.call(data.modifier, data.QBT.roleId, data.QBT.reader.roleId)
            assert(canReadInRoleModel, "roleModel.canRead must be true")

            let canReadInContainer = await container.userCanRead.call(data.QBT.reader.id, 0)
            assert(canReadInContainer, "reader can't read public archive from container")

            await roleModel.assignDenied(data.modifier, data.any.orgRoleId, data.any.userRoleId)
        })
    it ("should read public archive by " + data.any.userRoleId, async () => {
   
            let [registry, container] =
                await deployAll(data.AFT.roleId, data.AFT.writer.id)

            await roleModel.assignWriter(data.modifier, data.AFT.roleId, 
                data.AFT.writer.roleId)

            await container.appendArchive(data.modifier, data.hmac(),
                data.expiry, {from: data.AFT.writer.id})

            let count = await container.getArchivesCount.call()
            util.assertEq(count, 1, "wrong archives count")

            await roleModel.assignReader(data.modifier,
                data.QBT.roleId, data.any.userRoleId)

            let canReadInRoleModel = await roleModel.canRead.call(
                data.modifier, data.QBT.roleId, data.QBT.reader.roleId)
            assert(canReadInRoleModel, "roleModel.canRead must be true")

            await container.grantOrgRole(data.QBT.orgId, data.QBT.roleId)
            let orgRoleIdsCnt = await container.getOrgRoleIdsCount.call(data.QBT.orgId)
            util.assertEq(orgRoleIdsCnt, 1, 'Reader org must have 1 lole into container')

            let canReadInContainer = await container.userCanRead.call(data.QBT.reader.id, 0)
            assert(canReadInContainer, "reader can't read public archive from container")

            await roleModel.assignDenied(data.modifier, data.QBT.roleId, data.any.userRoleId)
    })
    it ("should read public archive by " + data.any.orgRoleId, async () => {
   
            let [registry, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id)

            await roleModel.assignWriter(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)

            await container.appendArchive(data.modifier, data.hmac(), data.expiry, {from: data.AFT.writer.id})

            let count = await container.getArchivesCount.call()
            util.assertEq(count, 1, "wrong archives count")

            await roleModel.assignReader(data.modifier, data.any.orgRoleId, data.QBT.reader.roleId)

            let canReadInRoleModel = await roleModel.canRead.call(data.modifier, data.QBT.roleId, data.QBT.reader.roleId)
            assert(canReadInRoleModel, "roleModel.canRead must be true")

            let orgRoleIdsCnt = await container.getOrgRoleIdsCount.call(data.QBT.orgId)
            util.assertEq(orgRoleIdsCnt, 0, 'Reader org must have 0 role into container')

            let canReadInContainer = await container.userCanRead.call(data.QBT.reader.id, 0)
            assert(canReadInContainer, "reader can't read public archive from container")

            await roleModel.assignDenied(data.modifier, data.any.orgRoleId, data.QBT.reader.roleId)
    })

    describe("[Test archive versions]: ", () => {

        let registry, container
        before(async () => {
            [registry, container] = await deployAll(data.AFT.roleId, data.AFT.writer.id)
            await roleModel.assignWriter(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)
            await container.appendArchive(data.modifier, data.hmac(), data.expiry, {from: data.AFT.writer.id})
        })

        it ("Must be one archive in container", async () => {
            let cnt = await container.getArchivesCount()
            assert.equal(1, cnt, "expect archive cnt to be 1 , get " + cnt)
        })

        it("Must add another archive", async () => {
            await container.appendArchive(data.modifier, data.hmac(), data.expiry, {from: data.AFT.writer.id})
            let cnt = await container.getArchivesCount()
            assert.equal(2, cnt, "expect archive cnt to be 2 , get " + cnt)
        })

        it ("Must append new version of new archive", async () => {
            await container.updateArchive(0, data.hmac(), data.expiry, {from: data.AFT.writer.id})
            let cnt = await container.getArchivesCount()
            assert.equal(3, cnt, "expect archive cnt to be 3 , get " + cnt.toString())
        })

        it("Newest must have next version equals it's id", async () => {

            let ver = await container.getArchiveNextVersion(2)
            assert.equal(2, ver, "Must link to 2, but have" + ver); //util.assertEq(2, ver)
        })

        it ("Newest must be latest", async () => {

            let isLatest = await container.isArchiveLatestVersion(2)
            assert.equal(true, isLatest, "Must be latest")
        })

        it ("Single archive must be latest", async () => {

            let isLatest = await container.isArchiveLatestVersion(1)
            assert.equal(true, isLatest, "Must be latest")
        })

        it("Newest must link to oldest", async () => {

            let prev = await container.getArchivePrevVersion(2)
            assert.equal(0, prev, "Must link to 0, but have" + prev)
        })

        it ("Oldest must be not latest", async () => {

            let isLatest = await container.isArchiveLatestVersion(0)
            assert.equal(false, isLatest, "Must be latest")
        })

        it("Oldest must link to newest", async () => {

            let next = await container.getArchiveNextVersion(0)
            assert.equal(2, next, "Must link to 2, but have" + next)
        })

        it("Must get latest version for very old archive", async () => {

            await container.updateArchive(2, data.hmac(), data.expiry, {from: data.AFT.writer.id})
            let latestId = await container.getArchiveLatestVersion(0)
            assert.equal(3, latestId)
        })

        it("Must return same id for single arhive when get latest", async () => {

            let latestId = await container.getArchiveLatestVersion(1)
            assert.equal(1, latestId)
        })

        it("Must throw when try update not latest archive", async () => {

            await util.runTillGasLimit(
                container.updateArchive(0, data.hmac(), data.expiry, {from: data.AFT.writer.id})
            ).should.be.rejected

        })
    })
})
