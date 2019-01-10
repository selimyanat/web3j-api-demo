package com.sy.web3j.api.demo.blockchain;

import static io.vavr.control.Try.run;
import static java.lang.String.format;

import com.sy.web3j.api.demo.listener.BlockListener;
import com.sy.web3j.api.demo.listener.TransactionListener;
import com.sy.web3j.api.demo.util.VisibleForTestOnly;
import io.reactivex.disposables.Disposable;
import io.vavr.control.Try;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

/**
 * Service that interacts with the blockchain.
 *
 * @author selim
 */
@Slf4j
public class BlockchainService {

  static final BigInteger GAS_PRICE = BigInteger.valueOf(200_000_0000L);

  static final BigInteger GAS_LIMIT = BigInteger.valueOf(672_1975L);

  private final Web3j web3j;

  @VisibleForTestOnly
  @Setter(AccessLevel.PACKAGE)
  private Disposable blockSubscription;

  @VisibleForTestOnly
  @Setter(AccessLevel.PACKAGE)
  private Disposable transactionSubscription;

  @VisibleForTestOnly
  @Getter(AccessLevel.PACKAGE)
  private final ConcurrentLinkedQueue<BlockListener> blockListeners;

  @VisibleForTestOnly
  @Getter(AccessLevel.PACKAGE)
  private final ConcurrentLinkedQueue<TransactionListener> transactionListeners;

  /**
   * Construct a new instance of <code>{@link BlockchainService}</code>.
   *
   * @param web3j The web3jOverHttp component to communicate with the blockchain.
   * @param blockListeners A list of block listeners.
   * @param transactionListeners A list of transaction listeners.
   */
  public BlockchainService(final Web3j web3j, final ConcurrentLinkedQueue<BlockListener> blockListeners,
      final ConcurrentLinkedQueue<TransactionListener> transactionListeners) {
    this.web3j = web3j;
    this.blockListeners = blockListeners;
    this.transactionListeners = transactionListeners;
  }

  /**
   * Subscribe to blockchain notifications on block and transaction.
   */
  @VisibleForTestOnly
  @PostConstruct
  void enableSubscriptions() {
    blockSubscription = web3j.blockFlowable(false)
        .map(
            block -> block.getBlock().getHash())
        .doOnSubscribe(
            subscription -> LOG.info("Subscribe to newly block created on the blockchain."))
        .subscribe(
            blockHash -> blockListeners.forEach(blockListener -> blockListener.onNewBlock(blockHash)),
            throwable -> LOG.error("Could not subscribe to block notifications:", throwable));

    transactionSubscription = web3j.transactionFlowable()
        .map(
            transaction -> transaction.getHash())
          .doOnSubscribe(
            subscription -> LOG.info("Subscribe to newly transactions confirmed on the blockchain."))
        .subscribe(
            transactionHash -> this.transactionListeners
                .forEach(listeners -> listeners.onTransactionConfirmed(transactionHash)),
            throwable -> LOG.error("Could not subscribe to transactions notifications:", throwable));
  }

  /**
   * Return the number of transaction sent from the given account.
   *
   * @param accountAddress the account address to check the number of transaction sent..
   * @return the number of transaction sent or 0 if the account is unknown.
   */
  public BigInteger getTransactionsCount(@NotEmpty final String accountAddress) {

    return Try.of(() -> web3j
        .ethGetTransactionCount(accountAddress, DefaultBlockParameterName.LATEST)
        .send())
        .map(ethGetTransactionCount -> ethGetTransactionCount.getTransactionCount())
        .getOrElseThrow(throwable -> new BlockchainException(
            format("Could not get transaction count for address %s", accountAddress), throwable));
  }

