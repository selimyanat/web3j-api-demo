package com.sy.web3j.api.demo.listener;

/**
 * Interface for listener to new block events.
 *
 * @author selim
 */
public interface BlockListener {

  /**
   * Called by the blockchain client when a new block is mined.
   * @param blockHash The block hash.
   */
  void onNewBlock(String blockHash);

}
