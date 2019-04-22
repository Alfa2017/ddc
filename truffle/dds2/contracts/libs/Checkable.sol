pragma solidity ^0.4.21;

library Checkable {
    struct bytes32Data {
        bool iExist;
        bytes32 data;
    }

    struct bytesData {
        bool iExist;
        bytes data;
    }
}
