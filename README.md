# Demo for a typical Spring Cloud Architecture on Azure Spring Cloud

## Azure Spring Apps Standard

### Deployment

#### Create backing service instances
```
POSTGRES_PW=<your-postgres-password>
az postgres server create -n sc-arch-postgres-server -g sc-arch -l germanywestcentral --sku-name B_Gen5_1 -u developer -p $POSTGRES_PW
az postgres db create -n order-db -s sc-arch-postgres-server -g sc-arch 
az redis create -n sc-arch-cache -g sc-arch -l germanywestcentral --sku Basic --vm-size c0
az servicebus namespace create -n sc-arch-bus -g sc-arch -l germanywestcentral --sku Basic
az servicebus queue create -n order-shipping-queue -g sc-arch --namespace-name sc-arch-bus
az servicebus queue create -n order-delivered-queue -g sc-arch --namespace-name sc-arch-bus 
```

#### Create apps
```
az spring app create -s sc-arch -g sc-arch -n product-service --env 
az spring app create -s sc-arch -g sc-arch -n order-service
az spring app create -s sc-arch -g sc-arch -n shipping-service
az spring app create -s sc-arch -g sc-arch -n gateway
az spring app create -s sc-arch -g sc-arch -n frontend --env PORT=1025
```
#### Connect apps to services
```
az spring connection create redis -g sc-arch --tg sc-arch --service sc-arch --app order-service --server sc-arch-cache --database 0 --client-type springBoot --deployment default
az spring connection create postgres -g sc-arch --tg sc-arch --service sc-arch --app order-service --server sc-arch-e-postgres-server --database order-db --client-type springBoot --secret name=developer secret=$POSTGRES_PW --deployment default
az spring connection create servicebus -g sc-arch --tg sc-arch --service sc-arch --app order-service --namespace sc-arch-bus --client-type springBoot --deployment default
az spring connection create servicebus -g sc-arch --tg sc-arch --service sc-arch --app shipping-service --namespace sc-arch-bus --client-type springBoot --deployment default
```

#### Configure Config Server
```
az spring config-server git set -g sc-arch -n sc-arch --uri https://github.com/timosalm/sc-arch-asa.git --search-paths config-server-configuration --label main
```

#### Deploy services
```
(cd product-service && az spring app deploy -s sc-arch -g sc-arch -n product-service --runtime-version Java_17 --source-path)
(cd order-service && az spring app deploy -s sc-arch -g sc-arch -n order-service --runtime-version Java_17 --source-path)
(cd shipping-service && az spring app deploy -s sc-arch -g sc-arch -n shipping-service --runtime-version Java_17 --source-path)
(cd gateway && az spring app deploy -s sc-arch -g sc-arch -n gateway --runtime-version Java_17 --assign-endpoint --source-path)
az spring app deploy -s sc-arch -g sc-arch -n frontend --container-image tap-workshops/frontend --container-registry harbor.main.emea.end2end.link
```

### Validate that the deployed application is working

```
GATEWAY_URL=$(az spring app show -s sc-arch -g sc-arch -n gateway | jq -r .properties.url)
curl $GATEWAY_URL/services/product-service/api/v1/products
curl $GATEWAY_URL/services/order-service/api/v1/orders
curl -X POST -H "Content-Type: application/json" -d '{"productId":"1", "shippingAddress": "Stuttgart"}' $GATEWAY_URL/services/order-service/api/v1/orders
curl $GATEWAY_URL/services/order-service/api/v1/orders
```

After a few seconds, the status of your created order should change to `DELIVERED`.

### Secure your application 

