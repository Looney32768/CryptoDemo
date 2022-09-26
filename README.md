# How to run
Entry point is `org.cryptodemo.CryptoDemoApplication`
`./gradlew clean bootRun`

# Where is documentation
JavaDocs added to public methods except for `AppController` class.
For REST endpoints Swagger is used for documentation generation, it is available at http://localhost:8080/swagger-ui/index.html

# What is csvjdbc
It is a JDBC driver for accessing folder/archive with CSV files in read-only mode. 

Taken from https://sourceforge.net/projects/csvjdbc/ 

Docs available at https://github.com/simoc/csvjdbc/blob/master/docs/doc.md

# How to build Docker image
`./gradlew clean bootBuildImage` => docker.io/library/cryptodemo:0.0.1-SNAPSHOT

# Run docker image
`docker run -p 127.0.0.1:8080:8080 cryptodemo:0.0.1-SNAPSHOT`

# Import docker image into microk8s
`docker save cryptodemo > cryptodemo.tar`
`microk8s ctr image import cryptodemo.tar`

# How to prepare k8s deployment
`microk8s kubectl create deployment cryptodemo --image=cryptodemo:0.0.1-SNAPSHOT --dry-run=client -o=yaml > deployment.yaml`

# How to launch the deployment
`microk8s kubectl apply -f deployment.yaml` / `microk8s kubectl delete -f deployment.yaml` 

Check status with `microk8s kubectl get all` / `microk8s kubectl get services`

# Enable ingress
`microk8s enable ingress`
 
I can't go further for free ...