Feature: Test Feature to demonstrate pseudo clock usage

Scenario: Multiple trades in last 5 minutes with same FA
Given Trade Events:
|Id|Investor Id|Advisor Id| Price |Quantity|Symbol|Type    |
|4 | 3         |  1       | 20.00 | 20     | TYI  | Buy    |
|5 | 2         |  1       | 20.00 | 20     | JKJ  | Buy    |
|6 | 3         |  1       | 20.00 | 20     | ANS  | Buy    | 
When I run the pseudo clock example
Then I expect no reports to be created