#### Create an App registration in Microsoft Entra
Go to the [Microsoft Entra admin center](https://entra.microsoft.com/) or access the Microsoft Entra service from your [Azure portal](https://portal.azure.com/).

Browse to `Identity > Applications > App registrations`, and click on `New registration`.
- Fill out the Name (e.g. `asa-sc-arch`)
- For the Redirect URI field, choose `Single Page Application (SPA)` as the platform. Run the following command to get the base URL for the Redirect URI `az spring app show -s sc-arch -g sc-arch -n gateway | jq -r .properties.url`, and add `/frontend/index.html` to it (e.g. https://sc-arch-gateway.azuremicroservices.io/frontend/index.html).
- Click on `Register`

Note the `Application (client) ID` on your just created App registration's Overview dashboard.

Go back to `Identity > Applications > App registrations`, and click on `Endpoints`. Note the `OpenID Connect metadata document` endpoint without the `/.well-known/openid-configuration` suffix as the issuer host (.e.g https://login.microsoftonline.com/29242f74-371f-4db2-2a50-c62b6877a0c1/v2.0).

#### Update application configuration

*TODO:* ADD info on how to set up config server config repo

```
# az spring app update -s sc-arch -g sc-arch -n product-service --env SPRING_PROFILES_ACTIVE=oauth
# az spring app update -s sc-arch -g sc-arch -n order-service --env SPRING_PROFILES_ACTIVE=oauth
az spring app update -s sc-arch -g sc-arch -n gateway --env SPRING_PROFILES_ACTIVE=oauth
```

## Azure Spring Apps Enterprise

### Deployment

#### Create backing service instances
```
POSTGRES_PW=<your-postgres-password>
az postgres server create -n sc-arch-e-postgres-server -g sc-arch-e -l germanywestcentral --sku-name B_Gen5_1 -u developer -p $POSTGRES_PW
az postgres db create -n order-db -s sc-arch-e-postgres-server -g sc-arch-e 
az redis create -n sc-arch-e-cache -g sc-arch-e -l germanywestcentral --sku Basic --vm-size c0
az servicebus namespace create -n sc-arch-e-bus -g sc-arch-e -l germanywestcentral --sku Basic
az servicebus queue create -n order-shipping-queue -g sc-arch-e --namespace-name sc-arch-e-bus
az servicebus queue create -n order-delivered-queue -g sc-arch-e --namespace-name sc-arch-e-bus 
```

#### Create apps
```
az spring app create -s sc-arch-e -g sc-arch-e -n product-service
az spring app create -s sc-arch-e -g sc-arch-e -n order-service
az spring app create -s sc-arch-e -g sc-arch-e -n shipping-service
az spring app create -s sc-arch-e -g sc-arch-e -n frontend --env PORT=8080
```
#### Connect apps to services
```
az spring connection create redis -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app order-service --server sc-arch-e-cache --database 0 --client-type springBoot --deployment default
az spring connection create postgres -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app order-service --server sc-arch-e-postgres-server --database order-db --client-type springBoot --secret name=developer secret=$POSTGRES_PW --deployment default
az spring connection create servicebus -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app order-service --namespace sc-arch-e-bus --client-type springBoot --deployment default
az spring connection create servicebus -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app shipping-service --namespace sc-arch-e-bus --client-type springBoot --deployment default
```

#### Configure Application Configuration Service
```
az spring application-configuration-service git repo add -g sc-arch-e -s sc-arch-e -n default --uri https://github.com/timosalm/sc-arch-asa.git --search-paths config-server-configuration --label main --patterns product-service,order-service,shipping-service
az spring application-configuration-service bind -g sc-arch-e -s sc-arch-e --app product-service
az spring application-configuration-service bind -g sc-arch-e -s sc-arch-e --app order-service
az spring application-configuration-service bind -g sc-arch-e -s sc-arch-e --app shipping-service
az spring app update -s sc-arch-e -g sc-arch-e -n product-service --config-file-patterns product-service
az spring app update -s sc-arch-e -g sc-arch-e -n order-service --config-file-patterns order-service
az spring app update -s sc-arch-e -g sc-arch-e -n shipping-service --config-file-patterns shipping-service
```

#### Bind apps to Service Registry
```
az spring service-registry bind -g sc-arch-e -s sc-arch-e --app product-service
az spring service-registry bind -g sc-arch-e -s sc-arch-e --app order-service
az spring service-registry bind -g sc-arch-e -s sc-arch-e --app shipping-service
```

#### Deploy services
```
(cd product-service && az spring app deploy -s sc-arch-e -g sc-arch-e -n product-service --build-env BP_JVM_VERSION=17 --source-path)
(cd order-service && az spring app deploy -s sc-arch-e -g sc-arch-e -n order-service --build-env BP_JVM_VERSION=17 --source-path)
(cd shipping-service && az spring app deploy -s sc-arch-e -g sc-arch-e -n shipping-service --build-env BP_JVM_VERSION=17 --source-path)
az spring app deploy -s sc-arch-e -g sc-arch-e -n frontend --container-image tap-workshops/frontend-optional-auth --container-registry harbor.main.emea.end2end.link
```

If you would like to build a container for the frontend yourself, run the following commands. See https://github.com/pivotal-cf/tanzu-python/issues/396 for more information on why there is a need for a customer builder with different buildpack order.
```
az spring build-service builder create -s sc-arch-e -g sc-arch-e -n frontend --builder-file frontend-builder.json
git clone https://github.com/timosalm/tap-spring-developer-workshop.git
(cd tap-spring-developer-workshop/setup/frontend && az spring app deploy -s sc-arch-e -g sc-arch-e -n frontend --build-env BP_NODE_RUN_SCRIPTS=build --build-env BP_WEB_SERVER=nginx --build-env BP_WEB_SERVER_ROOT=dist/frontend --build-env BP_WEB_SERVER_ENABLE_PUSH_STATE="true" --source-path)
```

#### Configure Spring Cloud Gateway
```
az spring gateway update -s sc-arch-e -g sc-arch-e --assign-endpoint true --https-only true 
az spring gateway route-config create -s sc-arch-e -g sc-arch-e -n order-service --app-name order-service --routes-file gateway-route-order-service.json
az spring gateway route-config create -s sc-arch-e -g sc-arch-e -n product-service --app-name product-service --routes-file gateway-route-product-service.json
az spring gateway route-config create -s sc-arch-e -g sc-arch-e -n frontend --app-name frontend --routes-file gateway-route-frontend.json
```

### Validate that the deployed application is working

```
GATEWAY_URL=https://$(az spring gateway show -s sc-arch-e -g sc-arch-e | jq -r .properties.url)
curl $GATEWAY_URL/services/product-service/api/v1/products
curl $GATEWAY_URL/services/order-service/api/v1/orders
curl -X POST -H "Content-Type: application/json" -d '{"productId":"1", "shippingAddress": "Stuttgart"}' $GATEWAY_URL/services/order-service/api/v1/orders
curl $GATEWAY_URL/services/order-service/api/v1/orders
```

After a few seconds, the status of your created order should change to `DELIVERED`.

### Configure API Portal
```
az spring api-portal update -s sc-arch-e -g sc-arch-e --assign-endpoint true
az spring api-portal show -s sc-arch-e -g sc-arch-e  | jq -r .properties.url
az spring gateway update -s sc-arch-e -g sc-arch-e --server-url $GATEWAY_URL
```

### Configure Dev Tool Portal
```
az spring dev-tool update -s sc-arch-e -g sc-arch-e --assign-endpoint true
az spring dev-tool show -s sc-arch-e -g sc-arch-e  | jq -r .properties.url
```
### Secure your application 

#### Create an App registration in Microsoft Entra
Go to the [Microsoft Entra admin center](https://entra.microsoft.com/) or access the Microsoft Entra service from your [Azure portal](https://portal.azure.com/).

Browse to `Identity > Applications > App registrations`, and click on `New registration`.
- Fill out the Name (e.g. `asa-sc-arch-e`)
- For the Redirect URI field, choose `Single Page Application (SPA)` as the platform. Run the following command to get the base URL for the Redirect URI `az spring app show -s sc-arch -g sc-arch -n gateway | jq -r .properties.url`, and add `/frontend/index.html` to it (e.g. https://sc-arch-gateway.azuremicroservices.io/frontend/index.html).
- Click on `Register`

Note the `Application (client) ID` on your just created App registration's Overview dashboard.

Go back to `Identity > Applications > App registrations`, and click on `Endpoints`. Note the `OpenID Connect metadata document` endpoint without the `/.well-known/openid-configuration` suffix as the issuer host (.e.g https://login.microsoftonline.com/29242f74-371f-4db2-2a50-c62b6877a0c1/v2.0).

*TODO* Add how to create client secret

#### Update Spring Cloud Gateway configuration
```
az spring gateway update -s sc-arch-e -g sc-arch-e --issuer-uri <issuer> --scope "openid email profile" --client-id <client-id> --client-secret <client-secret>
```