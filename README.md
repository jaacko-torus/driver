# Driver

## Dev setup

Make sure to run the following commands before starting development. Bloop is needed regardless of whether using
IntelliJ or metals. This project was built using IntelliJ so there are no guarantees it will work properly in metals.

```sh
git clone https://github.com/jaacko-torus/driver
cd driver
```

```sh
mill mill.bsp.BSP/install
mill mill.scalalib.GenIdea/idea
```

### IntelliJ folder tags:

```
driver @ content root
└── src
    ├── main
    │   ├── resources @ resources root
    │   │   └── client @ exclude
    │   │       └── ...
    │   └── scala @ source root
    │       └── ...
    └── test
        └── scala @ test root
            └── ...
```

# Docker

```shell
docker build -t jaacko-torus/driver:0.1.0 .
docker run -p 9000:9000 -p 9001:9001 jaacko-torus/driver:0.1.0
```