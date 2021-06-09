## Java developer challenge ##

ToDo:
1. Create converter service that support minimum two cross currencies to get rate (e.g.: BTC/RUB = BTC->EUR->USD->RUB)
2. Implement gRPC service RatesService
3. Complete test suite against implemented gRPC service (via inprocess communication)
4. Use BigDecimal, be care with divide operation

As additional tasks you may extend test suite and service for following cases:
1. Test conversion small values (check rounding)
2. Catch exceptions if rate couldn't be found
3. Find minimal rate with 1 cross currency limit (example: BTC->USD = 60000, BTC->LTC->USD = 58000)