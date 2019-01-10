package com.sy.web3j.api.demo.blockchain.web;

import com.sy.web3j.api.demo.blockchain.BlockchainService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to interact with the blockchain.
 *
 * @author selim
 */
@RestController
@Slf4j
@AllArgsConstructor
@SuppressWarnings("unused")
public class BlockchainController {

  private final BlockchainService blockchainService;

  /**
   * Return the number of transaction sent from the given account.
   *
   * @param accountAddress the account address to check the number of transaction sent..
   * @return the number of transaction sent.
   */
  @GetMapping("/transactions/{accountAddress}/count")
  @SuppressWarnings("unused")
  public BigInteger getTransactionsCount(@PathVariable @NotEmpty final String accountAddress) {
    return blockchainService.getTransactionsCount(accountAddress);
  }

  /**
   * Return the balance of the given account in Ether.
   *
   * @param accountAddress the account address to check the balance.
   * @return The balance in Ether.
   */
  @GetMapping("/accounts/{accountAddress}/balance")
  @SuppressWarnings("unused")
  public BigDecimal getAccountBalance(@PathVariable @NotEmpty final String accountAddress) {
    return blockchainService.getBalance(accountAddress);
  }

  /**
   * Returns a list of unlocked accounts.
   *
   * @return the list of accounts owned by the ethereum client.
   */
  @GetMapping("/accounts")
  @SuppressWarnings("unused")
  public Collection<String> getAccounts() {
    return blockchainService.getUnlockedAccounts();
  }

  /**
   * Send an amount of Ether from an unlocked account address to another account.
   * @param fundTransferRequest the fund transfer request.
   */
  @PostMapping(value = "/transactions")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @SuppressWarnings("unused")
  public void fundTransfer(@Valid @RequestBody final FundTransferRequest fundTransferRequest) {
    blockchainService.transferFund(fundTransferRequest.getFromAccountAddress(),
        fundTransferRequest.getToAccountAddress(),
        fundTransferRequest.getAmountInEther());
  }
}
