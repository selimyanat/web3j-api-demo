package com.sy.web3j.api.demo.listener;

import static org.mockito.Mockito.verify;

import com.sy.web3j.api.demo.blockchain.BlockchainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link BlockchainEventConsumer}.
 *
 * @author selim
 */
@ExtendWith(MockitoExtension.class)
class BlockchainEventConsumerTest {

  @Mock
  private BlockchainService blockchainService;

  @InjectMocks
  private BlockchainEventConsumer underTest;

  @Test
  void enableListeners_block_and_transaction_listeners_added() {

    underTest.enableListeners();
    verify(blockchainService).addBlockListener(underTest);
    verify(blockchainService).addTransactionListener(underTest);
  }

  @Test
  void shutdown_block_and_transaction_listeners_removed() {

    underTest.shutdown();
    verify(blockchainService).removeBlockListener(underTest);
    verify(blockchainService).removeTransactionListener(underTest);
  }
}