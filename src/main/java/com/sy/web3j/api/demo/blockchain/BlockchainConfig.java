package com.sy.web3j.api.demo.blockchain;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

/**
 * Configuration for blockchain components.
 *
 * @author selim
 */
@Configuration
@Slf4j
@SuppressWarnings("unused")
public class BlockchainConfig {

  /**
   * Construct a new instance of {@link Web3j}.
   * @param web3Url The blockchain client address.
   * @return A new instance of {@link Web3j}.
   */
  @Profile("!rpc-ws")
  @Bean(destroyMethod = "shutdown")
  @SuppressWarnings("unused")
  @SneakyThrows
  Web3j web3jOverHttp(final @Value("${web3j.http.client-address}") String web3Url) {
    return Web3j.build(new HttpService(web3Url));
  }

  @Profile("rpc-ws")
  @Bean(destroyMethod = "shutdown")
  @SneakyThrows
  Web3j web3jOverWebSocket(final @Value("${web3j.ws.client-address}") String web3Url) {
    var webSocketClient =  new WebSocketClient(URI.create(web3Url));
    var webSocketService = new WebSocketService(webSocketClient, true);
    webSocketService.connect();
    return Web3j.build(webSocketService);
  }

  /**
   * Construct a new instance of {@link BlockchainService}.
   * @param web3j The blockchain service.
   * @return A new instance of {@link BlockchainService}.
   */
  @Bean
  BlockchainService blockchainService(final Web3j web3j) {
    return new BlockchainService(web3j, new ConcurrentLinkedQueue<>(),
        new ConcurrentLinkedQueue<>());
  }
}
