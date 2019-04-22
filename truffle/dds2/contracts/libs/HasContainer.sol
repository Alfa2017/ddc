pragma solidity ^0.4.21;

import "../storage/Container.sol";


contract HasContainer {

    Container public container;

    function HasContainer(Container _container) public {
        container = _container;
    }
}
