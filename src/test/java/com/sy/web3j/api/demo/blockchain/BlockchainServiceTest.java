package com.sy.web3j.api.demo.blockchain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.web3j.utils.Numeric.encodeQuantity;

import com.sy.web3j.api.demo.listener.BlockListener;
import com.sy.web3j.api.demo.listener.TransactionListener;
import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.request.TransactionAssert;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

/**
 * Test class for {@link BlockchainService}.
 *
 * @author selim
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
public class BlockchainServiceTest {

  private static final String DEFAULT_ACCOUNT_ADDRESS = "0x5dd4232f1AF576F239D69f77F61Dc08d9Fda4CA2";

  private static final String ONE_ETHER = "1";

  private static final Long ONE_ETHER_TO_WEI = 100_000_000_000_000_0000L;

  @Mock
  private Web3j web3j;

  private BlockchainService underTest;

  @BeforeEach
  void setUp() {
    underTest = new BlockchainService(web3j, new ConcurrentLinkedQueue<>(),
        new ConcurrentLinkedQueue<>());
  }

  @Test
  void addBlockListener_listenerIsAdded() {
    final BlockListener listener = blockHash -> LOG.info("Am a block listener.");

    underTest.addBlockListener(listener);
    org.assertj.core.api.Assertions.assertThat(underTest.getBlockListeners())
        .contains(listener)
        .hasSize(1)
        .withFailMessage("There should be only one block listener.");
  }

  @Test
  void addTransactionListener_listenerIsAdded() {
    final TransactionListener listener = transactionHash ->
        LOG.info("Am a transaction listener.");

    underTest.addTransactionListener(listener);
    org.assertj.core.api.Assertions.assertThat(underTest.getTransactionListeners())
        .contains(listener)
        .hasSize(1)
        .withFailMessage("There should be only one transaction listener.");
  }

  @Test
  void removeBlockListener_listenerIsRemoved() {
    final BlockListener listener = blockHash -> LOG.info("Am a block listener.");
    underTest.addBlockListener(listener);

    underTest.removeBlockListener(listener);
    org.assertj.core.api.Assertions.assertThat(underTest.getBlockListeners())
        .withFailMessage("There should not be any block listeners.")
        .isEmpty();
  }

  @Test
  void removeTransactionListener_listenerIsRemoved() {
    final TransactionListener listener = transactionHash ->
        LOG.info("Am a transaction listener.");
    underTest.addTransactionListener(listener);

    underTest.removeTransactionListener(listener);
    org.assertj.core.api.Assertions.assertThat(underTest.getTransactionListeners())
        .withFailMessage("There should not be any transaction listeners.")
        .isEmpty();
  }

  @Test
  void enableSubscriptions_allListeners_areCalled() {
    var nbOfBlocks = new AtomicInteger(0);
    final BlockListener blockListener = blockHash -> nbOfBlocks.incrementAndGet();
    var nbOfTransactions = new AtomicInteger(0);
    final TransactionListener transactionListener = transactionHash ->
        nbOfTransactions.incrementAndGet();
    when(web3j.blockFlowable(false)).thenReturn(BlockSubscriptionFactory.monoBlock());
    underTest.addBlockListener(blockListener);
    when(web3j.transactionFlowable()).thenReturn(TransactionSubscriptionFactory.monoTransaction());
    underTest.addTransactionListener(transactionListener);

    underTest.enableSubscriptions();
    assertThat(nbOfBlocks.get())
        .withFailMessage("There should be one block.")
        .isEqualTo(1);
    assertThat(nbOfTransactions.get())
        .withFailMessage("There should be one transaction.")
        .isEqualTo(1);
  }

  @Test
  @SneakyThrows
  void getTransactionsCount_returnsTransactionCount() {
    var transactionCountRequest = mock(Request.class);
    var transactionCountResponse = mock(EthGetTransactionCount.class);
    // Compiler cannot tell what type is the first parameter (wildcard) before runtime and cannot
    // safe cast from Request<?, EthGetTransactionCount>.
    // The workaround is to use doReturn that is not safe but designed for these cases.
    doReturn(transactionCountRequest)
        .when(web3j)
        .ethGetTransactionCount(DEFAULT_ACCOUNT_ADDRESS, DefaultBlockParameterName.LATEST);
    when(transactionCountRequest.send()).thenReturn(transactionCountResponse);
    when(transactionCountResponse.getTransactionCount()).thenReturn(BigInteger.TEN);

    assertThat(underTest.getTransactionsCount(DEFAULT_ACCOUNT_ADDRESS))
        .isEqualTo(BigInteger.TEN)
        .withFailMessage("There should be ten transactions.");
  }

  @Test
  @SneakyThrows
  void getTransactionsCount_onFailure_throwBlockchainException() {
    var transactionCountRequest = mock(Request.class);
    // Compiler cannot tell what type is the first parameter (wildcard) before runtime and cannot
    // safe cast from Request<?, EthGetTransactionCount>.
    // The workaround is to use doReturn that is not safe but designed for these cases.
    doReturn(transactionCountRequest)
        .when(web3j)
        .ethGetTransactionCount(DEFAULT_ACCOUNT_ADDRESS, DefaultBlockParameterName.LATEST);
    when(transactionCountRequest.send()).thenThrow(IOException.class);

    assertThatThrownBy(() -> underTest.getTransactionsCount(DEFAULT_ACCOUNT_ADDRESS))
        .isInstanceOf(BlockchainException.class)
        .hasMessage("Could not get transaction count for address %s", DEFAULT_ACCOUNT_ADDRESS);

  }

  @Test
  @SneakyThrows
  void getBalance_returnsBalanceInEther() {
    var balanceRequest = mock(Request.class);
    var balanceResponse = mock(EthGetBalance.class);
    // Usage of doReturn is explained in the test above.
    doReturn(balanceRequest)
        .when(web3j)
        .ethGetBalance(DEFAULT_ACCOUNT_ADDRESS, DefaultBlockParameterName.LATEST);
    when(balanceRequest.send()).thenReturn(balanceResponse);
    when(balanceResponse.getBalance()).thenReturn(BigInteger.valueOf(ONE_ETHER_TO_WEI));

    assertThat(underTest.getBalance(DEFAULT_ACCOUNT_ADDRESS))
        .isEqualTo(BigDecimal.valueOf(1))
        .withFailMessage("The balance in wei should be converted in Ether and equal to 0.1");
  }

  @Test
  @SneakyThrows
  void getBalance_onFailure_throwBlockchainException() {
    var balanceRequest = mock(Request.class);
    // Usage of doReturn is explained in the test above.
    doReturn(balanceRequest)
        .when(web3j)
        .ethGetBalance(DEFAULT_ACCOUNT_ADDRESS, DefaultBlockParameterName.LATEST);
    when(balanceRequest.send()).thenThrow(IOException.class);

    assertThatThrownBy(() -> underTest.getBalance(DEFAULT_ACCOUNT_ADDRESS))
        .isInstanceOf(BlockchainException.class)
        .hasMessage("Could not get the balance of account %s", DEFAULT_ACCOUNT_ADDRESS);
  }

  @Test
  void transferFund_isAsynchronous() {
    var transactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
    var transactionRequest = mock(Request.class);
    // Usage of doReturn is explained in the test above.
    doReturn(transactionRequest)
        .when(web3j)
        .ethSendTransaction(any(Transaction.class));
    when(transactionRequest.sendAsync()).thenReturn(new CompletableFuture<>());

    underTest.transferFund(DEFAULT_ACCOUNT_ADDRESS,
        "0xEFF48dBF9b40Dd5bA47Ff52841D359FC1e749491",
        BigDecimal.valueOf(Long.valueOf(ONE_ETHER)));
    verify(web3j).ethSendTransaction(transactionArgumentCaptor.capture());
    TransactionAssert.assertThat(transactionArgumentCaptor.getValue())
        .isNotNull()
        .withFailMessage("The transaction sent should not be null.")
        .hasFrom(DEFAULT_ACCOUNT_ADDRESS)
        .hasTo("0xEFF48dBF9b40Dd5bA47Ff52841D359FC1e749491")
        .hasGasPrice(encodeQuantity(BlockchainService.GAS_PRICE))
        .hasGas(encodeQuantity(BlockchainService.GAS_LIMIT))
        .hasValue(encodeQuantity(BigInteger.valueOf(ONE_ETHER_TO_WEI)));
  }

  @Test
  @SneakyThrows
  void getUnlockedAccounts_returnsAllAccounts() {
    var accountRequest = mock(Request.class);
    var ethAccounts = mock(EthAccounts.class);
    // Usage of doReturn is explained in the test above.
    doReturn(accountRequest)
        .when(web3j)
        .ethAccounts();
    when(accountRequest.send()).thenReturn(ethAccounts);
    when(ethAccounts.getAccounts()).thenReturn(List.of(DEFAULT_ACCOUNT_ADDRESS));

    assertThat(underTest.getUnlockedAccounts())
        .isEqualTo(List.of(DEFAULT_ACCOUNT_ADDRESS))
        .withFailMessage("The account should contain one account %s", DEFAULT_ACCOUNT_ADDRESS);
  }

  @Test
  @SneakyThrows
  void getUnlockedAccounts_onFailure_throwBlockchainException() {
    var accountRequest = mock(Request.class);
    // Usage of doReturn is explained in the test above.
    doReturn(accountRequest)
        .when(web3j)
        .ethAccounts();
    when(accountRequest.send()).thenThrow(IOException.class);

    assertThatThrownBy(() -> underTest.getUnlockedAccounts())
        .isInstanceOf(BlockchainException.class)
        .hasMessage("Could not get accounts information", DEFAULT_ACCOUNT_ADDRESS);
  }

  @Test
  void shutdown_removeListeners_and_removeBlockchainSubscriptions() {

    var blockSubscription = mock(Disposable.class);
    underTest.setBlockSubscription(blockSubscription);
    var transactionSubscription = mock(Disposable.class);
    underTest.setTransactionSubscription(transactionSubscription);
    underTest.addBlockListener(blockHash -> LOG.info("Am a block listener"));
    underTest.addTransactionListener(transactionHash -> LOG.info("Am a transaction listener"));

    underTest.shutdown();

    // @formatter:off
    assertAll(
        () ->
            org.assertj.core.api.Assertions.assertThat(underTest.getBlockListeners())
                .withFailMessage("There should not be any block listeners")
                .isEmpty(),
        () ->
            org.assertj.core.api.Assertions.assertThat(underTest.getTransactionListeners())
                .withFailMessage("There should not be any transaction listeners")
                .isEmpty());
    // @formatter:on

    verify(blockSubscription).dispose();
    verify(transactionSubscription).dispose();
  }
}
