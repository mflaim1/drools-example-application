Feature: Testing explicit expiry

Scenario: Testing an event with a defined expiration time
Given Trade Events:
|Id|Investor Id|Advisor Id| Price |Quantity|Symbol|Type    |  Timestamp           |
|1 | 1         |  1       | 20.00 | 10     | ANS  | Buy    |  27-09-1991 20:28:10 |
When I run the inferred expiration example
Then I expect the following Reports to be created:
|Code                  								| Trades    |
|Example project code, testing expiry	| 1         |
Then I expect no Trades to exist:
|Event		| Trades	|