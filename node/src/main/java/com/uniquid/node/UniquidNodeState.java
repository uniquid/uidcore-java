package com.uniquid.node;

/**
 * Represents the internal state of an Uniquid Node 
 */
public enum UniquidNodeState {

    /**
     * Represents a node just created. The node contains the ID-based cryptography but it is not yet initialized.
     */
    CREATED,

    /**
     * Represents a node initialized and waiting for the Imprinting transaction.
     */
    IMPRINTING,

    /**
     * Represents a node ready to use.
     */
    READY

}
