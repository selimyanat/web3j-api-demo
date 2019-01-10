package com.sy.web3j.api.demo.listener;

import com.sy.web3j.api.demo.blockchain.BlockchainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the component that listens to blockchain events.
 *
 * @author selim
 */
@Configuration
@SuppressWarnings("unused")
public class ListenerConfig {

  /**
   * Construct a new instance of {@link BlockchainEventConsumer}.
   * @param blockchainService The blockchain service.
   * @return A new instance of {@link BlockchainEventConsumer}.
   */
  @Bean
  BlockchainEventConsumer blockChainEventConsumer(BlockchainService blockchainService) {
    return new BlockchainEventConsumer(blockchainService);
  }
}
