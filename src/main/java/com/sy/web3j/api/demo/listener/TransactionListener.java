package com.sy.web3j.api.demo.listener;

/**
 * Interface for listener to confirmed transaction events.
 *
 * @author selim
 */
public interface TransactionListener {

  /**
   * Called by the blockchain client when a transaction is confirmed.
   * @param transactionHash The hash of the transaction confirmed.
   */
  void onTransactionConfirmed(String transactionHash);

}