  /**
   * Return the balance of the given account in Ether.
   *
   * @param accountAddress the account address to check the balance.
   * @return The balance in Ether or 0 if the account is unknown.
   */
  public BigDecimal getBalance(@NotEmpty final String accountAddress) {
    return Try.of(() -> web3j
        .ethGetBalance(accountAddress, DefaultBlockParameterName.LATEST)
        .send())
        .map(ethGetBalance -> ethGetBalance.getBalance())
        .map(balance -> Convert.fromWei(balance.toString(), Unit.ETHER))
        .getOrElseThrow(throwable -> new BlockchainException(
            format("Could not get the balance of account %s", accountAddress), throwable));
  }

  /**
   * Returns a list of unlocked accounts. (aka owned by the ethereum client).
   *
   * @return the list of accounts owned by the ethereum client.
   */
  public Collection<String> getUnlockedAccounts() {

    return Try.of(() -> web3j
        .ethAccounts()
        .send())
        .map(ethAccounts -> ethAccounts.getAccounts())
        .getOrElseThrow(throwable -> new BlockchainException("Could not get accounts information",
            throwable));
  }

  /**
   * Send an amount of Ether from an unlocked account (aka the private key is stored on the
   * Ethereum client) address to another account.
   *
   * @param senderAddress the account that sends ether.
   * @param recipientAddress the account that receives ether.
   * @param amountInEther the amount to fundTransfer.
   */
  public void transferFund(@NotEmpty String senderAddress, @NotEmpty String recipientAddress,
      @Positive BigDecimal amountInEther) {
    var transaction = Transaction.createEtherTransaction(
        senderAddress,
        null,
        GAS_PRICE,
        GAS_LIMIT,
        recipientAddress,
        Convert.toWei(amountInEther, Unit.ETHER).toBigInteger());

    web3j.ethSendTransaction(transaction)
        .sendAsync()
        .thenAccept(ethSendTransaction -> Optional
            .ofNullable(ethSendTransaction.getTransactionHash())
            .orElseThrow(() -> new BlockchainException(ethSendTransaction.getError().getMessage())))
        .exceptionally(throwable -> {
          LOG.error("Could not complete Ether transfer to the blockchain:", throwable);
          return null;
        });
  }

  /**
   * Add a new block listener to the block notification listeners group.
   *
   * @param blockListener The block listener to add.
   */
  public void addBlockListener(@NotNull BlockListener blockListener) {
    blockListeners.add(blockListener);
  }

  /**
   * Remove a block listener from the block notification listeners group.
   *
   * @param blockListener The block listener to remove.
   */
  public void removeBlockListener(@NotNull BlockListener blockListener) {
    blockListeners.remove(blockListener);
  }

  /**
   * Add a new transaction listener to the transaction notification listeners group.
   *
   * @param transactionListener The transaction listener to add.
   */
  public void addTransactionListener(@NotNull TransactionListener transactionListener) {
    transactionListeners.add(transactionListener);
  }

  /**
   * Remove a transaction listener from the transaction notification listeners group.
   *
   * @param transactionListener The transaction listener to remove.
   */
  public void removeTransactionListener(@NotNull TransactionListener transactionListener) {
    transactionListeners.remove(transactionListener);
  }

  /**
   * Graceful shutdown by removing all block and transaction listeners along with the blockchain
   * subscription.
   */
  @VisibleForTestOnly
  @PreDestroy
  void shutdown() {
    blockListeners.clear();
    transactionListeners.clear();
    LOG.info("Block and transaction listeners have been successfully unregistered.");

    run(() -> blockSubscription.dispose())
        .onSuccess(aVoid -> LOG.info("Successfully unsubscribed to block notifications."))
        .onFailure(throwable -> LOG.warn("Fail to cancel subscription to block notifications: {}",
            throwable.getMessage()));
    run(() -> transactionSubscription.dispose())
        .onSuccess(aVoid -> LOG.info("Successfully unsubscribed to transaction notifications."))
        .onFailure(throwable -> LOG.warn("Fail to cancel subscription to transaction notifications: {}",
            throwable.getMessage()));
  }

}
