# Strava Integration App 
A server application for [Strava Apps](https://www.strava.com/apps). Users can sign up enabling the server to receive push events from Strava as they upload new activities.
The Stava Swagger spec is used with OpenAPI to generate Strava API classes which are then used to get and/or update user
activities as we are notified about them.

## Build
### Strava OpenAPI Code-gen
```shell
mvn clean compile
```
N.B. Strava have only published a [swagger 2.0 spec](https://developers.strava.com/swagger/swagger.json) which I have converted to an [OpenAPI 3.0.1 spec](src/main/resources/strava/api/strava-api-v301.yaml).


### Package into fat Jar
```shell
mvn clean package
```


## Configure
Make a copy of `config.example.properties` to `config.properties` with the applications details.

## Run
Run Server via an IDE or run the Jar directly once built:
```shell
java -jar target/StravaApp-1.0-SNAPSHOT-jar-with-dependencies.jar 
```

## Deploy
Ensure the Jar has been built, and configs set. Then can run
```shell
cd terraform
terraform init
terraform apply
```

May need to run `terraform destroy` to redeploy changes if only code changes are made and no infra changes.