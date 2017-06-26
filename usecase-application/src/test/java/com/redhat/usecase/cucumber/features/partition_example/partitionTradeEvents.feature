Feature: Test Feature to demonstrate partitioning rules and data

Scenario: Multiple trades in 5 minutes with same Investor and FA
Given Trade Events:
|Id|Investor Id|Advisor Id| Price |Quantity|Symbol|Type    |  Timestamp           |
|1 | 1         |  3       | 20.00 | 10     | ANS  | Buy    |  27-09-1990 01:29:33 |
|2 | 1         |  5       | 20.00 | 11     | ABS  | Sell   |  27-09-1990 01:28:08 |
|3 | 2         |  2       | 20.00 | 15     | HNG  | Sell   |  27-09-1990 13:25:13 |
|4 | 3         |  1       | 20.00 | 20     | TYI  | Buy    |  27-09-1990 19:27:10 |
|5 | 3         |  1       | 20.00 | 20     | JKJ  | Buy    |  27-09-1990 19:27:22 |
|6 | 3         |  1       | 20.00 | 20     | ANS  | Buy    |  27-09-1990 19:30:09 |
When I run the partition example
Then I expect the following Reports to be created:
|Code                                 | Trades           |
|Excessive Trading with one Investor  | 5,6              |
|Excessive Trading with one Investor  | 4,5,6            |
|Excessive Trading with one Investor  | 1,2              |
|Excessive Trading with one FA        | 4,5,6            |
|Excessive Trading with one FA        | 5,6              |




