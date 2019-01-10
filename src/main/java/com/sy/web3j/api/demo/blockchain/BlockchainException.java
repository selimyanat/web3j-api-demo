package com.sy.web3j.api.demo.blockchain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the Ethereum blockchain client reject a request.
 *
 * @author selim
 */
@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Could not execute the request on the blockchain")
public class BlockchainException extends RuntimeException {

  /**
   * Creates a new instance of <code>BlockchainException</code> with a detail message.
   *
   * @param message the detailed message
   */
  public BlockchainException(String message) {
    super(message);
  }

  /**
   * Creates a new instance of <code>BlockchainException</code> with a detail message and cause.
   *
   * @param message the detailed message
   */
  public BlockchainException(String message, Throwable cause) {
    super(message, cause);
  }
}
