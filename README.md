##Java developer challenge

ToDo:
* Implement converter service that support minimum two cross currencies to get rate (e.g.: BTC/RUB = BTC->EUR->USD->RUB)
* Complete test suite
* Use BigDecimal, be care with divide operation

As additional tasks you may extend test suite and service for following cases:
* small values (rounding)
* catch exceptions if rate could be found
* find minimal rate with 1 cross limit (example: BTC->USD = 60000, BTC->LTC->USD = 58000)