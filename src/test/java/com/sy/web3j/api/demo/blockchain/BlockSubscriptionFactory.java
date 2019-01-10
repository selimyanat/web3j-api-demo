package com.sy.web3j.api.demo.blockchain;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

/**
 * Return prepared instance {@link EthBlock} flowable.
 *
 * @author selim
 */
final class BlockSubscriptionFactory {

  private BlockSubscriptionFactory() {
  }

  /**
   * Return a subscription that signals a single block.
   *
   * @return a new flowable of one block.
   */
  public static Flowable<EthBlock> monoBlock() {
    return Flowable.create(
        emitter -> {
          var ethBlock = new EthBlock();
          var block = new Block();
          block.setHash(RandomString.make());
          ethBlock.setResult(block);
          emitter.onNext(ethBlock);
        },
        BackpressureStrategy.BUFFER);
  }
}
