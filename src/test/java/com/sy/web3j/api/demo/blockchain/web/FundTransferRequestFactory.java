package com.sy.web3j.api.demo.blockchain.web;


import java.math.BigDecimal;

/**
 * Return prepared instance {@link FundTransferRequest} flowable.
 *
 * @author selim
 */
final class FundTransferRequestFactory {

  private FundTransferRequestFactory() {}

  public static FundTransferRequest sendTenEther() {
    return new FundTransferRequest("0x64ba2c532c23c0a935834d16020f5c1e026aaa00",
        "0x5dd4232f1AF576F239D69f77F61Dc08d9Fda4CA2",
        BigDecimal.TEN);
  }
}
