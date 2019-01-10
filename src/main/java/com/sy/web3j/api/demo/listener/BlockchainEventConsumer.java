package com.sy.web3j.api.demo.listener;

import com.sy.web3j.api.demo.blockchain.BlockchainService;
import com.sy.web3j.api.demo.util.VisibleForTestOnly;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consume events produced by the blockchain.
 *
 * @author selim
 */
@Slf4j
@AllArgsConstructor
public class BlockchainEventConsumer implements BlockListener, TransactionListener {

  private final BlockchainService blockchainService;

  @VisibleForTestOnly
  @PostConstruct
  void enableListeners() {
    this.blockchainService.addBlockListener(this);
    this.blockchainService.addTransactionListener(this);
  }

  /**
   * Unregister all listeners from the blockchain client.
   */
  @VisibleForTestOnly
  @PreDestroy
  void shutdown() {
    blockchainService.removeBlockListener(this);
    blockchainService.removeTransactionListener(this);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void onNewBlock(String blockHash) {
    LOG.info("A new block with hash {} has been mined", blockHash);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void onTransactionConfirmed(String transactionHash) {
    LOG.info("A new transaction with hash {} has been confirmed", transactionHash);
  }
}
