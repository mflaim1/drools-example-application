Feature: Testing financial advisor fraud detection stream
#make sure timestamp attribute in TradeEvent is uncommented
#make sure expiration attribute in TradeEvent is commented
Scenario: Testing a financial advisor making too many trades on the same account stream mode
Given Trade Events:
|Id|Investor Id|Advisor Id| Price |Quantity|Symbol|Type    |  
|1 | 1         |  1       | 20.00 | 10     | ANS  | Buy    |  
|2 | 1         |  1       | 20.00 | 11     | ANS  | Buy    | 
When I run the stream mode example
Then I expect the following Reports to be created:
|Code                            																					| Trades   |
|Example project code, reason is too many trades by one financial advisor	| 1,2      |