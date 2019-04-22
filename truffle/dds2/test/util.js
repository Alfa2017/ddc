module.exports = (web3) => ({

    gasLimit: web3.eth.getBlock("latest").gasLimit,

    runTillGasLimit: (promise) =>
        promise.then(function (tx) {
                return tx.receipt.gasUsed ===
                            web3.eth.getBlock("latest").gasLimit
                    ? Promise.reject("failed due to gas limit")
                    : Promise.resolve(tx)
            }),

    assertEq: function (result, expected, error) {
        if (typeof expected == 'string')
            assert.equal(result.slice(0, expected.length), expected, error)
        else if (typeof expected == 'object') {
            for (var f in expected)
                this.assertEq(result[f], expected[f], error)
        }
        else
            assert.equal(result, expected, error)
    },

    sign: async (account, msg, registry) => {
        msg = await registry.gostHash256.call(web3.toHex(msg))
        var sig = web3.eth.sign(account, msg)
        var v = sig.slice(130, 132)
        var res = {
            r: sig.slice(0, 66),
            s: '0x' + sig.slice(66, 130),
            v: '0x' + ((v[0] == '1') ? v : (v[1] == '1') ? '1c' : '1b')
        }
        if (web3.version.network > 16777216 && // > 2 ^ 24 && < 2 ^ 32
            web3.version.network < 4294967296) // Masterchain network id
            res.hash = await registry.gostHashForEcrecover.call(msg)
        else {
            var prefix = web3.toHex('\x19Ethereum Signed Message:\n32')
            res.hash = web3.sha3(prefix + msg.slice(2), {encoding:'hex'})
        }
        return res
    }
})
