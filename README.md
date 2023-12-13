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
az spring app create -s sc-arch -g sc-arch -n product-service
az spring app create -s sc-arch -g sc-arch -n order-service
az spring app create -s sc-arch -g sc-arch -n shipping-service
az spring app create -s sc-arch -g sc-arch -n gateway
az spring app create -s sc-arch -g sc-arch -n frontend --env PORT=1025
```
#### Connect apps to services
```
az spring connection create redis -g sc-arch --tg sc-arch --service sc-arch --app order-service --server sc-arch-cache --database 0 --client-type springBoot --deployment default
az spring connection create postgres -g sc-arch --tg sc-arch --service sc-arch --app order-service --server sc-arch-postgres-server --database postgres --client-type springBoot --secret name=developer secret=$POSTGRES_PW --deployment default
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

GATEWAY_URL=$(az spring app show -s sc-arch -g sc-arch -n gateway | jq -r .properties.url)
curl $GATEWAY_URL/PRODUCT-SERVICE/api/v1/products
curl $GATEWAY_URL/ORDER-SERVICE/api/v1/orders
curl -X POST -H "Content-Type: application/json" -d '{"productId":"1", "shippingAddress": "Stuttgart"}' $GATEWAY_URL/ORDER-SERVICE/api/v1/orders

### Azure Spring Apps Enterprise

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
```
#### Connect apps to services
```
az spring connection create redis -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app order-service --server sc-arch-e-cache --database 0 --client-type springBoot --deployment default
az spring connection create postgres -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app order-service --server sc-arch-e-postgres-server --database postgres --client-type springBoot --secret name=developer secret=$POSTGRES_PW --deployment default
az spring connection create servicebus -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app order-service --namespace sc-arch-e-bus --client-type springBoot --deployment default
az spring connection create servicebus -g sc-arch-e --tg sc-arch-e --service sc-arch-e --app shipping-service --namespace sc-arch-e-bus --client-type springBoot --deployment default
```

#### Configure Config Server
```
az spring config-server git repo add -g sc-arch-e -n sc-arch-e --repo-name sc-arch-e --uri https://github.com/timosalm/sc-arch-asa.git --search-paths config-server-configuration --label main
```

#### Deploy services
```
(cd product-service && az spring app deploy -s sc-arch-e -g sc-arch-e -n product-service --build-env BP_JVM_VERSION=17 --source-path)
(cd order-service && az spring app deploy -s sc-arch-e -g sc-arch-e -n order-service --build-env BP_JVM_VERSION=17 --source-path)
(cd shipping-service && az spring app deploy -s sc-arch-e -g sc-arch-e -n shipping-service --build-env BP_JVM_VERSION=17 --source-path)
```

### Configure Gateway Routes
```
az spring gateway route-config create -s sc-arch-e -g sc-arch-e -n order-service --app-name order-service --routes-file gateway-route-order-service.json
az spring gateway route-config create -s sc-arch-e -g sc-arch-e -n product-service --app-name product-service --routes-file gateway-route-product-service.json
```