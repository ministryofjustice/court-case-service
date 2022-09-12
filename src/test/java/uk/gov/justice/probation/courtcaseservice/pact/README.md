# Pact testing of `court-case-service` 


`court-case-service` is the Pact Provider service for two Pact Consumer services, `prepare-a-case` and `court-case-matcher`. These services have their own set of Pact tests which are verified against `court-case-service` in the build pipeline. 

## ✅ Verifying the Pact contracts for the `main` branch of each consumer

The gradle task `pactVerifyPublish` will fetch each of the latest `main` branch consumer versions for each consumer and verify the contract against the states defined in the `VerificationPactTests` in this directory. This is useful for confirming that local changes don't break existing contracts with consumers or for debugging any contract breaches that do happen.  This task requires the following environmental variables to be set so it's most convenient to create a run configuration for this in your IDE.

Note: The broker username and password can be fetched from Kubernetes using this snippet `kubectl get secret  basic-auth -o json -n pact-broker-prod | jq '.data | map_values(@base64d)'`

```
PACTBROKER_AUTH_USERNAME=<username>;
PACTBROKER_AUTH_PASSWORD=<password>;
PACTBROKER_URL=https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk
```

## 🌳 Verifying changes on a consumer branch

To make sure that these tests don't fail in the pipeline you can run the consumer contracts tagged with a branch name on your local machine before pushing it to the remote repository. To do this specify a `PACT_CONSUMER_TAG` environmental variable to set the tag to the specific branch name that you want to test against. The following line of code in the PactTest configures the specific contract which will be fetched from the [Pact Broker](https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk).

```
@PactBroker(consumerVersionSelectors = @VersionSelector(consumer = "court-case-matcher", tag="${PACT_CONSUMER_TAG}", fallbackTag = "main"))
```

When you then run `./gradlew pactTestPublish` you can then confirm it has found the right version by checking that the following notice is printed to the console and has the correct value after `latest version of <consumer> tagged '<tag>'`.

e.g.
```
1) The pact at https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk/pacts/provider/court-case-service/consumer/court-case-matcher/pact-version/0b3c98d992e384247403a95acbdb7b899deb0c49 is being verified because the pact content belongs to the consumer version matching the following criterion:
    * latest version of court-case-matcher tagged 'PIC-2290-fix' (3f1514fa0efc20299b66f64fb28bfa345e4d76a5)
```

## 💥 The verification failed! 

Part of the challenge with contract testing is understanding which link in the chain has broken so you can focus on that part and ignore the many other parts. Taking a few minutes before you dive in to get this clear in your head is well worthwhile.

That being the case, a failure at the `verification` stage tells us that the consumer has a well-defined contract and its tests are passing against that contract, if this wasn't the case then the consumer wouldn't have been able to publish to the Pact Broker. In other words, the consumer might be expecting the wrong thing of the provider but its consistent with its own expectations. So starting with the assumption that the consumer is probably asking for the right thing, we need to check the provider (`court-case-service`) is actually providing what it's supposed to, if the consumer's expectations are wrong then this will usually become apparent when we find the problem. 

Typical failures at this stage are:
- A **state** that the consumer is expecting doesn't exist or has a different name
  - Fix - Create or update the state name in either the consumer or provider
- The correct provider state exists but the **identifiers** passed by the consumer are wrong so the wrong results (or more likely no results) are returned
  - Fix - Change the consumer identifiers so they find the expected results
- The state and identifiers are correct but some key bit of **data** is missing from the returned response
  - Fix - Update the provider state to return the expected values from the service mocks (or DB for some older tests - this pattern is deprecated though, see [ADR-10](../../../../../../../../../doc/adr/0010-decoupling-pact-tests-from-test-data.md))
- All of the above is correct but a field is missing from the **response DTO**
  - Fix - Add the missing field to the response DTO
