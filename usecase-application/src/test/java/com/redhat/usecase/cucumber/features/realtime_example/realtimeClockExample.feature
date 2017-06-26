Feature: Test Feature to demonstrate realtime clock usage

Scenario: Multiple trades in last 5 minutes with same FA
Given Trade Events:
|Id|Investor Id|Advisor Id| Price |Quantity|Symbol|Type    |
|1 | 1         |  3       | 20.00 | 10     | ANS  | Buy    |  
|2 | 1         |  5       | 20.00 | 11     | ABS  | Sell   |  
|3 | 2         |  2       | 20.00 | 15     | HNG  | Sell   | 
|4 | 3         |  1       | 20.00 | 20     | TYI  | Buy    |
|5 | 3         |  1       | 20.00 | 20     | JKJ  | Buy    |
|6 | 3         |  1       | 20.00 | 20     | ANS  | Buy    | 
When I run the realtime clock example
Then I expect the following Reports to be created:
|Code                                 | Trades           |
|Excessive Trading with one FA        | 4,5,6            |




