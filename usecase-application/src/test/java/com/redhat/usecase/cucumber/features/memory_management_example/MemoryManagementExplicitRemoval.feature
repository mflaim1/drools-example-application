Feature: Testing explicit removal
#make sure timestamp attribute in TradeEvent is commented
#make sure expiration attribute in TradeEvent is commented

Scenario: Testing an event being removed explicitly
Given Trade Events:
|Id|Investor Id|Advisor Id| Price |Quantity|Symbol|Type    | 
|1 | 1         |  1       | 20.00 | 10     | ANS  | Buy    |  
When I run the explicit removal example
Then I expect the following Reports to be created:
|Code                  													| Trades    |
|Example project code, testing explicit removal	| 1         |
Then I expect no Trades to exist:
|Event		| Trades	|