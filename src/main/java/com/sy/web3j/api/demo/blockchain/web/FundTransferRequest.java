package com.sy.web3j.api.demo.blockchain.web;

import java.math.BigDecimal;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the information required to carry out a fund transfer.
 *
 * @author selim.
 */
@Data
public class FundTransferRequest {

  @NotEmpty(message = "Sender address cannot be null or empty")
  private final String fromAccountAddress;
  @NotEmpty(message = "Recipient address cannot be null or empty")
  private final String toAccountAddress;
  @Positive
  private final BigDecimal amountInEther;

}
