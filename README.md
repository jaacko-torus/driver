# Driver

## Server

To get the server up and running, run the following commands:

```shell
git clone https://github.com/jaacko-torus/driver
cd driver
sbt
run
# or:
# `run [options]`.
# `run --help` for more options.
# Alternatively: `sbt "run [options]"`.
```

## For devs

This project was created with IntelliJ, and has not been tested with metals.
This means that no other development server is *actively* supported.
Make sure to do all development in the [dev branch][dev-branch].

### Docker

To get the project running on docker, you should run the following commands:

```shell
# Build
sbt dockerBuildAndPush
# Or `sbt docker` in the case a publish is unwanted.
docker run -p 80:80 -p 8081:8081 jaackotorus/driver:latest
# or whatever version tag you are using
```

[dev-branch]: https://github.com/jaacko-torus/driver/tree/dev