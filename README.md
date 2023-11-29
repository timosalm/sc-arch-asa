# Demo for a typical Spring Cloud Architecture on Azure Spring Cloud

## Deployment


### Azure Spring Apps Standard

#### Configure Config Server
```
az spring config-server git repo add -g asa-sc-arch -n asa-sc-arch --repo-name asa-sc-arch --uri https://github.com/timosalm/sc-arch-asa.git --search-paths config-server-configuration/
```

#### Deploy services
```
(cd product-service && az spring app create -s asa-sc-arch -g asa-sc-arch -n product-service  && az spring app deploy -s asa-sc-arch -g asa-sc-arch -n product-service --runtime-version Java_17 --source-path)
``