require('chai').use(require("chai-as-promised"))
require('chai/register-should')

var RoleModel = artifacts.require("RoleModel")

var data = require("./data.js")(web3)
var util = require("./util.js")(web3)

contract("RoleModel", (accounts) => {
    it("creator should be upgradable",
        async () => {
            let one = await RoleModel.new(0, {from: data.AFT.admin.id});
            let two = await RoleModel.new(one.address, {from: data.AFT.admin.id});

            let disabled = await one.disabled.call();
            assert(disabled, "precursor enabled");

            disabled = await two.disabled.call();
            assert(!disabled, "successor disabled");

            let precursor = await two.precursor.call();
            util.assertEq(precursor, one.address, "wrong two precursor");

            precursor = await one.precursor.call();
            util.assertEq(precursor, 0, "wrong one precursor");

            let successor = await one.successor.call();
            util.assertEq(successor, two.address, "wrong one successor");

            successor = await two.successor.call();
            util.assertEq(successor, 0, "wrong two successor");
        })
    it("creator should be the admin",
        async () => {
            let roleModel = await RoleModel.new(0, {from: data.AFT.admin.id});

            let admin = await roleModel.admin.call();
            util.assertEq(admin, data.AFT.admin.id, "wrong admin");
        })
    it("creator should add a writer",
        async () => {
            let roleModel = await RoleModel.new(0)

            await roleModel.assignWriter(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)
            let canWrite = await roleModel.canWrite.call(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)
            assert(canWrite, "writer cannot write");

            let canRead = await roleModel.canRead.call(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)
            assert(canRead, "writer cannot read");
        })
    it("creator should add a reader",
        async () => {
            let roleModel = await RoleModel.new(0)

            await roleModel.assignReader(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)
            let canRead = await roleModel.canRead.call(data.modifier, data.AFT.roleId, data.AFT.writer.roleId)
            assert(canRead, "reader cannot read");
        })
})
