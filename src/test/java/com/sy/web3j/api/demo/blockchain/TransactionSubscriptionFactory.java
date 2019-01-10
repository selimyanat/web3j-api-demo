package com.sy.web3j.api.demo.blockchain;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.web3j.protocol.core.methods.response.Transaction;

/**
 * Return prepared instance {@link Transaction} flowable.
 *
 * @author selim
 */
abstract class TransactionSubscriptionFactory {


  /**
   * Returns a subscription that signals a single transaction.
   *
   * @return a new flowable of one transaction.
   */
  public static Flowable<Transaction> monoTransaction() {
    return Flowable.create(
        emitter -> {
          var transaction = new Transaction();
          transaction.setHash(RandomString.make());
          emitter.onNext(transaction);
        },
        BackpressureStrategy.BUFFER);
  }
}
