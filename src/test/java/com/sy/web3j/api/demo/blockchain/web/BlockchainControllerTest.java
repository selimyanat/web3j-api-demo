package com.sy.web3j.api.demo.blockchain.web;

import static com.sy.web3j.api.demo.blockchain.web.FundTransferRequestFactory.sendTenEther;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sy.web3j.api.demo.blockchain.BlockchainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(BlockchainController.class)
class BlockchainControllerTest {

  static final String TEST_ACCOUNT = "0x64ba2c532c23c0a935834d16020f5c1e026aaa00";

  @Autowired
  MockMvc mockMvc;

  @MockBean
  BlockchainService blockchainService;

  @Test
  @SneakyThrows
  void getTransactionsCount() {
    when(blockchainService
        .getTransactionsCount(TEST_ACCOUNT))
        .thenReturn(BigInteger.TEN);

    mockMvc
        .perform(get("/transactions/{account}/count", TEST_ACCOUNT))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("10"));
  }

  @Test
  @SneakyThrows
  void getAccountBalance() {
    when(blockchainService
        .getBalance(TEST_ACCOUNT))
        .thenReturn(BigDecimal.TEN);

    mockMvc
        .perform(get("/accounts/{account}/balance", TEST_ACCOUNT))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("10"));
  }

  @Test
  @SneakyThrows
  void getAccounts() {
    when(blockchainService
        .getUnlockedAccounts())
        .thenReturn(List.of(TEST_ACCOUNT));

    mockMvc
        .perform(get("/accounts"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("[\""+TEST_ACCOUNT+"\"]"));
  }

  @Test
  @SneakyThrows
  void fundTransfer() {
    var fundTransfer = sendTenEther();
    doNothing()
        .when(blockchainService)
        .transferFund(fundTransfer.getFromAccountAddress(),
            fundTransfer.getToAccountAddress(),
            fundTransfer.getAmountInEther());

    mockMvc.perform(post("/transactions")
        .contentType("application/json")
        .content(new ObjectMapper().writeValueAsString(fundTransfer)))
        .andDo(print())
        .andExpect(status().isAccepted());
  }
}