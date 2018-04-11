# revolut-test-task

This task doesn't suit well to RESTful API, but I tried hard to mimic that it's fine.

## General usage

You can obtain basic understanding of API in tests (ApiTests class), but it should be used in flow like that:

1. Create account (`POST /account`) and get its id in response
2. Fullfill this account (`PATCH /account/{id}/fullfill/{amount}`)
3. Repeat for another accounts
4. Transfer money between them with (`PATCH /account/{from}/transfer/{to}/{amount}`)

All amounts are BigDecimals, so you can safely pass any numbers there

Application is thread-safe, coverage is 100%. during build it's tested with mutation tests.

I didn't write unit tests because on such scale 100% coverage by API tests doesn't require us to have unit tests in such simple business logics case.
