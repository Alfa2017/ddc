let hexes = '0123456789abcdef'

function randHex() {
    return hexes[Math.floor(Math.random() * 16)]
}

function randomBytes(len) {
    let res = ''
    for (let i = 0; i < len; i++)
        res += randHex() + randHex()
    return '0x' + res
}

module.exports = (web3) => ({
    AFT: {
        orgId: web3.toHex("OGRN:1177700002150"),
        nodeId: web3.toHex("mstor.fintechru.org"),
        roleId: "AFTMstorTestOrgOne",
        admin: { id: web3.eth.accounts[0] },
        signer: { id: web3.eth.accounts[1] },
        writer: {
            id: web3.eth.accounts[2],
            roleId: "AFTMstorTestWriter"
        },
        reader: {
            id: web3.eth.accounts[3],
            roleId: "AFTMstorTestReader"
        }
    }, 
    QBT: {
        orgId: web3.toHex("OGRN:5167746497606"),
        nodeId: web3.toHex("mstor.qdlt.io"),
        roleId: "AFTMstorTestOrgTwo",
        admin: { id: web3.eth.accounts[4] },
        signer: { id: web3.eth.accounts[5] },
        writer: {
            id: web3.eth.accounts[6],
            roleId: "AFTMstorTestWriter"
        },
        reader: {
            id: web3.eth.accounts[7],
            roleId: "AFTMstorTestReader"
        }
    },
    any: {
        orgRoleId: "AFTMstorAnyOrgRole",
        userRoleId: "AFTMstorAnyUserRole"
    },
    unknown: {
        userId: web3.eth.accounts[8],
        orgId: "FEIN:77-0493581",
        roleId: "AFTMstorTestUnknownRole"
    },
    modifier: "AFTMstorTestModifier",
    cert: () => randomBytes(500),
    hmac: () => randomBytes(32),
    expiry: Date.now() + 3600
})
