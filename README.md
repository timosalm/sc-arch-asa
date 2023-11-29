# Demo for a typical Spring Cloud Architecture on Azure Spring Cloud

## Deployment

### Azure Spring Apps Standard

#### Create backing service instances
```
POSTGRES_PW=<your-postgres-password>
az postgres server create -n asa-sc-arch-postgres-server -g asa-sc-arch -l germanywestcentral --sku-name B_Gen5_1 -u developer -p $POSTGRES_PW
az postgres db create -n order-db -s asa-sc-arch-postgres-server -g asa-sc-arch 
az redis create -n asa-sc-arch-cache -g asa-sc-arch -l germanywestcentral --sku Basic --vm-size c0
az servicebus namespace create -n asa-sc-arch-bus -g asa-sc-arch -l germanywestcentral --sku Basic
az servicebus queue create -n order-shipping-queue -g asa-sc-arch --namespace-name asa-sc-arch-bus
az servicebus queue create -n order-delivered-queue -g asa-sc-arch --namespace-name asa-sc-arch-bus 
```

#### Create apps
```
az spring app create -s asa-sc-arch -g asa-sc-arch -n product-service
az spring app create -s asa-sc-arch -g asa-sc-arch -n order-service
az spring app create -s asa-sc-arch -g asa-sc-arch -n shipping-service
```
#### Connect apps to services
```
az spring connection create redis -g asa-sc-arch --tg asa-sc-arch --service asa-sc-arch --app order-service --server asa-sc-arch-cache --database 1 --client-type springBoot --deployment default
az spring connection create postgres -g asa-sc-arch --tg asa-sc-arch --service asa-sc-arch --app order-service --server asa-sc-arch-postgres-server --database postgres --client-type springBoot --secret name=developer secret=$POSTGRES_PW --deployment default
az spring connection create servicebus -g asa-sc-arch --tg asa-sc-arch --service asa-sc-arch --app order-service --namespace asa-sc-arch-bus --client-type springBoot --deployment default
az spring connection create servicebus -g asa-sc-arch --tg asa-sc-arch --service asa-sc-arch --app shipping-service --namespace asa-sc-arch-bus --client-type springBoot --deployment default
```

#### Configure Config Server
```
az spring config-server git repo add -g asa-sc-arch -n asa-sc-arch --repo-name asa-sc-arch --uri https://github.com/timosalm/sc-arch-asa.git --search-paths config-server-configuration/
```

#### Deploy services
```
(cd product-service && az spring app deploy -s asa-sc-arch -g asa-sc-arch -n product-service --runtime-version Java_17 --source-path)
(cd order-service && az spring app deploy -s asa-sc-arch -g asa-sc-arch -n order-service --runtime-version Java_17 --source-path)
(cd shipping-service && az spring app deploy -s asa-sc-arch -g asa-sc-arch -n shipping-service --runtime-version Java_17 --source-path)
``


### Azure Spring Apps Enterprise

