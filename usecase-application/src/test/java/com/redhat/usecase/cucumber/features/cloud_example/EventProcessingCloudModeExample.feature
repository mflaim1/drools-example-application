Feature: Testing financial advisor fraud detection cloud
#make sure timestamp attribute in TradeEvent is uncommented
#make sure expiration attribute in TradeEvent is commented
Scenario: Testing a financial advisor making too many trades on the same account cloud mode
Given Trade Events:
|Id|Investor Id|Advisor Id| Price |Quantity|Symbol|Type    |  Timestamp           |
|1 | 1         |  1       | 20.00 | 10     | ANS  | Buy    |  27-09-1991 20:28:10 |
|2 | 1         |  1       | 20.00 | 11     | ANS  | Buy    |  27-09-1991 20:28:20 |
When I run the cloud mode example
Then I expect the following Reports to be created:
|Code                            																					| Trades    |
|Example project code, reason is too many trades by one financial advisor	| 1,2       |